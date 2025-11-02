package com.b1a4.cafeOn.community.comment.service;

import com.b1a4.cafeOn.community.comment.dto.CommentRequestDTO;
import com.b1a4.cafeOn.community.comment.dto.CommentResponseDTO;
import com.b1a4.cafeOn.community.comment.entity.CommentEntity;
import com.b1a4.cafeOn.community.comment.exception.CommentNotFoundException;
import com.b1a4.cafeOn.community.comment.exception.ParentCommentMismatchException;
import com.b1a4.cafeOn.community.comment.repository.CommentLikeRepository;
import com.b1a4.cafeOn.community.comment.repository.CommentRepository;
import com.b1a4.cafeOn.community.comment.repository.LikeCount;
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
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 생성
    @Transactional
    public CommentResponseDTO createComment(String userId, Long postId, Long parentId, CommentRequestDTO commentRequestDTO) {
        UserEntity author = findByUserId(userId);
        PostEntity post = findByPostId(postId);

        Long resolvedParentId = (commentRequestDTO.getParentId() != null) ? commentRequestDTO.getParentId() : parentId;
        CommentEntity parent = null;
        if (resolvedParentId != null) {
            // 부모는 같은 게시글에 속해야 함
            parent = commentRepository.findByCommentIdAndPost_PostId(resolvedParentId, postId)
                    .orElseThrow(() -> new ParentCommentMismatchException(resolvedParentId, postId));
        }

        CommentEntity saveComment = commentRepository.save(
                CommentEntity.builder()
                        .parent(parent)
                        .content(commentRequestDTO.getContent())
                        .post(post)
                        .user(author)
                        .build()
        );

        return CommentResponseDTO.from(saveComment);
    }

    // 게시글 상세: 루트 페이징 + 서브트리
    @Transactional(readOnly = true)
    public Page<CommentResponseDTO> getPostCommentsAsTree(Long postId, String userId, Pageable pageable) {
        Page<CommentEntity> roots = commentRepository.findByPost_PostIdAndParentIsNull(postId, pageable);
        if (roots.isEmpty()) return new PageImpl<>(List.of(), pageable, 0);

        // 루트 + 모든 자손 BFS로 수집
        List<CommentEntity> all = collectSubtreeBfs(roots.getContent());

        // 좋아요 배치(카운트/내가 누름)
        LikeBatch batch = loadLikeBatch(all, userId);

        // DTO로 변환 후 parent-child 연결
        Map<Long, CommentResponseDTO> dtoById = toDtoMap(all, batch.likeCountMap, batch.likedIds);
        linkParentChild(all, dtoById);

        // 루트만 뽑아 페이지 구성
        List<CommentResponseDTO> content = roots.getContent().stream()
                .map(r -> dtoById.get(r.getCommentId()))
                .toList();

        return new PageImpl<>(content, pageable, roots.getTotalElements());
    }


    // 특정 댓글 단건 조회
    @Transactional(readOnly = true)
    public CommentResponseDTO findCommentById(Long commentId, String userId) {
        findByUserId(userId);

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        long likeCount = commentLikeRepository.countByComment(comment);
        boolean likedByMe = commentLikeRepository.existsByComment_CommentIdAndUser_UserId(commentId, userId);

        return CommentResponseDTO.from(comment, likeCount, likedByMe);
    }


    // 특정 댓글 + 자식들(서브트리)
    @Transactional(readOnly = true)
    public CommentResponseDTO getCommentWithChildren(Long commentId, String userId) {
        CommentEntity root = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        List<CommentEntity> all = collectSubtreeBfs(List.of(root));
        LikeBatch batch = loadLikeBatch(all, userId);
        Map<Long, CommentResponseDTO> dtoById = toDtoMap(all, batch.likeCountMap, batch.likedIds);
        linkParentChild(all, dtoById);

        CommentResponseDTO rootDto = dtoById.get(root.getCommentId());
        // 정렬
        sortRecursively(rootDto.getChildren());
        return rootDto;
    }


    // 내가 작성한 댓글 목록
    @Transactional(readOnly = true)
    public Page<CommentResponseDTO> getCommentByUserId(String userId, Pageable pageable) {
        findByUserId(userId);

        Page<CommentEntity> page = commentRepository.findByUser_UserId(userId, pageable);
        List<Long> ids = page.getContent().stream().map(CommentEntity::getCommentId).toList();

        Map<Long, Long> likeCountMap = ids.isEmpty() ? Map.of()
                : commentLikeRepository.findLikeCountByCommentIdIn(ids).stream()
                .collect(Collectors.toMap(LikeCount::getCommentId, LikeCount::getCnt));

        return page.map(c -> CommentResponseDTO.from(
                c,
                likeCountMap.getOrDefault(c.getCommentId(), 0L),
                false // 내 목록에서는 likedByMe를 안 씀
        ));
    }
    
    // 내가 좋아요한 댓글 목록
    @Transactional(readOnly = true)
    public Page<CommentResponseDTO> getLikeCommentByUserId(String userId, Pageable pageable) {
        // 유저 검증
        findByUserId(userId);

        // 좋아요한 댓글 ID
        Page<Long> pageCommentIds = commentLikeRepository.findLikedCommentIdsByUserId(userId, pageable);
        List<Long> commentIds = pageCommentIds.getContent();

        if (commentIds.isEmpty()) {
            return Page.empty(pageable);
        }

        List<CommentEntity> comments = commentRepository.findByCommentIdIn(commentIds);

        // 재정렬
        Map<Long, CommentEntity> byId = comments.stream()
                .collect(Collectors.toMap(CommentEntity::getCommentId, c -> c));
        List<CommentEntity> ordered = commentIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull) // 누락된 경우 방어
                .toList();

        // 좋아요 카운트 배치 로딩 (ids 기준)
        Map<Long, Long> likeCountMap = commentLikeRepository.findLikeCountByCommentIdIn(commentIds).stream()
                .collect(Collectors.toMap(LikeCount::getCommentId, LikeCount::getCnt));

        List<CommentResponseDTO> content = ordered.stream()
                .map(c -> CommentResponseDTO.from(
                        c,
                        likeCountMap.getOrDefault(c.getCommentId(), 0L),
                        true
                ))
                .toList();

        return new PageImpl<>(content, pageable, pageCommentIds.getTotalElements());
    }



    // 수정
    @Transactional
    public CommentResponseDTO updateComment(Long commentId, Long postId, String userId, CommentRequestDTO req) {
        findByUserId(userId);

        CommentEntity comment = commentRepository.findByCommentIdAndPost_PostId(commentId, postId)
                .orElseThrow(() -> new ParentCommentMismatchException(commentId, postId));

        if (!Objects.equals(comment.getUser().getUserId(), userId)) {
            throw new AccessDeniedException("댓글을 수정할 권한이 없습니다.");
        }

        comment.update(req.getContent());
        return CommentResponseDTO.from(comment);
    }


    // 삭제 (작성자/ADMIN)
    @Transactional
    public void deleteComment(Long commentId, Long postId, String userId) {
        findByUserId(userId);

        CommentEntity comment = commentRepository.findByCommentIdAndPost_PostId(commentId, postId)
                .orElseThrow(() -> new ParentCommentMismatchException(commentId, postId));

        boolean isOwner = comment.getUser() != null && Objects.equals(comment.getUser().getUserId(), userId);
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("댓글을 삭제할 권한이 없습니다.");
        }
        commentRepository.delete(comment); // 댓글 좋아요도 삭제
    }


    // 내부 유틸(공통)
    private record LikeBatch(Map<Long, Long> likeCountMap, Set<Long> likedIds) {
    }

    // 루트 목록의 모든 자손을 BFS로 수집
    private List<CommentEntity> collectSubtreeBfs(List<CommentEntity> roots) {
        List<CommentEntity> all = new ArrayList<>(roots);
        Deque<Long> frontier = new ArrayDeque<>(roots.stream().map(CommentEntity::getCommentId).toList());

        while (!frontier.isEmpty()) {
            List<Long> parentIds = new ArrayList<>();
            while (!frontier.isEmpty()) parentIds.add(frontier.poll());

            // 빈 리스트 방어
            if (parentIds.isEmpty()) break;

            List<CommentEntity> children = commentRepository.findByParent_CommentIdIn(parentIds);
            if (children.isEmpty()) break;

            all.addAll(children);
            frontier.addAll(children.stream().map(CommentEntity::getCommentId).toList());
        }
        return all;
    }

    // 좋아요 카운트/내가 누른 댓글을 한 번에 로딩
    private LikeBatch loadLikeBatch(List<CommentEntity> all, String userId) {
        List<Long> ids = all.stream().map(CommentEntity::getCommentId).toList();

        Map<Long, Long> likeCountMap = ids.isEmpty() ? Map.of()
                : commentLikeRepository.findLikeCountByCommentIdIn(ids).stream()
                .collect(Collectors.toMap(LikeCount::getCommentId, LikeCount::getCnt));

        Set<Long> likedIds = (userId == null || userId.isBlank() || ids.isEmpty())
                ? Set.of()
                : new HashSet<>(commentLikeRepository.findLikedCommentIds(userId, ids));

        return new LikeBatch(likeCountMap, likedIds);
    }

    // DTO로 변환(좋아요 정보 포함)
    private Map<Long, CommentResponseDTO> toDtoMap(List<CommentEntity> all,
                                                   Map<Long, Long> likeCountMap,
                                                   Set<Long> likedIds) {
        Map<Long, CommentResponseDTO> dtoById = new LinkedHashMap<>();
        for (CommentEntity e : all) {
            dtoById.put(
                    e.getCommentId(),
                    CommentResponseDTO.from(
                            e,
                            likeCountMap.getOrDefault(e.getCommentId(), 0L),
                            likedIds.contains(e.getCommentId())
                    )
            );
        }
        return dtoById;
    }

    // DTO parent-child 연결
    private void linkParentChild(List<CommentEntity> all, Map<Long, CommentResponseDTO> dtoById) {
        for (CommentEntity e : all) {
            if (e.getParent() != null) {
                CommentResponseDTO parentDto = dtoById.get(e.getParent().getCommentId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dtoById.get(e.getCommentId()));
                }
            }
        }
    }

    // 생성일 기준 재귀 정렬
    private void sortRecursively(List<CommentResponseDTO> list) {
        list.sort(Comparator.comparing(CommentResponseDTO::getCreatedAt));
        for (CommentResponseDTO c : list) {
            if (c.getChildren() != null && !c.getChildren().isEmpty()) {
                sortRecursively(c.getChildren());
            }
        }
    }

    private UserEntity findByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));
    }

    private PostEntity findByPostId(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }
}
