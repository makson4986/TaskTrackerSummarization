package org.makson.tasktrackersummarization;

import lombok.Getter;
import org.makson.tasktrackersummarization.exceptions.PromptLoadException;
import org.makson.tasktrackersummarization.exceptions.PromptNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Getter
@Component
public class PromptLoader {
    private final String reportPrompt;

    public PromptLoader(ResourceLoader resourceLoader) {
        Resource prompt = resourceLoader.getResource("classpath:/llm/prompt.txt");
        try {
            reportPrompt = prompt.getContentAsString(StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            throw new PromptNotFoundException("Prompt file not found");
        } catch (IOException e) {
            throw new PromptLoadException("Failed to load the prompt file", e);
        }
    }
}
