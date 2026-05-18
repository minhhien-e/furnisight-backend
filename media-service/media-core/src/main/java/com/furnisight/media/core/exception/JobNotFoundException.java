package com.furnisight.media.core.exception;

import com.furnisight.media.core.exception.enums.ErrorCode;

import java.util.Map;

public class JobNotFoundException extends BaseException {
    public JobNotFoundException(String jobId) {
        super(ErrorCode.JOB_NOT_FOUND, "Processing job not found for media asset: " + jobId, Map.of("jobId", jobId));
    }
}
