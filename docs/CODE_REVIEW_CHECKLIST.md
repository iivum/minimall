# Code Review Checklist

## Pre-Review Requirements

- [ ] All CI checks passing
- [ ] No merge conflicts
- [ ] Branch is up to date with target branch

## Code Quality

- [ ] Code is readable and well-named
- [ ] Functions are focused (<50 lines)
- [ ] Files are cohesive (<800 lines)
- [ ] No deep nesting (>4 levels)
- [ ] No hardcoded secrets or credentials
- [ ] No console.log or debug statements
- [ ] Errors are handled explicitly

## Security

- [ ] No hardcoded credentials (API keys, passwords, tokens)
- [ ] All user inputs validated
- [ ] SQL injection prevention (parameterized queries)
- [ ] XSS prevention (sanitized output)
- [ ] Authentication/authorization verified
- [ ] Rate limiting on all endpoints
- [ ] Error messages don't leak sensitive data

## Testing

- [ ] Tests exist for new functionality
- [ ] Test coverage meets 80% minimum
- [ ] Tests follow AAA pattern (Arrange-Act-Assert)
- [ ] Test names are descriptive

## Functionality

- [ ] Code actually solves the issue
- [ ] Edge cases handled
- [ ] No unintended side effects
- [ ] API contracts maintained

## Performance

- [ ] No N+1 queries
- [ ] Pagination where applicable
- [ ] No unbounded queries

## Review Severity

| Level | Meaning | Action |
|-------|---------|--------|
| CRITICAL | Security vulnerability or data loss risk | **BLOCK** - Must fix before merge |
| HIGH | Bug or significant quality issue | **WARN** - Should fix before merge |
| MEDIUM | Maintainability concern | **INFO** - Consider fixing |
| LOW | Style or minor suggestion | **NOTE** - Optional |
