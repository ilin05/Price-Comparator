package com.pricecomparator.utils;

import lombok.Getter;
import lombok.ToString;

public class ApiResult {
    public Integer code;
    public String message;
    @Getter
    public Object payload;

    public ApiResult(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    public ApiResult(Integer code, Object payload) {
        this.code = code;
        this.payload = payload;
    }
    public ApiResult(Integer code, String message, Object payload) {
        this.code = code;
        this.message = message;
        this.payload = payload;
    }

    public static ApiResult success() {
        return new ApiResult(1, "success", null);
    }

    public static ApiResult success(Object payload) {
        return new ApiResult(1, "success", payload);
    }
    public static ApiResult failure(String message) {
        return new ApiResult(0, message, null);
    }
    public static ApiResult failure(Integer code,String message) {
        return new ApiResult(code, message, null);
    }
}
