import type {
  Vocabulary,
  VocabularyQuizHistory,
} from "~/generated/api/client/api";
import { shuffle } from "~/utils/rand";

export type QuizDirection = "FRONT_TO_BACK" | "BACK_TO_FRONT";
export type QuizResult = "CORRECT" | "INCORRECT";

export function selectQuizVocabularies(
  vocabularies: Vocabulary[],
  histories: VocabularyQuizHistory[],
  tagIds: string[],
  questionCount: number,
  direction: QuizDirection,
): Vocabulary[] {
  const scoped =
    tagIds.length === 0
      ? vocabularies
      : vocabularies.filter((v) =>
          Array.from(v.tags ?? []).some((t) =>
            tagIds.includes(t.vocabularyTagId ?? ""),
          ),
        );

  // 対象方向の最新回答結果をボキャブラリーIDごとに取得
  const latestResultMap = new Map<string, QuizResult>();
  const sorted = [...histories].sort(
    (a, b) =>
      new Date(b.answeredAt!).getTime() - new Date(a.answeredAt!).getTime(),
  );
  for (const h of sorted) {
    if (h.direction !== direction) continue;
    for (const answer of h.answers ?? []) {
      const id = answer.vocabularyId ?? "";
      if (!latestResultMap.has(id)) {
        latestResultMap.set(id, answer.result as QuizResult);
      }
    }
  }

  const neverAnswered: Vocabulary[] = [];
  const incorrect: Vocabulary[] = [];
  const correct: Vocabulary[] = [];

  for (const vocab of scoped) {
    const latest = latestResultMap.get(vocab.vocabularyId ?? "");
    if (!latest) neverAnswered.push(vocab);
    else if (latest === "INCORRECT") incorrect.push(vocab);
    else correct.push(vocab);
  }

  return [
    ...shuffle(neverAnswered),
    ...shuffle(incorrect),
    ...shuffle(correct),
  ].slice(0, questionCount);
}

export function getScopeCount(
  vocabularies: Vocabulary[],
  tagIds: string[],
): number {
  if (tagIds.length === 0) return vocabularies.length;
  return vocabularies.filter((v) =>
    Array.from(v.tags ?? []).some((t) =>
      tagIds.includes(t.vocabularyTagId ?? ""),
    ),
  ).length;
}

export function getCountOptions(scopeCount: number): number[] {
  const options: number[] = [];
  for (let i = 5; i <= scopeCount; i += 5) options.push(i);
  if (options.length === 0 && scopeCount > 0) options.push(scopeCount);
  else if (scopeCount % 5 !== 0 && scopeCount > 0) options.push(scopeCount);
  return options;
}

export function getDirectionLabel(direction: QuizDirection): string {
  return direction === "FRONT_TO_BACK"
    ? "Name->Description"
    : "Description->Name";
}
