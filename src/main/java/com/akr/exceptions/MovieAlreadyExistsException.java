package com.akr.exceptions;

public class MovieAlreadyExistsException extends RuntimeException {
        public MovieAlreadyExistsException(String message) {
            super(message);
    }
}
