package dev.nokee.core.exec;

public class ExecException extends RuntimeException {
    public ExecException(String message) {
        super(message);
    }

    public ExecException(String message, Throwable cause) {
        super(message, cause);
    }
}
