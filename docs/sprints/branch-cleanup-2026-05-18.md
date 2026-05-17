# Branch Cleanup 2026-05-18

## Deleted Branches

The following branches were identified as merged to `origin/main` and have been deleted:

| Branch Name | Status |
|-------------|--------|
| origin/feature/merge-security-fixes | Merged, now deleted |
| origin/feature/merge-sprint51 | Merged, now deleted |
| origin/feature/merge-sprint51-tech-debt-and-monitoring | Merged, now deleted |
| origin/feature/merge-sprint55 | Merged, now deleted |
| origin/feature/merge-sprint59 | Merged, now deleted |
| origin/feature/merge-sprint61 | Merged, now deleted |
| origin/feature/sprint111-points-coupons | Merged, now deleted |
| origin/merge-cicd-final | Merged, now deleted |
| origin/merge-gate-76467bf1 | Merged, now deleted |
| origin/temp-merge-da3c3eb0 | Merged, now deleted |

## Preserved Branches

The following branches remain as they are either not merged or are active development branches:

- `origin/fix/controller-tests-v2` - Active fix branch
- `origin/main` - Main branch

## Verification

```bash
git fetch origin
git branch -r --merged origin/main
```

Result: Only `origin/main` and `origin/fix/controller-tests-v2` remain in the merged list.