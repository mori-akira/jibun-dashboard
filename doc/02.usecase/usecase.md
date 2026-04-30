## ダッシュボード

```mermaid
graph TB

    subgraph "ダッシュボード"
        user((ユーザ))
        salary[給与を参照する]
        qualification[資格を参照する]
        educationPlan[教育計画を参照する]
        vocabulary[ボキャブラリーを参照する]
    end

    user --- salary
    user --- qualification
    user --- educationPlan
    user --- vocabulary
```

## 給与

```mermaid
graph TB

    subgraph "給与"
        user((ユーザ))
        summary[給与サマリーを参照する]
        payslip[給与明細を参照する]
        edit[給与情報を更新する]

        yearIncomeSummary(年収サマリー)
        overtimeSummary(残業サマリー)
        structureSummary(構成サマリー)
        compareSummary(比較サマリー)
        payslipSummary(給与明細サマリー)

        formEdit(フォーム編集)
        jsonEdit(JSON編集)
        uploadOcrEdit(アップロード＆OCR)
    end

    user --- summary
    user --- payslip
    user --- edit

    summary -.- yearIncomeSummary
    summary -.- overtimeSummary
    summary -.- structureSummary
    summary -.- compareSummary
    summary -.- payslipSummary

    edit -.- formEdit
    edit -.- jsonEdit
    edit -.- uploadOcrEdit
```

## 資格

```mermaid
graph TB

    subgraph "資格"
        user((ユーザ))
        view[資格を参照する]
        edit[資格情報を更新する]

        acquired(現在の資格)
        planning(近い未来の資格)
        dream(遠い未来の資格)
    end

    user --- view
    user --- edit

    view -.- acquired
    view -.- planning
    view -.- dream
```

## ボキャブラリー

```mermaid
graph TB

    subgraph "ボキャブラリー"
        user((ユーザ))
        view[ボキャブラリーを参照する]
        edit[ボキャブラリーを更新する]
        quiz[クイズを出題する]
    end

    user --- view
    user --- edit
    user --- quiz
```
