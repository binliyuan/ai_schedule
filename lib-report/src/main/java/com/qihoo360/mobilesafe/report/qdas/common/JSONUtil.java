package com.qihoo360.mobilesafe.report.qdas.common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Created by lichongbo on 2015/9/28. */
public class JSONUtil {
    public static final String TAG = "JSONUtil";

    public static JSONArray getArray(JSONObject object, String key) throws JSONException {
        JSONArray array = null;
        if (object.has(key)) {
            array = object.optJSONArray(key);
        } else {
            array = new JSONArray();
            object.put(key, array);
        }
        return array;
    }

    public static boolean addArrayItem(JSONObject object, String key, JSONObject item) {
        try {
            JSONArray array = getArray(object, key);
            array.put(item);
            return true;
        } catch (JSONException e) {
        }
        return false;
    }

    public static boolean setData(JSONObject object, String key, Object value) {
        try {
            if (object == null) return false;
            object.put(key, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取JSON中ext的指定Key的String值
     *
     * @param object json对象
     * @param key ext中的key
     * @param defaultValue 取不到时的默认值
     * @return 如果存在则返回该值，取不到则返回defaultValue
     */
    public static String getStringExtData(JSONObject object, String key, String defaultValue) {
        try {
            if (object == null) return defaultValue;
            JSONObject ext = null;
            if (object.has("ext")) {
                ext = object.optJSONObject("ext");
                return getString(ext, key, defaultValue);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean setExtData(JSONObject object, String key, Object value) {
        try {
            if (object == null) return false;
            JSONObject ext = null;
            if (object.has("ext")) {
                ext = object.optJSONObject("ext");
            } else {
                ext = new JSONObject();
                object.put("ext", ext);
            }
            return setData(ext, key, value);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置JSON中，指定第一级中的ext信息
     *
     * @param object 要修改的JSON
     * @param type 第一级的key名称
     * @param key Ext中的Key名称
     * @param value Ext中要设置的Value
     * @return 是否更新成功
     */
    public static boolean setExtData(JSONObject object, String type, String key, Object value) {
        try {
            if (object == null) return false;
            if (object.has(type)) {
                return setExtData(object.optJSONObject(type), key, value);
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * 获取JSON对象中字符串类型的值
     *
     * @param json JSON对象
     * @param key 要取的Key
     * @param defaultValue 如果没有则返回的默认值
     * @return 取到的数据或默认值
     */
    public static String getString(JSONObject json, String key, String defaultValue) {
        if (json == null) return defaultValue;
        try {
            if (json.has(key)) return json.getString(key);
        } catch (Exception e) {
            CommonUtil.printError(TAG, "", e);
        }
        return defaultValue;
    }
    /**
     * 获取JSON对象中长整型类型的值
     *
     * @param json JSON对象
     * @param key 要取的Key
     * @param defaultValue 如果没有则返回的默认值
     * @return 取到的数据或默认值
     */
    public static Long getLong(JSONObject json, String key, Long defaultValue) {
        if (json == null) return defaultValue;
        try {
            if (json.has(key)) return json.getLong(key);
        } catch (Exception e) {
            CommonUtil.printError(TAG, "", e);
        }
        return defaultValue;
    }
}
