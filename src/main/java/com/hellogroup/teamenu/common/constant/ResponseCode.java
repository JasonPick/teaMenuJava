package com.hellogroup.teamenu.common.constant;

import lombok.Getter;

/**
 * 响应码枚举
 * 
 * @author HelloGroup
 */
@Getter
public enum ResponseCode {
    
    /**
     * 成功
     */
    SUCCESS("0000", "操作成功"),
    
    /**
     * 系统错误
     */
    SYSTEM_ERROR("9999", "系统错误"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR("1001", "参数错误"),
    
    /**
     * 资源不存在
     */
    RESOURCE_NOT_FOUND("1002", "资源不存在"),
    
    /**
     * 业务错误
     */
    BUSINESS_ERROR("2001", "业务处理失败"),
    
    /**
     * 外部服务调用失败
     */
    EXTERNAL_SERVICE_ERROR("3001", "外部服务调用失败");
    
    private final String code;
    private final String message;
    
    ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
