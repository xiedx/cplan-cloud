package com.cplan.common.exception;

import com.cplan.common.result.ResultCode;

/**
 * Business exception thrown by service layers.
 * Carries a ResultCode for structured error handling.
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BizException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
