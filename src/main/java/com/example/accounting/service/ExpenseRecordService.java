package com.example.accounting.service;

import com.example.accounting.dto.ExpenseAnalysisResult;
import com.example.accounting.entity.Expense;
import com.example.accounting.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 串接「Gemini 解析」與「資料庫儲存」兩個步驟的應用層 Service。
 * Controller 只需要呼叫這裡，不需要知道 Gemini 與 JPA 的細節。
 */
@Service
public class ExpenseRecordService {

    private final GeminiExpenseService geminiExpenseService;
    private final ExpenseRepository expenseRepository;

    public ExpenseRecordService(GeminiExpenseService geminiExpenseService,
                                 ExpenseRepository expenseRepository) {
        this.geminiExpenseService = geminiExpenseService;
        this.expenseRepository = expenseRepository;
    }

    /**
     * 1. 呼叫 Gemini 取得結構化分析結果
     * 2. 將原始文字 + 分析結果組裝成 Expense Entity
     * 3. 寫入資料庫並回傳已儲存的 Entity（含 id、createdAt）
     *
     * 注意：Gemini API 呼叫本身不在 @Transactional 範圍內會更理想，
     * 但為求黑客松流程簡單，這裡讓「呼叫 API + 存檔」整體視為一個方法；
     * 若要嚴謹拆分交易邊界，可把 analyze() 移到呼叫端，只把 save 包進交易。
     */
    @Transactional
    public Expense createExpenseFromText(String rawText) {
        ExpenseAnalysisResult analysisResult = geminiExpenseService.analyze(rawText);

        Expense expense = new Expense(
                rawText,
                analysisResult.getAmount(),
                analysisResult.getCategory(),
                analysisResult.getAdvice()
        );

        return expenseRepository.save(expense);
    }

    /** 取得所有記帳紀錄，依時間由新到舊排序，供前端儀表板顯示 */
    @Transactional(readOnly = true)
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAllByOrderByCreatedAtDesc();
    }
}
