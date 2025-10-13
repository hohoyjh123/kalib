package com.yesjnet.gwanak.core

import java.io.IOException

/**
 * Exception, Error... : Throwable  예외처리 정의
 */

/**
 * UI RuntimeException
 */
class NavScreenNotDefiendException(message: String?) : NoSuchElementException(message)


/**
 * Network IOException
 */
class ServerErrorException(override val message: String,
                           override val cause: Throwable) : IOException(message,cause) {
    override fun toString(): String {
        return "$message , $cause"
    }
}

class NeedReloginException(val code: Int,
                           override val message: String?,
                           override val cause: Throwable?) : IOException(message,cause)

class NoConnectivityException(override val message: String?,
                            override val cause: Throwable?) : IOException(message,cause)
