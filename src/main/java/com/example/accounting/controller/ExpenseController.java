package com.example.accounting.controller;

import com.example.accounting.entity.Expense;
import com.example.accounting.service.ExpenseRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ExpenseController {

    private final ExpenseRecordService expenseRecordService;

    public ExpenseController(ExpenseRecordService expenseRecordService) {
        this.expenseRecordService = expenseRecordService;
    }

    /**
     * POST /api/expenses
     * Body: { "text": "中午跟同事吃拉麵花了 150 元" }
     *
     * 流程：呼叫 Gemini 解析文字 -> 組裝成 Expense -> 存入資料庫 -> 回傳已儲存的紀錄（含 id、createdAt）
     */
    @PostMapping("/api/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public Expense createExpense(@RequestBody CreateExpenseRequest request) {
        return expenseRecordService.createExpenseFromText(request.text());
    }

    /**
     * GET /api/expenses
     * 回傳所有記帳紀錄，依建立時間由新到舊排序，供前端儀表板顯示。
     */
    @GetMapping("/api/expenses")
    public List<Expense> listExpenses() {
        return expenseRecordService.getAllExpenses();
    }

    public record CreateExpenseRequest(String text) {
    }
}
