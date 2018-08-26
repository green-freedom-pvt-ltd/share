package com.sharesmile.share.core;

import com.sharesmile.share.core.base.UnObfuscable;

public class ValueWrapper<T> implements UnObfuscable {
    public final T value;
    public final boolean isDefaultValue;

    public ValueWrapper(T value, boolean isDefaultValue) {
        this.value = value;
        this.isDefaultValue = isDefaultValue;
    }
}
