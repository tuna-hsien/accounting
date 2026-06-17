package com.example.accounting.enums;

import java.util.Arrays;
import java.util.List;

/**
 * 記帳固定分類清單。
 * 這份清單同時用於：
 *   1. 組成提示詞 (prompt) 讓 AI 知道可選範圍
 *   2. 組成 Gemini responseSchema 的 enum 限制，強制模型只能回傳清單內的值
 * 如需新增/修改分類，只需要改這裡，不用動到 Service 的呼叫邏輯。
 */
public enum ExpenseCategory {

    FOOD("餐飲"),
    TRANSPORT("交通"),
    SHOPPING("購物"),
    ENTERTAINMENT("娛樂"),
    HOUSING("居住"),
    MEDICAL("醫療"),
    EDUCATION("教育"),
    UTILITIES("水電瓦斯"),
    INSURANCE("保險"),
    OTHER("其他");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 取得所有分類的中文字串清單，給 Gemini responseSchema 的 enum 用 */
    public static List<String> labels() {
        return Arrays.stream(values())
                .map(ExpenseCategory::getLabel)
                .toList();
    }
}
