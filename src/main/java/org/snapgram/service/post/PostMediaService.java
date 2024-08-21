package org.snapgram.service.post;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostMediaService {
    MediaUploader uploader;
    PostMediaRepository postMediaRepository;

    @Transactional
    public List<PostMedia> savePostMedia(MultipartFile[] media, Post post) {
        // Create an ExecutorService with a fixed thread pool.
        ExecutorService executor = Executors.newFixedThreadPool(4);
        // Create a list to hold Future objects.
        List<Future<PostMedia>> futures = new ArrayList<>();

        for (MultipartFile file : media) {
            // Submit the task for execution and add its Future to the list.
            futures.add(executor.submit(() -> {

                String contentType = file.getContentType();
                MediaType type = contentType.startsWith("image") ? MediaType.IMAGE : MediaType.VIDEO;
                String url = null;

                try {
                    url = uploader.uploadFile(file);
                } catch (IOException e) {
                    throw new UploadFileException("Failed to upload file: " + file.getOriginalFilename(), e);
                }

                return PostMedia.builder().url(url).type(type).post(post).isDeleted(false).build();

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
}
