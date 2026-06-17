# 🚀 AI 智慧記帳與消費分析管家 (AI Smart Accounting)

> **期末個人實戰挑戰 | 極速黑客松 Hackathon 參賽作品**
> 透過 AI Vibe Coding 在 3 小時內打造的「Java 後端 × H2 DB × 網頁前端 × Gemini AI」全端應用系統。

## 🌐 專案連結
* **Live Demo (作品部署連結):** [accounting-production-3a73.up.railway.app]
* **GitHub Repository:** [https://github.com/tuna-hsien/accounting.git]
* **AI Chat History (Prompt 紀錄):** [https://share.gemini.google/abbSG3TyR7oe]

---

## 💡 專案簡介與商務邏輯
本專案為一款「一句話記帳」的智慧管家。解決了現代人記帳繁瑣的痛點，使用者只需以自然語言輸入消費情境（例如：「今天中午跟同事吃拉麵花了 150 元」），系統便會無縫結合 AI 模型，自動擷取消費金額、精準分類，並根據該筆消費給予幽默或實用的理財建議，最終將結構化資料落地儲存並視覺化呈現。

### 🛠️ 技術選型
* **Frontend:** HTML5, TailwindCSS, Vanilla JavaScript, Chart.js
* **Backend:** Java 17, Spring Boot (Web, Data JPA)
* **Database:** H2 In-Memory Database
* **AI/ML Module:** Gemini 2.5 Flash API (透過 `RestTemplate` 串接)
* **Deployment:** Railway

---

## 🔄 系統架構與資料流向 (Data Flow)

本系統的核心亮點在於**「精準控制 AI 輸出並無縫嵌入商務邏輯」**。透過在後端組裝嚴謹的 Prompt 與設定 `responseSchema`，確保 AI 穩定回傳符合 DB 格式的 JSON，避免前端處理非結構化資料的風險。

### 📍 資料流動步驟說明：
1. **Frontend 呼叫 API：** 使用者在前端介面輸入自然語言，透過 `fetch` 發送 HTTP POST 請求至 Java 後端 `/api/expenses`。
2. **Backend 整合 AI 模型：** Spring Boot Controller 接收字串後，交由 Service 層封裝成包含 `responseSchema` (強制 JSON 輸出) 與特定分類 Enum 的 Prompt。
3. **AI 分析與擷取：** 透過 `RestTemplate` 呼叫 Gemini 2.5 Flash API，模型將語意解析為結構化的 `{amount, category, advice}`。
4. **資料庫存取：** 後端將 AI 解析結果與原始字串組裝成 JPA Entity，寫入 H2 Database，完成資料落地。
5. **前端動態渲染：** 後端回傳儲存成功的實體，前端重新發送 GET 請求取得最新列表，並即時更新 Chart.js 的分類占比圖表。

### 🗺️ 系統架構圖
```mermaid
graph TD
    subgraph Frontend [前端網頁 UI]
        A[使用者輸入文字]
        E[儀表板與 Chart.js 圖表]
    end

    subgraph Backend [Java Spring Boot 後端]
        B[ExpenseController]
        C[ExpenseRecordService]
        F[GeminiExpenseService]
    end

    subgraph AI_Model [AI 模型]
        G[Gemini 2.5 Flash API]
    end

    subgraph Database [資料庫]
        D[(H2 In-Memory DB)]
    end

    %% 資料流向定義
    A -- "1. POST /api/expenses" --> B
    B -- "2. 傳遞 rawText" --> C
    C -- "3. 請求解析" --> F
    F -- "4. HTTP POST (Prompt + Schema)" --> G
    G -- "5. 回傳結構化 JSON" --> F
    F -- "6. DTO 轉換" --> C
    C -- "7. JPA Save" --> D
    D -- "8. 回傳 Entity" --> C
    C -- "9. HTTP 201 Created" --> A
    E -- "10. GET /api/expenses" --> B
    B -- "11. JPA FindAll" --> D
    D -- "12. 回傳 List" --> E
