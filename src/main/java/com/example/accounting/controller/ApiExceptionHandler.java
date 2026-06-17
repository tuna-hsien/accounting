package com.example.accounting.controller;

import com.example.accounting.exception.GeminiApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 集中處理 Controller 拋出的例外，避免前端收到沒有意義的 500 + 空 body。
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(GeminiApiException.class)
    public ResponseEntity<Map<String, String>> handleGeminiError(GeminiApiException e) {
        // Gemini 呼叫失敗或解析失敗，視為上游服務異常（502），而不是我方伺服器的程式錯誤
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "AI 解析記帳內容失敗，請稍後再試或換個說法輸入"));
    }
}
