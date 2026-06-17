package com.example.accounting.service;

import com.example.accounting.dto.ExpenseAnalysisResult;
import com.example.accounting.enums.ExpenseCategory;
import com.example.accounting.exception.GeminiApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 呼叫 Gemini API，將一段自然語言記帳文字（例如「中午跟同事吃拉麵花了 150 元」）
 * 轉換成結構化的記帳資料：金額、分類、理財建議。
 *
 * 核心做法：
 *   - 透過 generationConfig.responseMimeType = "application/json"
 *     以及 responseSchema 強制 Gemini 回傳符合結構的 JSON，
 *     而不是單純在 prompt 文字裡「拜託」AI 回 JSON。
 *   - category 欄位用 enum 限制在固定分類清單內，模型不可能回傳清單外的值。
 */
@Service
public class GeminiExpenseService {

    private static final Logger log = LoggerFactory.getLogger(GeminiExpenseService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    public GeminiExpenseService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 分析一段記帳文字，回傳結構化結果。
     *
     * @param text 使用者輸入的記帳描述，例如「晚餐吃牛肉麵 120 元」
     * @return 解析後的金額、分類、建議
     * @throws GeminiApiException 當 API 呼叫失敗，或回應內容不是預期格式時
     */
    public ExpenseAnalysisResult analyze(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("記帳文字不可為空");
        }

        String url = baseUrl + "/models/" + model + ":generateContent";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        ObjectNode requestBody = buildRequestBody(text);
        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.postForEntity(url, entity, String.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // 4xx/5xx：把 Gemini 回傳的錯誤內容一起記錄下來，方便除錯（例如 API key 錯誤、配額超過等）
            log.error("呼叫 Gemini API 失敗，狀態碼: {}, 回應內容: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new GeminiApiException(
                    "呼叫 Gemini API 失敗，狀態碼: " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            // 連線逾時、DNS 失敗等網路層級問題
            log.error("呼叫 Gemini API 發生網路錯誤", e);
            throw new GeminiApiException("呼叫 Gemini API 發生網路錯誤", e);
        }

        HttpStatusCode statusCode = response.getStatusCode();
        if (!statusCode.is2xxSuccessful() || response.getBody() == null) {
            throw new GeminiApiException("Gemini API 回應異常，狀態碼: " + statusCode);
        }

        return parseResponse(response.getBody());
    }

    /**
     * 組裝請求 body，包含：
     *   - contents: 實際 prompt
     *   - generationConfig.responseMimeType: 強制回傳 application/json
     *   - generationConfig.responseSchema: 嚴格定義回傳的欄位與型別，
     *     category 用 enum 鎖在固定分類清單內
     */
    private ObjectNode buildRequestBody(String text) {
        ObjectNode root = objectMapper.createObjectNode();

        // ---- contents ----
        ArrayNode contents = root.putArray("contents");
        ObjectNode contentItem = contents.addObject();
        ArrayNode parts = contentItem.putArray("parts");
        parts.addObject().put("text", buildPrompt(text));

        // ---- generationConfig ----
        ObjectNode generationConfig = root.putObject("generationConfig");
        generationConfig.put("responseMimeType", "application/json");
        generationConfig.put("temperature", 0.2); // 記帳分類需求穩定輸出，溫度調低

        ObjectNode responseSchema = generationConfig.putObject("responseSchema");
        responseSchema.put("type", "OBJECT");

        ObjectNode properties = responseSchema.putObject("properties");

        ObjectNode amountProp = properties.putObject("amount");
        amountProp.put("type", "INTEGER");
        amountProp.put("description", "支出金額，必須是不含小數點與貨幣符號的整數");

        ObjectNode categoryProp = properties.putObject("category");
        categoryProp.put("type", "STRING");
        ArrayNode categoryEnum = categoryProp.putArray("enum");
        for (String label : ExpenseCategory.labels()) {
            categoryEnum.add(label);
        }
        categoryProp.put("description", "支出分類，必須是給定清單中的其中一項");

        ObjectNode adviceProp = properties.putObject("advice");
        adviceProp.put("type", "STRING");
        adviceProp.put("description", "根據這筆支出給予的簡短理財建議，限 50 字以內");

        ArrayNode required = responseSchema.putArray("required");
        required.add("amount").add("category").add("advice");

        // propertyOrdering 讓輸出順序穩定，方便閱讀與除錯（非必要但建議）
        ArrayNode propertyOrdering = responseSchema.putArray("propertyOrdering");
        propertyOrdering.add("amount").add("category").add("advice");

        return root;
    }

    private String buildPrompt(String text) {
        return """
                你是一個記帳助理。請分析以下使用者輸入的記帳文字，並提取出：
                1. amount：這筆支出的金額（整數，不含貨幣符號）
                2. category：這筆支出屬於哪個分類，只能從給定的分類清單中選擇
                3. advice：針對這筆支出給的簡短理財建議

                可選分類清單：%s

                使用者輸入：「%s」
                """.formatted(String.join("、", ExpenseCategory.labels()), text);
    }

    /**
     * 解析 Gemini API 的回應，取出 candidates[0].content.parts[0].text，
     * 該文字內容即為符合 responseSchema 的 JSON 字串，再反序列化成 ExpenseAnalysisResult。
     */
    private ExpenseAnalysisResult parseResponse(String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new GeminiApiException("Gemini 回應中沒有 candidates，原始回應: " + rawBody);
            }

            // 模型可能因安全性原因（safety filter）回傳沒有 content 的 candidate
            JsonNode firstCandidate = candidates.get(0);
            String finishReason = firstCandidate.path("finishReason").asText("");
            JsonNode partsNode = firstCandidate.path("content").path("parts");
            if (!partsNode.isArray() || partsNode.isEmpty()) {
                throw new GeminiApiException(
                        "Gemini 回應沒有有效內容，finishReason: " + finishReason);
            }

            String jsonText = partsNode.get(0).path("text").asText(null);
            if (jsonText == null || jsonText.isBlank()) {
                throw new GeminiApiException("Gemini 回應的 text 欄位為空");
            }

            ExpenseAnalysisResult result =
                    objectMapper.readValue(jsonText, ExpenseAnalysisResult.class);

            validate(result);
            return result;

        } catch (GeminiApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析 Gemini 回應失敗，原始回應: {}", rawBody, e);
            throw new GeminiApiException("解析 Gemini 回應失敗", e);
        }
    }

    /**
     * 雖然 responseSchema 已經在模型端做了限制，但仍對關鍵欄位做一層保護性驗證，
     * 避免模型偶發異常（例如 enum 限制失效、欄位缺漏）流入下游業務邏輯。
     */
    private void validate(ExpenseAnalysisResult result) {
        if (result.getAmount() == null || result.getAmount() < 0) {
            throw new GeminiApiException("Gemini 回傳的 amount 無效: " + result.getAmount());
        }
        List<String> validCategories = ExpenseCategory.labels();
        if (result.getCategory() == null || !validCategories.contains(result.getCategory())) {
            throw new GeminiApiException("Gemini 回傳的 category 不在允許清單內: " + result.getCategory());
        }
        if (result.getAdvice() == null || result.getAdvice().isBlank()) {
            throw new GeminiApiException("Gemini 回傳的 advice 為空");
        }
    }
}
