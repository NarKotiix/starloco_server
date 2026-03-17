package com.singularsys.jep;

public class JepException extends Exception {
    public JepException(String message, Throwable cause) {
        super(message, cause);
    }

    public JepException(String message) {
        super(message);
    }
}

