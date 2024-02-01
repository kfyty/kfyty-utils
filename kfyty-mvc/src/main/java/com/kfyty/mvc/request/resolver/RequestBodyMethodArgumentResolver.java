package com.kfyty.mvc.request.resolver;

import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.JsonUtil;
import com.kfyty.mvc.annotation.bind.RequestBody;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.util.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class RequestBodyMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), RequestBody.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        if (String.class.isAssignableFrom(parameter.getParamType())) {
            return ServletUtil.getRequestBody(request);
        }
        return JsonUtil.toObject(ServletUtil.getRequestBody(request), parameter.getParamType());
    }
}
