import type { Vocabulary } from "~/generated/api/client/api";

export type TagCount = { id: string; name: string; count: number };

export function getTopTagCounts(
  vocabularies: Vocabulary[],
  limit: number = 3,
): TagCount[] {
  const map = new Map<string, TagCount>();
  for (const vocab of vocabularies) {
    for (const tag of vocab.tags ?? []) {
      const id = tag.vocabularyTagId ?? tag.vocabularyTag;
      const existing = map.get(id);
      if (existing) {
        existing.count++;
      } else {
        map.set(id, { id, name: tag.vocabularyTag, count: 1 });
      }
    }
  }
  return [...map.values()].sort((a, b) => b.count - a.count).slice(0, limit);
}
