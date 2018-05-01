package com.semisonfire.cloudgallery.data.remote.exceptions;

public class InternetUnavailableException extends ServerException {
    public InternetUnavailableException() {
        super("Turn on the Internet to download/upload files.");
    }
}
