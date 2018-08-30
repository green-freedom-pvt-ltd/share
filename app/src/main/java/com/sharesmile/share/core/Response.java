package com.sharesmile.share.core;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;

public class Response implements UnObfuscable {
    @SerializedName("code")
    private int code;
    @SerializedName("status")
    private String status;
    @SerializedName("response")
    private JsonElement response;
    @SerializedName("errors")
    private JsonElement errors;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonElement getResponse() {
        return response;
    }

    public void setResponse(JsonElement response) {
        this.response = response;
    }

    public JsonElement getErrors() {
        return errors;
    }

    public void setErrors(JsonElement errors) {
        this.errors = errors;
    }
}
