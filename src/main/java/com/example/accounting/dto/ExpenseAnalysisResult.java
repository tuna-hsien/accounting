package com.example.accounting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 對應 Gemini API 回傳的記帳分析結果。
 * 欄位名稱與 JSON Schema 中要求的 key 完全一致：amount, category, advice。
 */
public class ExpenseAnalysisResult {

    @JsonProperty("amount")
    private Integer amount;

    @JsonProperty("category")
    private String category;

    @JsonProperty("advice")
    private String advice;

    public ExpenseAnalysisResult() {
    }

    public ExpenseAnalysisResult(Integer amount, String category, String advice) {
        this.amount = amount;
        this.category = category;
        this.advice = advice;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    @Override
    public String toString() {
        return "ExpenseAnalysisResult{" +
                "amount=" + amount +
                ", category='" + category + '\'' +
                ", advice='" + advice + '\'' +
                '}';
    }
}
