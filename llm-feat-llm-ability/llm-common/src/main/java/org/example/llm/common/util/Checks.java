package org.example.llm.common.util;

import java.util.Collection;
import java.util.Map;

/**
 * 校验工具
 * <br/>--------------------------
 * @author zybi
 */
public class Checks {

    private Checks() {}


    /**
     * ==================================================================
     * 判断对象是否为空
     * ==================================================================
     */

    public static boolean isNull(Object obj) {
        boolean nullObject = obj == null;
        if ( nullObject) {
            return true;
        } else {
            if ( obj instanceof String) {
                return isNull((String) obj);
            } else if (obj instanceof Collection) {
                return isNull((Collection<?>) obj);
            } else if (obj instanceof Map) {
                return isNull((Map<?, ?>) obj);
            }
            return false;
        }
    }

    public static boolean isNull(String str) {
        return str == null || str.isEmpty() || str.trim().isEmpty();
    }

    public static boolean isNull(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    public static boolean isNull(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean anyNull(Object... objects) {
        if ( isNull( objects)) {
            return true;
        }
        for (Object object : objects) {
            if ( isNull( object)) {
                return true;
            }
        }
        return false;
    }



    /**
     * ==================================================================
     * 判断对象是否非空
     * ==================================================================
     */

    public static boolean noNull(Object obj) {
        return !isNull(obj);
    }

    public static boolean noNull(String str) {
        return !isNull(str);
    }

    public static boolean noNull(Collection<?> coll) {
        return !isNull(coll);
    }

    public static boolean noNull(Map<?, ?> map) {
        return !isNull(map);
    }

    public static boolean noNull(Object obj, Object... objects) {
        return !anyNull(obj, objects);
    }


}
