package com.github.moriakira.jibundashboard.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.GONE)
class GoneException(message: String) : RuntimeException(message)
