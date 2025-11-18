package com.b1a4.cafeOn.image.service;

import com.b1a4.cafeOn.chat.entity.ChatEntity;
import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import com.b1a4.cafeOn.image.repository.ImageRepository;
import com.b1a4.cafeOn.review.entity.ReviewEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.awt.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Service s3Service;

    @Transactional
    public ImageEntity attachNewImageCommon(PostEntity post, ReviewEntity review, ChatEntity chat,
                                            UploadedImageInfo uploaded) {
        if (uploaded == null) {
            throw new IllegalArgumentException("업로드된 이미지 정보가 없습니다.");
        }

        ImageEntity image = ImageEntity.builder()
                .post(post)
                .review(review)
                .chat(chat)
                .s3Key(uploaded.getS3Key())
                .publicUrl(uploaded.getPublicUrl())
                .originalFileName(uploaded.getOriginalFileName())
                .contentType(uploaded.getContentType())
                .sizeBytes(uploaded.getSizeBytes())
                .build();

        ImageEntity saved = imageRepository.save(image);

        return saved;
    }

    @Transactional
    private void removeImageCommon(ImageEntity image) {
        if (image == null) return;

        s3Service.deleteImageByKey(image.getS3Key());
        imageRepository.delete(image);
    }

    @Transactional
    private void bulkRemoveImagesCommon(List<ImageEntity> images) {
        for (ImageEntity img : images) {
            s3Service.deleteImageByKey(img.getS3Key());
        }
        imageRepository.deleteAll(images);
    }


    // 부모 엔티티 쪽 컬렉션과 동기화
    @Transactional
    private void syncImagesForPost(PostEntity post) {
        List<ImageEntity> refreshed = imageRepository.findByPost(post);
        post.getImages().clear();
        post.getImages().addAll(refreshed);
    }

    @Transactional
    private void syncImagesForReview(ReviewEntity review) {
        List<ImageEntity> refreshed = imageRepository.findByReview(review);
        review.getImages().clear();
        review.getImages().addAll(refreshed);
    }

    @Transactional
    private void syncImagesForChat(ChatEntity chat) {
        List<ImageEntity> refreshed = imageRepository.findByChat(chat);
        chat.getImages().clear();
        chat.getImages().addAll(refreshed);
    }


    // post
    // 추가
    @Transactional
    public ImageEntity attachNewImageToPost(PostEntity post, UploadedImageInfo uploaded) {
        return attachNewImageCommon(post, null, null, uploaded);
    }

    // 수정
    @Transactional
    public void updatePostImages(PostEntity post, List<Long> keepImageIds, List<UploadedImageInfo> newlyUploaded) {
        if (keepImageIds == null) keepImageIds = List.of();
        if (newlyUploaded == null) newlyUploaded = List.of();

        List<ImageEntity> currentImage = imageRepository.findByPost(post);

        for (ImageEntity img : currentImage) {
            if (!keepImageIds.contains(img.getImageId())) {
                removeImageCommon(img);
            }
        }

        for (UploadedImageInfo info : newlyUploaded) {
            attachNewImageToPost(post, info);
        }

        syncImagesForPost(post);
    }

    // 삭제
    @Transactional
    public void removeAllImagesOfPost(PostEntity post) {
        List<ImageEntity> images = imageRepository.findByPost(post);
        bulkRemoveImagesCommon(images);

        if (post.getImages() != null) {
            post.getImages().clear();
        }
    }


    // review
    // 추가
    public ImageEntity attachNewImageToReview(ReviewEntity review, UploadedImageInfo uploaded) {
        return attachNewImageCommon(null, review, null, uploaded);
    }

    // 수정
    public void updateReviewImages(ReviewEntity review, List<Long> keepImagedIds, List<UploadedImageInfo> newlyUploaded) {
        if (keepImagedIds == null) keepImagedIds = List.of();
        if (newlyUploaded == null) newlyUploaded = List.of();

        List<ImageEntity> currentImages = imageRepository.findByReview(review);

        for (ImageEntity img : currentImages) {
            if (!keepImagedIds.contains(img.getImageId())) {
                removeImageCommon(img);
            }
        }

        for (UploadedImageInfo info : newlyUploaded) {
            attachNewImageToReview(review, info);
        }

        syncImagesForReview(review);
    }

    // 삭제
    public void removeAllImagesOfReview(ReviewEntity review) {
        List<ImageEntity> images = imageRepository.findByReview(review);
        bulkRemoveImagesCommon(images);

        if (review.getImages() != null) {
            review.getImages().clear();
        }
    }


    // chat
    // 채팅 삭제(마지막 멤버가 채팅방 나가기)
    @Transactional
    public void removeAllImagesOfChatRoom(Long roomId) {
        List<String> keys = imageRepository.findAllS3KeysByRoomId(roomId);

        imageRepository.bulkDeleteByRoomId(roomId);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                s3Service.deleteAll(keys);
            }
        });
    }


}