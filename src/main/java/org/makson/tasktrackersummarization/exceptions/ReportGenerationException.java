package org.makson.tasktrackersummarization.exceptions;

public class ReportGenerationException extends RuntimeException {
    public ReportGenerationException(String message) {
        super(message);
    }

    public ReportGenerationException() {
        super();
    }

    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportGenerationException(Throwable cause) {
        super(cause);
    }
}
