import pluginNext from "@next/eslint-plugin-next";

export default [
    // 1. Global Ignores
    {
        ignores: [
            ".next/**",
            "out/**",
            "build/**",
            "next-env.d.ts",
            "eslint-results.sarif" // Ignore your CI artifacts!
        ],
    },

    // 2. Next.js Core Web Vitals Rules
    {
        files: ["**/*.{js,jsx,ts,tsx}"],
        plugins: {
            "@next/next": pluginNext,
        },
        rules: {
            ...pluginNext.configs.recommended.rules,
            ...pluginNext.configs["core-web-vitals"].rules,
        },
    },
];
