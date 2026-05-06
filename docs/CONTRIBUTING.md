# Contributing to Minimall

## Commit Message Format

```
<type>: <description>

[optional body]
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `refactor`: Code refactoring
- `docs`: Documentation changes
- `test`: Test changes
- `chore`: Maintenance tasks
- `perf`: Performance improvements
- `ci`: CI/CD changes

### Issue Association

All commits MUST be associated with an issue. Use one of the following formats:

1. **In commit message footer**:
   ```
   Refs: MIN-123
   Fixes: MIN-456
   ```

2. **In branch name** (for feature branches):
   ```
   feature/MIN-123-add-user-auth
   fix/MIN-456-order-calculation
   ```

### Code Review Requirements

All code changes require review before merging:

1. **Review Checklist**: See [CODE_REVIEW_CHECKLIST.md](./CODE_REVIEW_CHECKLIST.md)
2. **Security Scan**: Security-sensitive code must pass security review
3. **Test Coverage**: Minimum 80% coverage required

### Pull Request Process

1. Create a feature branch from `main`
2. Make changes following the commit message format
3. Ensure all tests pass
4. Complete the code review checklist
5. Request review from at least one team member
6. Squash and merge after approval

### Issue/PR Linking

- Use `Refs: MIN-XXX` to reference an issue
- Use `Closes: MIN-XXX` to auto-close on merge
- Use `Fixes: MIN-XXX` for bug fixes that close issues

## Branch Naming Convention

```
<type>/MIN-<issue-number>-<short-description>
```

Examples:
- `feature/MIN-123-user-authentication`
- `fix/MIN-456-order-total-calculation`
- `refactor/MIN-789-cleanup-controller`
