package com.semisonfire.cloudgallery.core.data.remote.exceptions

import java.io.IOException

open class ServerException : IOException {
    constructor() : super()
    constructor(message: String) : super(message)
}

open class HttpException(private val code: Int, override val message: String) : ServerException() {
    override fun toString(): String {
        return "HttpException [code=$code, response message=$message]"
    }
}

class UnauthorizedException(code: Int, message: String) : HttpException(code, message)

class InternetUnavailableException :
    ServerException("Turn on the Internet to download/upload files.")