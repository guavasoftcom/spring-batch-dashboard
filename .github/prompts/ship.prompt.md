---
description: Generate a commit message from the working tree, commit it, then push to the current branch's remote.
---

Bundle the standard "commit + push" loop into one shot. Steps:

1. **Inspect the working tree** — run these in parallel:
   - `git status` (no `-uall` flag)
   - `git diff HEAD` (everything that would be committed, staged + unstaged)
   - `git log --oneline -10` (so the drafted message matches repo style)

   If the tree is clean, tell the user there's nothing to ship and stop.

2. **Refuse if on `main`/`master`** — abort with a one-line explanation. The user must branch first; this command is for feature work.

3. **Draft the commit message** — match the repo's existing style (look at the `git log` output), lead with the *why* in 1–2 sentences for non-trivial changes, no emojis, no PR/branch references, no "this commit" phrasing.

4. **Stage explicitly** — `git add` specific paths rather than `git add -A` / `git add .` so untracked secrets or build artifacts don't sneak in. If anything in `git status` looks unintended (`.env`, generated output, scratch files), call it out and skip it.

5. **Commit** via heredoc, including this trailer:

   `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>`

   If a pre-commit hook fails, fix the underlying issue and create a new commit — never `--amend`, never `--no-verify`.

6. **Push** — run `git push`. On the branch's first push, set upstream with `git push -u origin <current-branch>`. Never `--force` or `--force-with-lease`.

7. **Confirm** — run `git status` to verify the tree is clean, then report the new commit SHA and the remote ref it landed on.
