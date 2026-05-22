# Delivery Verification Guide

Guide for verifying that PRs have been successfully merged to the `main` branch.

## Verification Steps

### 1. Check PR Merge Status

Use GitHub CLI to check if a PR has been merged:

```bash
gh pr list --state merged --base main
```

To check a specific PR:
```bash
gh pr view <pr-number> --json state,mergedAt,mergeCommitSha
```

### 2. Verify Commit in git log

Check if the commit appears in the main branch history:

```bash
git log origin/main --oneline | grep "<commit-message-or-hash>"
```

Or to see the full recent history:
```bash
git log origin/main --oneline -20
```

### 3. Verify File Exists in origin/main

Use `git show` to verify a file exists in the main branch:

```bash
git show origin/main:docs/delivery-verification.md
```

If the file exists, this command outputs its content. If not, it returns an error.

### 4. Get origin/main Commit Hash

```bash
git rev-parse origin/main
```

Or for a more detailed view:
```bash
git log origin/main -1 --format="%H %s"
```

## Verification Commands Summary

| Purpose | Command |
|---------|---------|
| List merged PRs | `gh pr list --state merged --base main` |
| Check specific PR | `gh pr view <PR#> --json state,mergedAt,mergeCommitSha` |
| Verify commit in log | `git log origin/main --oneline | grep "<term>"` |
| Verify file exists | `git show origin/main:<file-path>` |
| Get main branch HEAD hash | `git rev-parse origin/main` |

## Example: Complete Verification Flow

```bash
# 1. Get the commit hash from merged PR
gh pr list --state merged --base main --limit 10

# 2. Clone/checkout the repo and fetch latest
git fetch origin

# 3. Verify the file exists
git show origin/main:docs/delivery-verification.md

# 4. Get the commit hash as delivery proof
git rev-parse origin/main
```

## Troubleshooting

### PR shows merged but git log doesn't show commit

- The branch may not have been pushed to `origin`
- Run `git fetch origin` to update local refs
- Check if the PR was merged to the correct base branch

### git show returns "Path does not exist"

- The file may not have been committed on the main branch
- Verify the file path is correct (case-sensitive)
- Ensure the PR actually included this file
