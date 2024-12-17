package org.snapgram.service.tag;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.snapgram.entity.database.post.Tag;
import org.snapgram.repository.database.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagService implements ITagService {
    TagRepository tagRepository;

    @Override
    @Transactional
    public List<Tag> saveAll(List<String> tags) {
        List<Tag> tagEntities = new ArrayList<>();
        List<Tag> tagExists = new ArrayList<>();
        for (String tagName : tags) {
            // Check if the tag exists by name
            Tag tag = tagRepository.findByName(tagName);
            if (tag != null) {
                tagExists.add(tag);
            }else {
                tagEntities.add(new Tag(tagName));
            }
        }
        // save tags to database
        List<Tag> savedTags = tagRepository.saveAllAndFlush(tagEntities);
        savedTags.addAll(tagExists);
        return savedTags;
    }
}
