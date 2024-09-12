package org.snapgram.service.search;

import org.snapgram.dto.response.UserDTO;
import org.springframework.data.domain.Pageable;

import java.util.Set;
import java.util.UUID;

public interface ISearchService {
    Set<UserDTO> searchByKeyword(String keyword, Pageable page);

    Set<UserDTO> searchFollowersByUser(UUID userId, String keyword, Pageable pageable);

    Set<UserDTO> searchFollowingByUser(UUID userId, String keyword, Pageable pageable);
}
