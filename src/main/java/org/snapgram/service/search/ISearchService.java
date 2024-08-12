package org.snapgram.service.search;

import org.snapgram.dto.response.UserDTO;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface ISearchService {
    Set<UserDTO> searchByKeyword(String keyword, Pageable page);
}
