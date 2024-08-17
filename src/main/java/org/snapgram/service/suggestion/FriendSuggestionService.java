package org.snapgram.service.suggestion;

import org.snapgram.dto.response.UserDTO;

import java.util.List;
import java.util.UUID;

public interface FriendSuggestionService {
    List<UserDTO> recommendFriends(UUID userId);
}
