package com.yzd.common;

import java.util.HashMap;

/**
 * @Author: yaozh
 * @Description:HeaderHashMap忽略key的大小写敏感
 */
public class HeaderHashMap<K, V> extends HashMap {

    public HeaderHashMap() {
        super();
    }

    public HeaderHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public Object put(Object key, Object value) {
        if (key instanceof String) {
            key = ((String) key).toLowerCase();
        }
        return super.put(key, value);
    }

    @Override
    public Object get(Object key) {
        if (key instanceof String) {
            key = ((String) key).toLowerCase();
        }
        return super.get(key);
    }
}
