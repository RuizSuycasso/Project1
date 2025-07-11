// File: AdviceResponse.java
package com.example.myapplication; // Đảm bảo package name này đúng

import com.google.gson.annotations.SerializedName;

public class AdviceResponse {

    @SerializedName("advice")
    private String advice;

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }
}