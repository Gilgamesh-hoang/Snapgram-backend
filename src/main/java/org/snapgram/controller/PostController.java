package org.snapgram.controller;

import jakarta.validation.Valid;
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
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/posts")
@Validated
public class PostController {
    IPostService postService;

    @PostMapping
    public ResponseObject<PostDTO> createPost(@RequestParam(value = "caption", required = false) @Length(max = 2200) String caption,
                                              @RequestParam(value = "tags", required = false) @Valid @ValidTags List<String> tags,
                                              @RequestParam(value = "media", required = false) @Valid @ValidMedia MultipartFile[] media) {


        if (StringUtils.isBlank(caption) && media == null && tags == null) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Caption, media or tags must be provided");
        }

        PostDTO response = postService.createPost(caption, media,tags);
        return new ResponseObject<>(HttpStatus.CREATED, "Create post successfully", response);
    }
}
