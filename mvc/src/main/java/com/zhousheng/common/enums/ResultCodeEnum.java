package com.zhousheng.common.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200,"操作成功"),
    FAIL(500,"操作失败"),
    PARAM_ERROR(400,"参数错误"),
    UNAUTHORIZED(401,"认证失败"),
    NOT_FOUND(404,"资源不存在");

    private final Integer code;
    private final String message;
    ResultCodeEnum(Integer code,String message){
        this.code = code;
        this.message = message;
    }
}
