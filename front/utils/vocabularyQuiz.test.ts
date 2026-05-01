import { describe, it, expect } from "vitest";
import {
  selectQuizVocabularies,
  getScopeCount,
  getCountOptions,
} from "./vocabularyQuiz";
import type {
  Vocabulary,
  VocabularyQuizHistory,
} from "~/generated/api/client/api";

// --- helpers ---

const tag = (id: string) => ({
  vocabularyTagId: id,
  vocabularyTag: `tag-${id}`,
  order: 0,
});

const vocab = (id: string, tagIds: string[] = []): Vocabulary => ({
  vocabularyId: id,
  name: `name-${id}`,
  description: `desc-${id}`,
  tags: new Set(tagIds.map(tag)),
});

const history = (
  answeredAt: string,
  direction: "FRONT_TO_BACK" | "BACK_TO_FRONT",
  answers: { vocabularyId: string; result: "CORRECT" | "INCORRECT" }[],
): VocabularyQuizHistory => ({
  quizHistoryId: `hist-${answeredAt}`,
  answeredAt,
  direction,
  questionCount: answers.length,
  correctCount: answers.filter((a) => a.result === "CORRECT").length,
  incorrectCount: answers.filter((a) => a.result === "INCORRECT").length,
  answers,
});

// --- getScopeCount ---

describe("getScopeCount", () => {
  const vocabs = [
    vocab("1", ["a"]),
    vocab("2", ["b"]),
    vocab("3", ["a", "b"]),
    vocab("4"),
  ];

  it("tagIds が空のとき全件を返す", () => {
    expect(getScopeCount(vocabs, [])).toBe(4);
  });

  it("指定タグに一致するボキャブラリー数を返す", () => {
    expect(getScopeCount(vocabs, ["a"])).toBe(2);
  });

  it("複数タグ指定時はいずれかに一致すれば含む", () => {
    expect(getScopeCount(vocabs, ["a", "b"])).toBe(3);
  });

  it("一致しないタグを指定すると 0 を返す", () => {
    expect(getScopeCount(vocabs, ["z"])).toBe(0);
  });

  it("ボキャブラリーが空のとき 0 を返す", () => {
    expect(getScopeCount([], ["a"])).toBe(0);
  });
});

// --- getCountOptions ---

describe("getCountOptions", () => {
  it("scopeCount が 0 のとき空配列を返す", () => {
    expect(getCountOptions(0)).toEqual([]);
  });

  it("scopeCount が 5 未満のとき scopeCount 自体を返す", () => {
    expect(getCountOptions(3)).toEqual([3]);
    expect(getCountOptions(4)).toEqual([4]);
  });

  it("scopeCount がちょうど 5 のとき [5] を返す", () => {
    expect(getCountOptions(5)).toEqual([5]);
  });

  it("5 刻みのオプションを返す", () => {
    expect(getCountOptions(15)).toEqual([5, 10, 15]);
  });

  it("5 の倍数でない scopeCount は最後に余り分を含む", () => {
    expect(getCountOptions(12)).toEqual([5, 10, 12]);
  });
});

// --- selectQuizVocabularies ---

describe("selectQuizVocabularies", () => {
  const v1 = vocab("1", ["a"]);
  const v2 = vocab("2", ["b"]);
  const v3 = vocab("3", ["a", "b"]);
  const v4 = vocab("4");
  const allVocabs = [v1, v2, v3, v4];

  it("questionCount 分だけ返す", () => {
    const result = selectQuizVocabularies(
      allVocabs,
      [],
      [],
      2,
      "FRONT_TO_BACK",
    );
    expect(result).toHaveLength(2);
  });

  it("tagIds 指定でスコープを絞る", () => {
    const result = selectQuizVocabularies(
      allVocabs,
      [],
      ["a"],
      10,
      "FRONT_TO_BACK",
    );
    expect(result.every((v) => ["1", "3"].includes(v.vocabularyId!))).toBe(
      true,
    );
    expect(result).toHaveLength(2);
  });

  it("questionCount がスコープ数より多い場合はスコープ数を上限にする", () => {
    const result = selectQuizVocabularies(
      allVocabs,
      [],
      [],
      100,
      "FRONT_TO_BACK",
    );
    expect(result).toHaveLength(allVocabs.length);
  });

  it("未回答のボキャブラリーが優先して含まれる", () => {
    const hist = history("2024-01-01T00:00:00Z", "FRONT_TO_BACK", [
      { vocabularyId: "1", result: "CORRECT" },
      { vocabularyId: "2", result: "CORRECT" },
      { vocabularyId: "3", result: "CORRECT" },
    ]);
    // v4 のみ未回答 → questionCount=1 なら v4 が選ばれる
    const result = selectQuizVocabularies(
      allVocabs,
      [hist],
      [],
      1,
      "FRONT_TO_BACK",
    );
    expect(result[0]?.vocabularyId).toBe("4");
  });

  it("INCORRECT が CORRECT より優先して含まれる", () => {
    const hist = history("2024-01-01T00:00:00Z", "FRONT_TO_BACK", [
      { vocabularyId: "1", result: "INCORRECT" },
      { vocabularyId: "2", result: "CORRECT" },
    ]);
    // v3, v4 は未回答 → 優先順: v3/v4(未回答) > v1(INCORRECT) > v2(CORRECT)
    // questionCount=2 なら v2 は含まれない
    const result = selectQuizVocabularies(
      [v1, v2, v3],
      [hist],
      [],
      2,
      "FRONT_TO_BACK",
    );
    const ids = result.map((v) => v.vocabularyId);
    expect(ids).toContain("3");
    expect(ids).toContain("1");
    expect(ids).not.toContain("2");
  });

  it("direction が異なる履歴は無視する", () => {
    const hist = history("2024-01-01T00:00:00Z", "BACK_TO_FRONT", [
      { vocabularyId: "1", result: "CORRECT" },
      { vocabularyId: "2", result: "CORRECT" },
      { vocabularyId: "3", result: "CORRECT" },
      { vocabularyId: "4", result: "CORRECT" },
    ]);
    // FRONT_TO_BACK の選択では全員未回答扱い
    const result = selectQuizVocabularies(
      allVocabs,
      [hist],
      [],
      4,
      "FRONT_TO_BACK",
    );
    expect(result).toHaveLength(4);
  });

  it("複数の履歴がある場合、最新の結果を使う", () => {
    const older = history("2024-01-01T00:00:00Z", "FRONT_TO_BACK", [
      { vocabularyId: "1", result: "INCORRECT" },
    ]);
    const newer = history("2024-06-01T00:00:00Z", "FRONT_TO_BACK", [
      { vocabularyId: "1", result: "CORRECT" },
    ]);
    // 最新は CORRECT なので v1 は correct 扱い → v4(未回答) が優先
    const result = selectQuizVocabularies(
      [v1, v4],
      [older, newer],
      [],
      1,
      "FRONT_TO_BACK",
    );
    expect(result[0]?.vocabularyId).toBe("4");
  });

  it("ボキャブラリーが空のとき空配列を返す", () => {
    expect(selectQuizVocabularies([], [], [], 5, "FRONT_TO_BACK")).toEqual([]);
  });
});
