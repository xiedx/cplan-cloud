package com.cplan.ai.service;

/**
 * LLM service interface for script generation.
 */
public interface LlmService {

    /**
     * Generate a script based on the outline content.
     *
     * @param outlineContent the user-submitted outline text
     * @param llmModel       the LLM model to use (null for default)
     * @return the generated script text
     */
    String generateScript(String outlineContent, String llmModel);
}
