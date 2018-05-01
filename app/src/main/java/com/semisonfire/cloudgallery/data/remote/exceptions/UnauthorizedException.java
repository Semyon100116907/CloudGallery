package com.semisonfire.cloudgallery.data.remote.exceptions;

public class UnauthorizedException extends HttpException {
    public UnauthorizedException(int code, String message) {
        super(code, message);
    }
}
