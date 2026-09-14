# Prompt for GitHub Copilot

Use this prompt in GitHub Copilot Chat with **Agent mode** enabled:

```text
Act as a senior Android engineer. Build and maintain a very small, beginner-friendly Android quiz app in this repository.

Requirements:
- Native Android app written in Kotlin; no web view and no network access.
- Use a single Activity and Android platform widgets so the sample stays easy to read.
- Arabic, right-to-left interface with five short general-knowledge questions and four choices per question.
- After a choice, lock the answers, color the correct choice green, color a wrong selected choice red, and show a one-sentence explanation.
- Show question progress, calculate the score, provide a result screen, and allow restarting.
- Preserve state during screen recreation.
- Minimum Android 7.0 (API 24), target/compile API 35, Java 17.
- Keep dependencies minimal. Never add analytics, ads, permissions, accounts, secrets, or remote services.
- Keep the project buildable with `gradle assembleDebug`.
- Update README.md whenever behavior or build requirements change.
- Before finishing: inspect all changed files, fix compilation issues, explain the changes, and state the exact verification command.

Visual direction: clean white/slate background, indigo primary color, large readable Arabic text, generous spacing, portrait layout, and accessible button labels.

Do not expand the scope beyond this experimental quiz app unless explicitly asked.
```

## Suggested Copilot settings

- Mode: **Agent** for repository-wide work; **Ask** for explanations only.
- Working set: repository root.
- Auto-fix: allow only project files, never credentials or machine settings.
- Review: inspect the proposed diff before accepting large changes.
- Terminal commands: allow only Android build/test commands for this repository.
