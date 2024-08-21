package org.snapgram.service.tag;

import org.snapgram.entity.database.Tag;

import java.util.List;

public interface ITagService {
    List<Tag> saveAll(List<String> tags);
}
