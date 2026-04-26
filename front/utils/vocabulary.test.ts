import { describe, it, expect } from "vitest";
import { getTopTagCounts } from "./vocabulary";
import type { Vocabulary } from "~/generated/api/client/api";

const tag = (id: string, name: string) => ({
  vocabularyTagId: id,
  vocabularyTag: name,
  order: 0,
});

const vocab = (id: string, tags: ReturnType<typeof tag>[]): Vocabulary => ({
  vocabularyId: id,
  name: `vocab-${id}`,
  tags: new Set(tags),
});

describe("getTopTagCounts", () => {
  it("タグなしの場合は空配列を返す", () => {
    expect(getTopTagCounts([vocab("1", []), vocab("2", [])])).toEqual([]);
  });

  it("件数の多い順に並ぶ", () => {
    const tagA = tag("a", "A");
    const tagB = tag("b", "B");
    const tagC = tag("c", "C");
    const result = getTopTagCounts([
      vocab("1", [tagA, tagB]),
      vocab("2", [tagA, tagC]),
      vocab("3", [tagA]),
    ]);
    expect(result).toEqual([
      { id: "a", name: "A", count: 3 },
      { id: "b", name: "B", count: 1 },
      { id: "c", name: "C", count: 1 },
    ]);
  });

  it("上位limit件のみ返す", () => {
    const tagA = tag("a", "A");
    const tagB = tag("b", "B");
    const tagC = tag("c", "C");
    const tagD = tag("d", "D");
    const vocabs = [
      vocab("1", [tagA, tagB, tagC, tagD]),
      vocab("2", [tagA, tagB, tagC]),
      vocab("3", [tagA, tagB]),
      vocab("4", [tagA]),
    ];
    const result = getTopTagCounts(vocabs, 3);
    expect(result).toHaveLength(3);
    expect(result[0]).toEqual({ id: "a", name: "A", count: 4 });
    expect(result[1]).toEqual({ id: "b", name: "B", count: 3 });
    expect(result[2]).toEqual({ id: "c", name: "C", count: 2 });
  });

  it("vocabularyTagId がない場合は vocabularyTag をIDとして使う", () => {
    const vocabs: Vocabulary[] = [
      {
        vocabularyId: "1",
        name: "vocab-1",
        tags: new Set([{ vocabularyTag: "Foo", order: 0 }]),
      },
    ];
    const result = getTopTagCounts(vocabs);
    expect(result).toEqual([{ id: "Foo", name: "Foo", count: 1 }]);
  });

  it("vocabulariesが空の場合は空配列を返す", () => {
    expect(getTopTagCounts([])).toEqual([]);
  });
});
