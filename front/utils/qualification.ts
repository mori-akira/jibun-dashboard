export function getRankColorClass(rank: unknown): string {
  switch (rank) {
    case "A":
      return "text-blue-500";
    case "B":
      return "text-green-500";
    case "C":
      return "text-orange-500";
    case "D":
      return "text-yellow-500";
    default:
      return "";
  }
}
