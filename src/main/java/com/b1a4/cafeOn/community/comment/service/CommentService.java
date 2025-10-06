package com.b1a4.cafeOn.community.comment.service;

import com.b1a4.cafeOn.community.comment.dto.CommentRequestDTO;
import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import com.b1a4.cafeOn.community.comment.exception.CommentNotFoundException;
import com.b1a4.cafeOn.community.comment.exception.ParentCommentMismatchException;
import com.b1a4.cafeOn.community.comment.repository.CommentRepository;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.community.post.exception.PostNotFoundException;
import com.b1a4.cafeOn.community.post.repository.PostRepository;
import com.b1a4.cafeOn.user.entity.UserEntity;
import com.b1a4.cafeOn.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 생성
    @Transactional
    public CommentResponseDTO createComment(String userId, Long postId, Long parentId, CommentRequestDTO commentRequestDTO) {

        // 유저 검증
        UserEntity author = findByUserId(userId);

        // 게시글 검증
        PostEntity post = findByPostId(postId);

        // body의 parentId를 우선적으로 사용하고 없으면 path의 parentId 사용
        Long resolvedParentId = (commentRequestDTO.getParentId() != null) ? commentRequestDTO.getParentId() : parentId;

        CommentEntity parent = null;
        if (resolvedParentId != null) {

            parent = commentRepository.findByCommentIdAndPost_PostId(resolvedParentId, postId)
                    .orElseThrow(() -> new ParentCommentMismatchException(resolvedParentId, postId));

        }

        CommentEntity saveComment = CommentEntity.builder()
                .parent(parent)
                .content(commentRequestDTO.getContent())
                .post(post)
                .user(author)
                .build();

        CommentEntity saved = commentRepository.save(saveComment);

        return CommentResponseDTO.from(saved);

    }


    // 게시글 상세 댓글 목록
    @Transactional(readOnly = true)
    public Page<CommentResponseDTO> getPostCommentsAsTree(Long postId, Pageable pageable) {
        Page<CommentEntity> roots = commentRepository.findByPost_PostIdAndParentIsNull(postId, pageable);

        Map<Long, CommentResponseDTO> dtoById = new LinkedHashMap<>();
        for (CommentEntity root : roots.getContent()) {
            dtoById.put(root.getCommentId(), CommentResponseDTO.from(root)); // children은 빈 리스트 보장
        }

        Deque<Long> frontier = new ArrayDeque<>(dtoById.keySet());
        while (!frontier.isEmpty()) {
            List<Long> parentIds = new ArrayList<>();
            while (!frontier.isEmpty()) parentIds.add(frontier.poll());

            List<CommentEntity> children = commentRepository.findByParent_CommentIdIn(parentIds);
            if (children.isEmpty()) break;

            Deque<Long> next = new ArrayDeque<>();
            for (CommentEntity child : children) {
                CommentResponseDTO childDto = CommentResponseDTO.from(child);
                dtoById.put(childDto.getCommentId(), childDto);

                Long pId = child.getParent().getCommentId();
                CommentResponseDTO parentDto = dtoById.get(pId);
                if (parentDto != null) parentDto.getChildren().add(childDto);

                next.add(childDto.getCommentId());
            }
            frontier = next;
        }

        List<CommentResponseDTO> content = roots.getContent().stream()
                .map(e -> dtoById.get(e.getCommentId()))
                .toList();

        return new PageImpl<>(content, roots.getPageable(), roots.getTotalElements());
    }


    // 특정 댓글 한개만 조회
    @Transactional(readOnly = true)
    public CommentResponseDTO findCommentById(Long commentId, String userId) {

        findByUserId(userId);

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        return CommentResponseDTO.from(comment);
    }

    // 특정 댓글 상세 조회(부모/자식)
    @Transactional(readOnly = true)
    public CommentResponseDTO getCommentWithChildren(Long commentId) {
        // (선택) 접근 정책에 따라 필요하면 유저 검증
        // findByUserId(userId);

        CommentEntity root = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        // 루트 DTO 준비
        Map<Long, CommentResponseDTO> dtoById = new LinkedHashMap<>();
        CommentResponseDTO rootDto = CommentResponseDTO.from(root);
        dtoById.put(rootDto.getCommentId(), rootDto);

        // BFS로 모든 자식 붙이기
        Deque<Long> frontier = new ArrayDeque<>();
        frontier.add(root.getCommentId());

        while (!frontier.isEmpty()) {
            List<Long> parentIds = new ArrayList<>();
            while (!frontier.isEmpty()) parentIds.add(frontier.poll());

            var children = commentRepository.findByParent_CommentIdIn(parentIds);
            if (children.isEmpty()) break;

            Deque<Long> next = new ArrayDeque<>();
            for (CommentEntity child : children) {
                CommentResponseDTO childDto = CommentResponseDTO.from(child);
                dtoById.put(childDto.getCommentId(), childDto);

                Long pId = child.getParent().getCommentId();
                CommentResponseDTO parentDto = dtoById.get(pId);
                if (parentDto != null) parentDto.getChildren().add(childDto);

                next.add(child.getCommentId());
            }
            frontier = next;
        }

        // (선택) 생성일 기준 정렬
        sortRecursively(rootDto.getChildren());
        return rootDto;
    }

    private void sortRecursively(List<CommentResponseDTO> list) {
        list.sort(Comparator.comparing(CommentResponseDTO::getCreatedAt));
        for (var c : list) {
            if (c.getChildren() != null && !c.getChildren().isEmpty()) {
                sortRecursively(c.getChildren());
            }
        }
    }


    // 내가 작성한 댓글 목록
    // fixme: mypage 브랜치로 이동
    @Transactional(readOnly = true)
    public Page<CommentResponseDTO> getCommentByUserId(String userId, Pageable pageable) {

        findByUserId(userId);

        Page<CommentEntity> myCommentList = commentRepository.findByUser_UserId(userId, pageable);

        return myCommentList.map(CommentResponseDTO::from);
    }

    // 내가 좋아요한 댓글 목록
    // fixme


    // 댓글 수정
    @Transactional
    public CommentResponseDTO updateComment(Long commentId, Long postId, String userId, CommentRequestDTO commentRequestDTO) {

        findByUserId(userId);

        CommentEntity comment = commentRepository.findByCommentIdAndPost_PostId(commentId, postId)
                .orElseThrow(() -> new ParentCommentMismatchException(commentId, postId));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("댓글을 수정할 권한이 없습니다.");
        }

        comment.update(commentRequestDTO.getContent());

        return CommentResponseDTO.from(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Long postId, String userId) {
        findByUserId(userId);

        CommentEntity comment = commentRepository.findByCommentIdAndPost_PostId(commentId, postId)
                .orElseThrow(() -> new ParentCommentMismatchException(commentId, postId));

        boolean isOwner = comment.getUser() != null && comment.getUser().getUserId().equals(userId);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }


    // 유저 검증
    private UserEntity findByUserId(String userId) {
        UserEntity author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));
        return author;
    }

    // 게시글 검증
    private PostEntity findByPostId(Long postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        return post;
    }

}
