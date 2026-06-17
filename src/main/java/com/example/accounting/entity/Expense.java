package com.example.accounting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 記帳紀錄資料表。
 * rawText 為使用者原始輸入，amount/category/aiAdvice 為 Gemini 解析後的結構化結果。
 */
@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "raw_text", nullable = false)
    private String rawText;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Lob
    @Column(name = "ai_advice")
    private String aiAdvice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Expense() {
    }

    public Expense(String rawText, Integer amount, String category, String aiAdvice) {
        this.rawText = rawText;
        this.amount = amount;
        this.category = category;
        this.aiAdvice = aiAdvice;
    }

    /**
     * 在 INSERT 前自動補上建立時間，不需要在外部手動指定，
     * 也不依賴資料庫方言的預設值語法（H2 / SQLite 之間更通用）。
     */
    @jakarta.persistence.PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
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

    public String getAiAdvice() {
        return aiAdvice;
    }

    public void setAiAdvice(String aiAdvice) {
        this.aiAdvice = aiAdvice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
