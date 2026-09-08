package com.noom.interview.fullstack.sleep.domain.exception

import java.time.LocalDate
import java.util.UUID

class DuplicateSleepLogException(val userId: UUID, val sleepDate: LocalDate) :
    RuntimeException("Sleep log already exists for user '$userId' on '$sleepDate'")
