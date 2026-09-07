package com.noom.interview.fullstack.sleep.domain.exception

class DuplicateUsernameException(val username: String) : RuntimeException("Username '$username' is already taken")
