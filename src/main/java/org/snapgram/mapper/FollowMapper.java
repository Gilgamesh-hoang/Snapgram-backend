package org.snapgram.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.snapgram.dto.AffinityDTO;
import org.snapgram.entity.database.Follow;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FollowMapper {
    @Mapping(source = "follower.id", target = "followerId")
    @Mapping(source = "followee.id", target = "followeeId")
    @Mapping(source = "closeness", target = "closeness")
    AffinityDTO toAffinity(Follow follow);

    List<AffinityDTO> toAffinities(List<Follow> follows);
}

