package org.snapgram.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.dto.CustomUserSecurity;
import org.snapgram.dto.response.ResponseObject;
import org.snapgram.dto.response.UserDTO;
import org.snapgram.service.follow.IFollowService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("${API_PREFIX}/users")
@Validated
public class FollowController {
    IFollowService followService;

    @PostMapping("/filter-following")
    public ResponseObject<List<UUID>> checkFollowing(@RequestBody @NotNull List<UUID> userIds,
                                                     @AuthenticationPrincipal CustomUserSecurity user) {
        if (userIds.isEmpty()) {
            return new ResponseObject<>(HttpStatus.OK, List.of());
        }
        // Call the service to get the list of users that currentUserId is following
        List<UUID> followingIds = followService.getFollowedUserIds(user.getId(), userIds);
        return new ResponseObject<>(HttpStatus.OK, followingIds);
    }

    @PostMapping("/{userId}/follow")
    public ResponseObject<Void> followAnotherUser(
            @AuthenticationPrincipal CustomUserSecurity user,
            @PathVariable("userId") @NotNull UUID userId) {
        if (user.getId().equals(userId)) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "You cannot follow yourself");
        }
        followService.followUser(user.getId(), userId);
        return new ResponseObject<>(HttpStatus.OK);
    }

    @DeleteMapping("/{userId}/unfollow")
    public ResponseObject<Void> unfollowAnotherUser(
            @AuthenticationPrincipal CustomUserSecurity user,
            @PathVariable("userId") @NotNull UUID userId) {
        if (user.getId().equals(userId)) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "You cannot unfollow yourself");
        }
        followService.unfollowUser(user.getId(), userId);
        return new ResponseObject<>(HttpStatus.OK);
    }

    @DeleteMapping("/{followerId}/remove-follower")
    public ResponseObject<Void> removeFollower(@AuthenticationPrincipal CustomUserSecurity user,
                                               @PathVariable("followerId") @NotNull UUID followerId) {
        if (user.getId().equals(followerId)) {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "You cannot remove yourself");
        }
        followService.removeFollower(user.getId(), followerId);
        return new ResponseObject<>(HttpStatus.OK);
    }

    @GetMapping("/{userId}/followers")
    public ResponseObject<List<UserDTO>> getFollowers(
            @PathVariable("userId") @NotNull UUID userId,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "15") @Min(0) Integer pageSize
    ) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize).withSort(sort);
        List<UserDTO> res = followService.getFollowersByUser(userId, pageable);
        return new ResponseObject<>(HttpStatus.OK, res);
    }

    @GetMapping("/{userId}/following")
    public ResponseObject<List<UserDTO>> getFollowing(
            @PathVariable("userId") @NotNull UUID userId,
            @RequestParam(value = "pageNum", defaultValue = "1") @Min(0) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "15") @Min(0) Integer pageSize
    ) {
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize).withSort(sort);
        List<UserDTO> res = followService.getFollowingByUser(userId, pageable);
        return new ResponseObject<>(HttpStatus.OK, res);
    }
}
