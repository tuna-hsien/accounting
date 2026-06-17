package com.example.accounting.exception;

/**
 * 當呼叫 Gemini API 失敗，或回應內容無法解析成預期的 JSON 結構時拋出。
 */
public class GeminiApiException extends RuntimeException {

    public GeminiApiException(String message) {
        super(message);
    }

    public GeminiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
