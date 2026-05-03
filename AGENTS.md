# Phone Down Project Instructions

These instructions apply to work in this repository.

## Teaching Mode

- Explain each meaningful step in detail while working.
- Teach the theory, concepts, and tradeoffs behind what you are doing.
- State why each step is being taken and what outcome it is meant to achieve.
- Treat the user as someone learning through the build process, not just receiving output.

## Phase Implementation Workflow

For every phase, sprint, or implementation section in this project:

1. Ask any clarification questions first.
   - Do this before writing a phase plan.
   - Wait for the user's answers before proceeding if clarification is needed.
2. Create an in-depth phase/sprint planning `.md` file.
   - Include detailed scope, architecture notes, tradeoffs, implementation steps, acceptance criteria, and checklists for progress tracking.
   - Do not start implementation until the user approves the phase plan.
3. After approval, implement the approved phase.
4. After implementation, run a comprehensive automated verification suite.
   - Include relevant unit tests, regression tests, integration tests, build checks, lint checks, and any project-appropriate validation.
   - If a test category cannot run yet, clearly document why.
5. Report completion only after verification passes and implementation completeness has been reviewed.
   - Summarize what changed, what tests were run, what passed, and any residual risks or follow-up items.

## Documentation During Iteration

- Update relevant Markdown documentation during each meaningful development iteration.
- Record what changed, which files were modified, which functions, classes, methods, or components were touched, why the change was made, tests run, and next steps.
- Prefer updating an existing relevant `.md` file when one already exists.
- If no relevant `.md` file exists, create or update a concise progress log in a sensible location rather than skipping documentation.
- Prefer updating the current phase plan or `v1-implementation-plan.md` progress log when appropriate.

## Local Build Priority

- For Android and iOS work, default to local builds on the developer machine.
- Use cloud builds only when explicitly requested or when local signing, SDK, emulator, or tooling constraints block progress.
- Prefer local Expo iteration workflows such as `expo run:android` and local dev-client setups when applicable.
- Before suggesting cloud builds, first verify and help configure local prerequisites such as Node.js, Java, Android SDK, `adb`, Xcode, CocoaPods, Expo CLI, and related tooling.
- Keep cloud build instructions available as a fallback, but treat them as secondary.

## Planning Gate

- Before writing any new phase plan, sprint plan, or implementation-planning `.md` file, ask the user clarifying questions first.
- Do not proceed directly into plan writing when scope, sequencing, priorities, tradeoffs, or acceptance expectations could affect the plan structure.
- Wait for the user's answers before drafting the planning document.
- After clarifications are resolved, create the detailed planning `.md` file with implementation checklists as usual.
- If no clarification is needed, explicitly say so and proceed to the planning document.

## Response Next-Step Guidance

- In each response, include the likely next step or next-step options so the user knows how to proceed.
- If the work can branch, present concise options for the user, especially after planning or documentation milestones.
- After writing a plan document, explicitly prompt the user to review it and state the common next actions (e.g., approve and start implementation, or request updates to the plan).
- Keep these next-step options short, actionable, and specific to the current workflow stage.

## Git And Secrets Safety

### Git Workflow Safety

- Before every commit, review what is being committed with `git status` and `git diff --cached`.
- Never use broad staging commands such as `git add .` or `git add -A` when sensitive or unrelated files might be present; prefer explicit paths or carefully reviewed patch staging.
- Check for sensitive or backup files before committing, especially files matching patterns like `.env`, `.env.*`, `.bak`, `.backup`, `.key`, `.pem`, `.p12`, `credentials`, or service-account JSON files.
- Verify `.gitignore` coverage when working with environment files, backups, generated secrets, or credentials.
- If suspicious untracked files appear, stop and resolve them before staging anything else.
- Before force operations or risky history edits, verify the current branch and avoid unsafe operations on primary branches.

### Secrets Prevention

- Never allow secrets, API keys, tokens, passwords, private keys, or credentials to be committed to git.
- Before any commit, scan staged changes for secrets and confirm no sensitive values appear in code, docs, config, or examples.
- Treat backup files created by tools as potential leak sources and either avoid creating them or store them outside the repository.
- If secrets are detected in staged or modified files, stop immediately, remove or replace them with placeholders, and verify the cleaned diff before proceeding.
- If a secret was already committed or pushed, clearly instruct the user to rotate it immediately and explain the recovery steps.

### Pre-Commit Safety Checklist

- Run `git status`.
- Run `git diff --cached`.
- Check staged filenames for sensitive patterns.
- Check docs and examples for hardcoded credentials.
- Confirm `.gitignore` protects environment files and backups.
- Ensure commit messages are descriptive and that the target branch is correct before pushing.

### Reference Incident

- Remember the Feb 7, 2026 incident: `.env.bak` created by `sed` exposed a bot token in documentation and was only caught because `git status` and staged changes were reviewed in time.
- Use that incident as a standing reminder to inspect untracked and staged files before every commit.
