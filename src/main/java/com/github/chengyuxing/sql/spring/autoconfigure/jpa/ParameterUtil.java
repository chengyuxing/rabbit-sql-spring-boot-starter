package com.github.chengyuxing.sql.spring.autoconfigure.jpa;

import com.github.chengyuxing.common.tuple.Pair;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 参数工具
 */
public class ParameterUtil {
    /**
     * 构建参数
     *
     * @param method 方法
     * @param args   参数值组
     * @return 参数字典
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> buildArgs(MethodSignature method, Object[] args) {
        String[] names = method.getParameterNames();
        if (args.length == 1 && args[0] instanceof Map) {
            return (Map<String, Object>) args[0];
        }
        if (args.length >= 1) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < names.length; i++) {
                map.put(names[i], args[i]);
            }
            return map;
        }
        return Collections.emptyMap();
    }

    /**
     * 解析类型和类型的泛型为字符串类名
     *
     * @param type 类型
     * @return 类型名和类型的泛型名
     */
    public static Pair<String, String> resolveGenericIfNecessary(Type type) {
        String typeName = type.getTypeName();
        String genericTypeName = "";

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            typeName = parameterizedType.getRawType().getTypeName();
            Type genericType = parameterizedType.getActualTypeArguments()[0];
            genericTypeName = genericType.getTypeName();
            if (genericType instanceof ParameterizedType) {
                genericTypeName = ((ParameterizedType) genericType).getRawType().getTypeName();
            }
        }
        return Pair.of(typeName, genericTypeName);
    }
}
