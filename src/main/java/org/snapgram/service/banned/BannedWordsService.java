package org.snapgram.service.banned;


import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BannedWordsService {
    AhoCorasickDoubleArrayTrie<String> ahoCorasickTrie;

    public BannedWordsService() {
        ahoCorasickTrie = new AhoCorasickDoubleArrayTrie<>();
    }

    @PostConstruct
    private void loadBannedWords() {
        try {
            ClassPathResource resource = new ClassPathResource("banned_words.txt");
            List<String> bannedWordsList = Files.readAllLines(resource.getFile().toPath());

            Map<String, String> map = new HashMap<>();
            for (String word : bannedWordsList) {
                map.put(word.toLowerCase(), "");  // Đưa từ cấm vào map
            }
            ahoCorasickTrie.build(map);

        } catch (IOException e) {
            log.error("Error loading banned words", e);
        }
    }

    public String removeBannedWords(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // List to store the indexes of banned words found in the content
        List<int[]> indexes = new ArrayList<>();

        // Parse the content to find all occurrences of banned words
        List<AhoCorasickDoubleArrayTrie.Hit<String>> hits = ahoCorasickTrie.parseText(content.toLowerCase());

        // Iterate over each hit (banned word found)
        for (AhoCorasickDoubleArrayTrie.Hit<String> hit : hits) {
            int[] index = new int[]{hit.begin, hit.end}; // Store the start and end index of the banned word

            // If the indexes list is empty, add the current index
            if (indexes.isEmpty()) {
                indexes.add(index);
            } else {
                // If the current index overlaps with the last index in the list, merge them
                if (index[0] <= indexes.get(indexes.size() - 1)[1]) {
                    indexes.get(indexes.size() - 1)[1] = Math.max(indexes.get(indexes.size() - 1)[1], index[1]);
                } else {
                    // Otherwise, add the current index to the list
                    indexes.add(index);
                }
            }
        }

        StringBuilder result = new StringBuilder(content);
        // Replace all banned words with "***" starting from the end of the content
        for (int i = indexes.size() - 1; i >= 0; i--) {
            int[] index = indexes.get(i);
            result.replace(index[0], index[1], "***");
        }
        return result.toString();
    }

    public static void main(String[] args) {
        BannedWordsService bannedWordsService = new BannedWordsService();
        bannedWordsService.loadBannedWords();
        bannedWordsService.removeBannedWords("Hello, I VCC is the best CCMNR con hèn mọn");

    }
}