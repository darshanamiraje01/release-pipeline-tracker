package com.darshana.pipeline.exception;

public class InvalidStageTransitionException extends RuntimeException {
    public InvalidStageTransitionException(String message) {
        super(message);
    }
}
