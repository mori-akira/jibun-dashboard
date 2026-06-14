import type {
  CardbookCard,
  CardbookQuizHistory,
} from "~/generated/api/client/api";
import { shuffle } from "./rand";
import type { QuizDirection, QuizResult } from "./vocabularyQuiz";

export function selectQuizCards(
  cards: CardbookCard[],
  histories: CardbookQuizHistory[],
  questionCount: number,
  direction: QuizDirection,
): CardbookCard[] {
  const latestResultMap = new Map<string, QuizResult>();
  const sorted = [...histories].sort(
    (a, b) =>
      new Date(b.answeredAt!).getTime() - new Date(a.answeredAt!).getTime(),
  );
  for (const h of sorted) {
    if (h.direction !== direction) continue;
    for (const answer of h.answers ?? []) {
      const id = answer.cardId ?? "";
      if (!latestResultMap.has(id)) {
        latestResultMap.set(id, answer.result as QuizResult);
      }
    }
  }

  const neverAnswered: CardbookCard[] = [];
  const incorrect: CardbookCard[] = [];
  const correct: CardbookCard[] = [];

  for (const card of cards) {
    const latest = latestResultMap.get(card.cardId ?? "");
    if (!latest) neverAnswered.push(card);
    else if (latest === "INCORRECT") incorrect.push(card);
    else correct.push(card);
  }

  return [
    ...shuffle(neverAnswered),
    ...shuffle(incorrect),
    ...shuffle(correct),
  ].slice(0, questionCount);
}

export function getCardbookScopeCount(cards: CardbookCard[]): number {
  return cards.length;
}

export function getCardbookDirectionLabel(direction: QuizDirection): string {
  return direction === "FRONT_TO_BACK" ? "Front → Back" : "Back → Front";
}
