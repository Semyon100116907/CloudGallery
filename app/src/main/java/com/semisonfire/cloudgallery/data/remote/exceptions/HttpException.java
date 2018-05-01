package com.semisonfire.cloudgallery.data.remote.exceptions;

public class HttpException extends ServerException {

    private int code;
    private String message;

    public HttpException(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "HttpException [code=" + code + ", response message=" + message +']';
    }
}
