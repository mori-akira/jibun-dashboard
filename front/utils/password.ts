export function matchCharacterTypeRule(value: string): boolean {
  const rules = [/[A-Z]/, /[a-z]/, /[0-9]/, /[^A-Za-z0-9]/];
  const matched = rules.filter((rule) => rule.test(value)).length;
  return matched >= 3;
}
