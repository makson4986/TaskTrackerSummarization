package org.makson.tasktrackersummarization.exceptions;

public class PromptNotFoundException extends RuntimeException {
    public PromptNotFoundException(String message) {
        super(message);
    }

    public PromptNotFoundException() {
        super();
    }

    public PromptNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PromptNotFoundException(Throwable cause) {
        super(cause);
    }
}
