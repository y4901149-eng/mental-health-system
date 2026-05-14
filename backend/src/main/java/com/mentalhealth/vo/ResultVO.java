package com.mentalhealth.vo;

import lombok.Data;

/**
 * 统一响应结果类
 * 作用：所有接口统一返回格式，前端根据 code 判断请求是否成功
 * code: 200=成功, 400=参数错误, 401=未登录, 500=服务器异常
 */
@Data
public class ResultVO<T> {
    /** 状态码 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 返回数据 */
    private T data;

    private ResultVO() {}

    /** 成功 - 带数据 */
    public static <T> ResultVO<T> success(T data) {
        ResultVO<T> result = new ResultVO<>();
        result.code = 200;
        result.message = "操作成功";
        result.data = data;
        return result;
    }

    /** 成功 - 无数据 */
    public static <T> ResultVO<T> success() {
        return success(null);
    }

    /** 失败 - 自定义消息 */
    public static <T> ResultVO<T> error(Integer code, String message) {
        ResultVO<T> result = new ResultVO<>();
        result.code = code;
        result.message = message;
        return result;
    }

    /** 参数错误 */
    public static <T> ResultVO<T> badRequest(String message) {
        return error(400, message);
    }

    /** 未授权 */
    public static <T> ResultVO<T> unauthorized(String message) {
        return error(401, message);
    }

    /** 服务器异常 */
    public static <T> ResultVO<T> serverError() {
        return error(500, "服务器内部异常");
    }
}
