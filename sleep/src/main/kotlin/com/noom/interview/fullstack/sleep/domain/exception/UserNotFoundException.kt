package com.noom.interview.fullstack.sleep.domain.exception

import java.util.*

class UserNotFoundException(val userId: UUID) :
    RuntimeException("User with ID '\$userId' was not found")