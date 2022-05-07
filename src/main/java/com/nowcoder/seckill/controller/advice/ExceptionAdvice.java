package com.nowcoder.seckill.controller.advice;

import com.nowcoder.seckill.common.BusinessException;
import com.nowcoder.seckill.common.ErrorCode;
import com.nowcoder.seckill.common.ResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionAdvice implements ErrorCode {

    private Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler
    @ResponseBody
    public ResponseModel handleException(Exception e) {
        Map<Object, Object> data = new HashMap<>();
        if (e instanceof BusinessException) {
            data.put("code", ((BusinessException) e).getCode());
            data.put("message", ((BusinessException) e).getMessage());
        } else if (e instanceof NoHandlerFoundException) {
            data.put("code", UNDEFINED_ERROR);
            data.put("message", "该资源不存在！");
        } else {
            data.put("code", UNDEFINED_ERROR);
            data.put("message", "发生未知错误！");
            // 记录日志
            logger.error("发生未知错误", e);
        }

        return new ResponseModel(ResponseModel.STATUS_FAILURE, data);
    }

}
