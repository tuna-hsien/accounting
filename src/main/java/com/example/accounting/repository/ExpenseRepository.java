package com.example.accounting.repository;

import com.example.accounting.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * 依建立時間由新到舊排序，供前端儀表板顯示列表。
     * 方法名稱遵循 Spring Data JPA 命名規則，自動產生對應的查詢，不需要寫 SQL。
     */
    List<Expense> findAllByOrderByCreatedAtDesc();
}
