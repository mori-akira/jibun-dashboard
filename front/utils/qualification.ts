import type { SettingQualification } from "~/api/client";

export type Rank = "A" | "B" | "C" | "D";

export function getRankColorHexCode(
  rank: Rank,
  rankColors: SettingQualification
): string {
  const rankColorStr = `rank${rank}Color` as keyof SettingQualification;
  return rankColors?.[rankColorStr] || "#888";
}
