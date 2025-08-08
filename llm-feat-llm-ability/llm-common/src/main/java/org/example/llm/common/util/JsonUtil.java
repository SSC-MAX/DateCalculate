package org.example.llm.common.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.*;

import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * json序列化工具
 */
public final class JsonUtil {

    /**
     * 通用的gson对象
     */
    public static final Gson COMMON_GSON;

    static {
        COMMON_GSON = new GsonBuilder()
                .serializeNulls()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                        Collection<Annotation> allAnnotations = fieldAttributes.getAnnotations();
                        for (Annotation annotation : allAnnotations) {
                            if (annotation instanceof JsonIgnore) {
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> aClass) {
                        return false;
                    }
                })
                .excludeFieldsWithModifiers( Modifier.STATIC )
                .setDateFormat( "yyyy-MM-dd HH:mm:ss" )
                .create();
    }


    /**
     * 创建Gson对象
     * @param ignoreFieldNames 忽略字段名称列表
     * @return Gson对象
     */
    private static Gson createGson(String[] ignoreFieldNames) {
        return COMMON_GSON.newBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                if (ignoreFieldNames != null) {
                    for (String ignoreFieldName : ignoreFieldNames) {
                        if (fieldAttributes.getName().equalsIgnoreCase(ignoreFieldName)) {
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        }).create();
    }

    /**
     * 将对象转换成json字符串
     * @param obj 待转化对象
     * @param ignoreFieldNames 忽略字段名称列表
     * @return json字符串
     */
    public static String toJSonString(Object obj, String... ignoreFieldNames) {
        Gson gson = ignoreFieldNames == null? COMMON_GSON : createGson( ignoreFieldNames );
        return gson.toJson( obj );
    }

    public static byte[] toJSonBytes(Object obj, String... ignoreFieldNames) {
        String jsonString = toJSonString(obj, ignoreFieldNames);
        if (null!=jsonString) {
            return jsonString.getBytes(StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * 转换json字符串回对象
     * @param jsonStr json字符串
     * @param clazz 对象类型
     */
    public static <T> T parse(String jsonStr, Class<T> clazz) {
        return COMMON_GSON.fromJson(jsonStr, clazz);
    }

    /**
     * 转换json字符串回对象
     * @param jsonStr json字符串
     * @param type 类型对象
     */
    public static <T> T parseWrapper(String jsonStr, Type type) {
        Reader stringReader = new StringReader(jsonStr);
        return COMMON_GSON.fromJson(stringReader, type);
    }

    /**
     * <pre>
     * 判断一个对象转换json后是否也就是一个对象
     * <ul>
     *     <li>"123": false</li>
     *     <li>123: false</li>
     *     <li>null: false</li>
     *     <li>new Object: true</li>
     *     <li>new ArrayList(): false</li>
     * </ul>
     * </pre>
     * @param obj 任意对象
     * @return true:转换json后是对象
     */
    public static boolean isJsonObject(Object obj) {
        // 将对象转换为JsonElement
        JsonElement jsonElement = COMMON_GSON.toJsonTree(obj);

        // 判断JsonElement是否为对象
        return jsonElement.isJsonObject();
    }
}
