package org.makson.tasktrackersummarization.exceptions;

public class PromptLoadException extends RuntimeException {
    public PromptLoadException(String message) {
        super(message);
    }

    public PromptLoadException() {
        super();
    }

    public PromptLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public PromptLoadException(Throwable cause) {
        super(cause);
    }
}
