package org.snapgram.service.sentiment;

import jakarta.validation.constraints.NotBlank;
import org.snapgram.enums.SentimentType;

public interface ISentimentService {
    /**
     * Analyzes the sentiment of the given text.
     *
     * @param text the text to analyze, must not be blank
     * @return the sentiment type of the analyzed text
     */
    SentimentType analyzeSentiment(@NotBlank String text);
}
