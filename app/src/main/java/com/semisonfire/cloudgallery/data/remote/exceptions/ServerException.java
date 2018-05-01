package com.semisonfire.cloudgallery.data.remote.exceptions;

import java.io.IOException;

public class ServerException extends IOException {

    public ServerException() {
        super();
    }

    public ServerException(String message) {
        super(message);
    }
}
