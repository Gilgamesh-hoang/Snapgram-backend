package org.snapgram.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.service.search.ISearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/search")
@Validated
public class SearchController {
    ISearchService searchService;

    @GetMapping("/users/followers")
    public ResponseObject<Set<UserDTO>> searchFollowers(
            @RequestParam("userId") @NotNull UUID userId,
            @RequestParam("keyword") @NotBlank String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(0) Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        keyword = keyword.trim().toLowerCase();
        Set<UserDTO> users = searchService.searchFollowersByUser(userId, keyword, pageable);
        return new ResponseObject<>(HttpStatus.OK, "Users found", users);
    }
    @GetMapping("/users/following")
    public ResponseObject<Set<UserDTO>> searchFollowing(
            @RequestParam("userId") @NotNull UUID userId,
            @RequestParam("keyword") @NotBlank String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(0) Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        keyword = keyword.trim().toLowerCase();
        Set<UserDTO> users = searchService.searchFollowingByUser(userId, keyword, pageable);
        return new ResponseObject<>(HttpStatus.OK, "Users found", users);
    }

    @GetMapping("/users")
    public ResponseObject<Set<UserDTO>> searchUser(@RequestParam("keyword") @NotBlank String keyword,
                                                   @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
                                                   @RequestParam(value = "pageSize", defaultValue = "10") @Min(0) Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        keyword = keyword.trim().toLowerCase();
        Set<UserDTO> users = searchService.searchByKeyword(keyword, pageable);
        return new ResponseObject<>(HttpStatus.OK, "Users found", users);
    }
}
