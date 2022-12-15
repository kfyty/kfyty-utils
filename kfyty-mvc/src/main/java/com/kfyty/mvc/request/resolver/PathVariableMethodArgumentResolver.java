package com.kfyty.mvc.request.resolver;

import com.kfyty.core.method.MethodParameter;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.mvc.annotation.bind.PathVariable;
import com.kfyty.mvc.mapping.MethodMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.kfyty.core.utils.AnnotationUtil.findAnnotation;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 10:25
 * @email kfyty725@hotmail.com
 */
public class PathVariableMethodArgumentResolver extends AbstractHandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return AnnotationUtil.hasAnnotation(parameter.getParameter(), PathVariable.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, MethodMapping mapping, HttpServletRequest request) throws IOException {
        List<String> paths = CommonUtil.split(request.getRequestURI(), "[/]");
        Map<String, Integer> restfulURLMappingIndex = mapping.getRestfulURLMappingIndex();
        String paramName = parameter.getParameterName(findAnnotation(parameter.getParameter(), PathVariable.class), PathVariable::value);
        Integer paramIndex = restfulURLMappingIndex.get(paramName);
        return this.createDataBinder(paramName, paths.get(paramIndex)).getPropertyContext().getProperty(paramName, parameter.getParameterGeneric());
    }
}
