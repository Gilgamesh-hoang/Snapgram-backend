package org.snapgram.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.snapgram.dto.request.PostRequest;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.service.post.IPostService;
import org.snapgram.validation.media.ValidMedia;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/posts")
@Validated
public class PostController {
    IPostService postService;
    ObjectMapper objectMapper;

    @GetMapping("/{id}")
    public ResponseObject<PostDTO> getPostsById(@PathVariable("id") @NotNull UUID id) {
        PostDTO post = postService.getPostById(id);
        return new ResponseObject<>(HttpStatus.OK, post);
    }

    @GetMapping("/user")
    public ResponseObject<List<PostDTO>> getPostsByUser(@RequestParam("nickname") @NotBlank String nickname,
                                                        @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
                                                        @RequestParam(value = "pageSize", defaultValue = "10") @Min(0) @Max(50) Integer pageSize) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
        nickname = nickname.trim();
        return new ResponseObject<>(HttpStatus.OK, postService.getPostsByUser(nickname, pageable));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseObject<Void> createPost(@RequestBody @Valid PostRequest request) {

        if (StringUtils.isBlank(request.getCaption()) && request.getMedia() == null) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Caption or media  must be provided");
        }

        postService.createPost(request);
        return new ResponseObject<>(HttpStatus.ACCEPTED, "Post is being processed");
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseObject<Void> createPost(
            @RequestPart("post") @NotBlank String postJson,
            @RequestPart("media") @Valid @ValidMedia MultipartFile[] media) throws JsonProcessingException {

        PostRequest request = objectMapper.readValue(postJson, PostRequest.class);
        if (StringUtils.isBlank(request.getCaption()) && media == null) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Caption or media  must be provided");
        }

        postService.createPost(request, media);
        return new ResponseObject<>(HttpStatus.ACCEPTED, "Post is being processed");
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseObject<PostDTO> updatePost(@RequestBody @Valid PostRequest request) {
        if (request.getId() == null) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Post id must be provided");
        }
        PostDTO response = postService.updatePost(request);
        return new ResponseObject<>(HttpStatus.OK, response);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseObject<PostDTO> updatePost(
            @RequestPart("post") @NotBlank String postJson,
            @RequestPart(value = "media", required = false) @Valid @ValidMedia MultipartFile[] media) throws JsonProcessingException {

        PostRequest request = objectMapper.readValue(postJson, PostRequest.class);
        if (request.getId() == null) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Post id must be provided");
        }
        PostDTO response = postService.updatePost(request, media);
        return new ResponseObject<>(HttpStatus.OK, response);
    }
}
