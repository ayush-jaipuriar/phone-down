# Phone Down Project Instructions

These instructions apply to work in this repository.

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
- Record what changed, which files were modified, which functions/classes/components were touched, why the change was made, tests run, and next steps.
- Prefer updating the current phase plan or `v1-implementation-plan.md` progress log when appropriate.

## Planning Gate

- Before writing any new phase plan, sprint plan, or implementation-planning `.md` file, ask clarifying questions if scope, sequencing, priorities, tradeoffs, or acceptance expectations could affect the plan.
- If no clarification is needed, explicitly say so and proceed to the planning document.

## Git And Secrets Safety

- Before every commit, review `git status` and `git diff --cached`.
- Avoid broad staging commands when unrelated or sensitive files may be present.
- Never commit secrets, API keys, tokens, passwords, private keys, credentials, environment files, backup files, or service-account JSON files.
- Treat `.env.bak` and similar backup files as high-risk.
