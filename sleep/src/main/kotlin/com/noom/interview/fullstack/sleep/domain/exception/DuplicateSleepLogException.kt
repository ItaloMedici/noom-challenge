package com.noom.interview.fullstack.sleep.domain.exception

import java.time.LocalDate

class DuplicateSleepLogException(val sleepDate: LocalDate) :
    RuntimeException("Sleep log already exists on date $sleepDate for this user")
