添付の PDF を読んで、次の集計ルールで正規化した JSON のみを返してください。

## 集計ルール

- overview.grossIncome: 「支給額合計」を参照
- overview.netIncome: 「差引支給額」を参照
- overview.operatingTime: 「所定内勤務時間数」に「時間外勤務時間数」「深夜勤務時間数」「休日勤務時間数」を加算
- overview.overtime: 「時間外勤務時間数」「深夜勤務時間数」「休日勤務時間数」を加算
- overview.bonus / overview.bonusTakeHome: 常に 0

- structure.basicSalary: 「基礎給与」
- structure.overtimePay: 「時間外勤務手当」「深夜勤務手当」「休日勤務手当」の合計
- structure.housingAllowance: 0
- structure.positionAllowance: 0
- structure.other: 上記以外の支給項目の合計（例: 通勤手当、前払退職金 等）

- payslipData:
  - 「支給項目」「控除項目」「勤怠管理項目」をカテゴリ key とし、各明細（名称=key, 数値=data）を配列化
  - 金額は整数、時間/日数は必要なら小数

## ワンショット参照（期待 JSON 例）

```
{{ONE_SHOT_JSON}}
```

## 注意事項

必ずスキーマ準拠の**純粋な JSON のみ**を返しなさい。
