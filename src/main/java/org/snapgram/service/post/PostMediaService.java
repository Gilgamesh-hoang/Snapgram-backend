package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CloudinaryMedia;
import org.snapgram.dto.CloudinaryPojo;
import org.snapgram.entity.database.Post;
import org.snapgram.entity.database.PostMedia;
import org.snapgram.enums.MediaType;
import org.snapgram.exception.UploadFileException;
import org.snapgram.repository.database.PostMediaRepository;
import org.snapgram.service.upload.MediaUploader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostMediaService {
    MediaUploader uploader;
    PostMediaRepository postMediaRepository;

    @Transactional
    public List<PostMedia> savePostMedia(MultipartFile[] media, UUID postId) {
        // Create an ExecutorService with a fixed thread pool.
        ExecutorService executor = Executors.newFixedThreadPool(4);
        // Create a list to hold Future objects.
        List<Future<PostMedia>> futures = new ArrayList<>();

        for (MultipartFile file : media) {
            // Submit the task for execution and add its Future to the list.
            futures.add(executor.submit(() -> {

                try {
                    CloudinaryPojo cloudinary = uploader.uploadFile(file);
                    MediaType type = null;
                    if (cloudinary.getResourceType().equals("image")) {
                        type = MediaType.IMAGE;
                    } else if (cloudinary.getResourceType().equals("video")) {
                        type = MediaType.VIDEO;
                    }
                    return PostMedia.builder()
                            .url(cloudinary.getSecureUrl())
                            .type(type)
                            .cloudinaryPublicId(cloudinary.getPublicId())
                            .post(Post.builder().id(postId).build()).isDeleted(false).build();
                } catch (IOException e) {
                    throw new UploadFileException("Failed to upload file: " + file.getOriginalFilename(), e);
                }
            }));
        }

        List<PostMedia> postMediaList = new ArrayList<>();
        // Collect the results of the futures.
        for (Future<PostMedia> future : futures) {
            try {
                postMediaList.add(future.get());
            } catch (CancellationException | InterruptedException | ExecutionException | UploadFileException e) {
                Thread.currentThread().interrupt();
                throw new UploadFileException("Failed to upload file", e);
            }
        }

        // Shut down the executor service.
        executor.shutdown();
        // save media to database
        return postMediaRepository.saveAllAndFlush(postMediaList);
    }

    public void removeMedia(List<UUID> removeMedia) {
        List<PostMedia> postMedia = postMediaRepository.findAllById(removeMedia);
        postMediaRepository.deleteAllById(removeMedia);
        uploader.deleteFiles(postMedia.stream().map(PostMedia::getCloudinaryPublicId).toList());
    }

    @Transactional
    public List<PostMedia> savePostMedia(List<CloudinaryMedia> media, UUID postId) {
        List<PostMedia> postMediaList = new ArrayList<>();
        for (CloudinaryMedia file : media) {
            MediaType type = null;
            if (file.getResourceType().equals("image")) {
                type = MediaType.IMAGE;
            } else if (file.getResourceType().equals("video")) {
                type = MediaType.VIDEO;
            }
            postMediaList.add(PostMedia.builder()
                    .url(file.getUrl())
                    .type(type)
                    .cloudinaryPublicId(file.getPublicId())
                    .post(Post.builder().id(postId).build()).isDeleted(false).build());
        }
        // save media to database
        return postMediaRepository.saveAllAndFlush(postMediaList);
    }
}
