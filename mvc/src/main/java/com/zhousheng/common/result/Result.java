package com.zhousheng.common.result;

import com.zhousheng.common.enums.ResultCodeEnum;
import lombok.Data;

@Data
public class Result<T>{
    private Integer code;
    private String message;
    private T data;
//    构造私有函数防止被直接创建对象
    private Result(){}
    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setCode(ResultCodeEnum.SUCCESS.getCode());
        result.setMessage(ResultCodeEnum.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }
    public static <T> Result<T> success(){
        return success(null);
    }
    public static <T> Result<T> fail(ResultCodeEnum resultCodeEnum){
        Result<T> result = new Result<>();
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }
//    自定义fail
    public static <T> Result<T> fail(Integer code,String msg){
        Result<T> result = new Result<>();
        result.setMessage(msg);
        result.setCode(code);
        return result;
    }
}
