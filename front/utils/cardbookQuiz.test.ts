import { describe, it, expect } from "vitest";
import { selectQuizCards, getCardbookScopeCount } from "./cardbookQuiz";
import { getCountOptions } from "./vocabularyQuiz";
import type {
  CardbookCard,
  CardbookQuizHistory,
} from "~/generated/api/client/api";

// --- helpers ---

const card = (id: string): CardbookCard => ({
  cardId: id,
  front: `front-${id}`,
  back: `back-${id}`,
  cardbookId: "book-1",
  createdDateTime: `2024-01-0${id}T00:00:00Z`,
});

const history = (
  answeredAt: string,
  direction: "FRONT_TO_BACK" | "BACK_TO_FRONT",
  answers: { cardId: string; result: "CORRECT" | "INCORRECT" }[],
): CardbookQuizHistory => ({
  quizHistoryId: `hist-${answeredAt}`,
  cardbookId: "book-1",
  answeredAt,
  direction,
  questionCount: answers.length,
  correctCount: answers.filter((a) => a.result === "CORRECT").length,
  incorrectCount: answers.filter((a) => a.result === "INCORRECT").length,
  answers,
});

// --- getCardbookScopeCount ---

describe("getCardbookScopeCount", () => {
  it("カード数をそのまま返す", () => {
    expect(getCardbookScopeCount([card("1"), card("2"), card("3")])).toBe(3);
  });

  it("カードが空のとき 0 を返す", () => {
    expect(getCardbookScopeCount([])).toBe(0);
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

// --- selectQuizCards ---

describe("selectQuizCards", () => {
  const c1 = card("1");
  const c2 = card("2");
  const c3 = card("3");
  const c4 = card("4");
  const allCards = [c1, c2, c3, c4];

  it("questionCount 分だけ返す", () => {
    const result = selectQuizCards(allCards, [], 2, "FRONT_TO_BACK");
    expect(result).toHaveLength(2);
  });

  it("questionCount がカード数より多い場合はカード数を上限にする", () => {
    const result = selectQuizCards(allCards, [], 100, "FRONT_TO_BACK");
    expect(result).toHaveLength(allCards.length);
  });

  it("未回答のカードが優先して含まれる", () => {
    const hist = history("2024-01-01T00:00:00Z", "FRONT_TO_BACK", [
      { cardId: "1", result: "CORRECT" },
      { cardId: "2", result: "CORRECT" },
      { cardId: "3", result: "CORRECT" },
    ]);
    // c4 のみ未回答 → questionCount=1 なら c4 が選ばれる
    const result = selectQuizCards(allCards, [hist], 1, "FRONT_TO_BACK");
    expect(result[0]?.cardId).toBe("4");
  });

  it("INCORRECT が CORRECT より優先して含まれる", () => {
    const hist = history("2024-01-01T00:00:00Z", "FRONT_TO_BACK", [
      { cardId: "1", result: "INCORRECT" },
      { cardId: "2", result: "CORRECT" },
    ]);
    // c3, c4 は未回答 → 優先順: c3/c4(未回答) > c1(INCORRECT) > c2(CORRECT)
    // questionCount=2 なら c2 は含まれない
    const result = selectQuizCards([c1, c2, c3], [hist], 2, "FRONT_TO_BACK");
    const ids = result.map((c) => c.cardId);
    expect(ids).toContain("3");
    expect(ids).toContain("1");
    expect(ids).not.toContain("2");
  });

  it("direction が異なる履歴は無視する", () => {
    const hist = history("2024-01-01T00:00:00Z", "BACK_TO_FRONT", [
      { cardId: "1", result: "CORRECT" },
      { cardId: "2", result: "CORRECT" },
      { cardId: "3", result: "CORRECT" },
      { cardId: "4", result: "CORRECT" },
    ]);
    // FRONT_TO_BACK の選択では全員未回答扱い
    const result = selectQuizCards(allCards, [hist], 4, "FRONT_TO_BACK");
    expect(result).toHaveLength(4);
  });

  it("複数の履歴がある場合、最新の結果を使う", () => {
    const older = history("2024-01-01T00:00:00Z", "FRONT_TO_BACK", [
      { cardId: "1", result: "INCORRECT" },
    ]);
    const newer = history("2024-06-01T00:00:00Z", "FRONT_TO_BACK", [
      { cardId: "1", result: "CORRECT" },
    ]);
    // 最新は CORRECT なので c1 は correct 扱い → c4(未回答) が優先
    const result = selectQuizCards(
      [c1, c4],
      [older, newer],
      1,
      "FRONT_TO_BACK",
    );
    expect(result[0]?.cardId).toBe("4");
  });

  it("カードが空のとき空配列を返す", () => {
    expect(selectQuizCards([], [], 5, "FRONT_TO_BACK")).toEqual([]);
  });
});
