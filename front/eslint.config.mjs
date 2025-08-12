import withNuxt from "./.nuxt/eslint.config.mjs";
import pinia from "eslint-plugin-pinia";

export default withNuxt({
  plugins: { pinia },
  rules: {
    "vue/html-self-closing": "off",
    "vue/multi-word-component-names": "off",
  },
  files: ["**/*.ts", "**/*.vue"],
  ignores: ["api/client/**"],
});
