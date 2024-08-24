package org.snapgram.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.snapgram.annotation.media.ValidMedia;
import org.snapgram.annotation.tag.ValidTags;
import org.snapgram.dto.response.PostDTO;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.service.post.IPostService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/posts")
@Validated
public class PostController {
    IPostService postService;

    @GetMapping("/user")
    public ResponseObject<List<PostDTO>> getPostsByUser(@RequestParam("nickname") @NotBlank String nickname,
                                                        @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
                                                        @RequestParam(value = "pageSize", defaultValue = "10") @Min(0) Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        nickname = nickname.trim();
        return new ResponseObject<>(HttpStatus.OK, postService.getPostsByUser(nickname,pageable));
    }

    @PostMapping
    public ResponseObject<Void> createPost(@RequestParam(value = "caption", required = false) @Length(max = 2200) String caption,
                                           @RequestParam(value = "tags", required = false) @Valid @ValidTags List<String> tags,
                                           @RequestParam(value = "media") @Valid @ValidMedia MultipartFile[] media) {


        if (StringUtils.isBlank(caption) && media == null && tags == null) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Caption, media or tags must be provided");
        }

        postService.createPost(caption, media, tags);
        return new ResponseObject<>(HttpStatus.ACCEPTED, "Post is being processed");
    }
}
