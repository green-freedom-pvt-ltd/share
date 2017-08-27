package com.sharesmile.share.rfac.models;

/**
 * Created by ankitmaheshwari on 8/26/17.
 */

public class FeedbackNode {

    public static final int LEVEL_1 = 1;
    public static final int LEVEL_2 = 2;
    public static final int LEVEL_3 = 4;

    private int level;
    private String value;
    private Type type;

    public FeedbackNode(int level, String value, Type type) {
        this.level = level;
        this.value = value;
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeedbackNode that = (FeedbackNode) o;

        if (level != that.level) return false;
        if (!value.equals(that.value)) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = level;
        result = 31 * result + value.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    public enum Type{
        CATEGORY,
        QNA,
        RESOLUTION;
    }
}
