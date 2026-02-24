import pluginNext from "@next/eslint-plugin-next";
import tsParser from "@typescript-eslint/parser";

export default [
    // 1. Global Ignores
    {
        ignores: [
            ".next/**",
            "out/**",
            "build/**",
            "next-env.d.ts",
            "eslint-results.sarif"
        ],
    },

    // 2. Next.js Core Web Vitals Rules
    {
        files: ["**/*.{js,jsx,ts,tsx}"],
        languageOptions: {
            parser: tsParser,
            parserOptions: {
                ecmaFeatures: {
                    jsx: true,
                },
            },
        },
        plugins: {
            "@next/next": pluginNext,
        },
        rules: {
            ...pluginNext.configs.recommended.rules,
            ...pluginNext.configs["core-web-vitals"].rules,
        },
    },
];
