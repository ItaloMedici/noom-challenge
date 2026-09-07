package com.noom.interview.fullstack.sleep.domain.exception

class InvalidDateRangeException() :
    RuntimeException("End date cannot be before start date")
