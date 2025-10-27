package com.b1a4.cafeOn.image.service;

import com.b1a4.cafeOn.community.post.entity.PostEntity;
import com.b1a4.cafeOn.image.entity.ImageEntity;
import com.b1a4.cafeOn.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Service s3Service;

    @Transactional
    public ImageEntity attachNewImageToPost(PostEntity post, S3Service.UploadedImageInfo uploaded) {
        if (uploaded == null) {
            throw new IllegalArgumentException("업로드된 이미지 정보가 없습니다.");
        }

        ImageEntity image = ImageEntity.builder()
                .post(post)
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
    public void removeImage(ImageEntity image) {
        if (image == null) return;

        s3Service.deleteImageByKey(image.getS3Key());

        imageRepository.delete(image);
    }


    @Transactional
    public void removeAllImagesOfPost(PostEntity post) {
        List<ImageEntity> images = imageRepository.findByPost(post);
        for (ImageEntity img : images) {
            s3Service.deleteImageByKey(img.getS3Key());
        }
        imageRepository.deleteAll(images);

        if (post.getImages() != null) {
            post.getImages().clear();
        }
    }


    @Transactional
    public void updatePostImages(PostEntity post, List<Long> keepImageIds,
                                 List<S3Service.UploadedImageInfo> newlyUploaded) {
        if (keepImageIds == null) {
            keepImageIds = List.of();
        }
        if (newlyUploaded == null) {
            newlyUploaded = List.of();
        }

        List<ImageEntity> currentImages = imageRepository.findByPost(post);

        for (ImageEntity img : currentImages) {
            if (!keepImageIds.contains(img.getImageId())) {
                removeImage(img);
            }
        }

        for (S3Service.UploadedImageInfo info : newlyUploaded) {
            attachNewImageToPost(post, info);
        }

        List<ImageEntity> refreshed = imageRepository.findByPost(post);
        post.getImages().clear();
        post.getImages().addAll(refreshed);
    }
}
