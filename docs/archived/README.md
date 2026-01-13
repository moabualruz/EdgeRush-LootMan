# Archived Documentation

This folder contains historical documentation files that have been superseded by the new documentation structure.

## Why These Files Were Archived

As part of a documentation cleanup initiative (January 2026), we consolidated 60+ scattered status files into a single source of truth structure:

### New Authoritative Documents

| Document | Purpose |
|----------|---------|
| `CLAUDE.md` | Project overview, principles, and quick reference |
| `PROJECT_PRIORITIES.md` | Current work priorities and status |
| `.project/requirements.md` | Functional and non-functional requirements |
| `.project/decisions.md` | Architecture decisions and rationale |
| `.project/constraints.md` | Technical constraints and stack |
| `.project/glossary.md` | Domain terminology |
| `.project/non_goals.md` | Explicit non-goals |
| `.project/risks.md` | Risk assessment |

### What's in This Archive

- `status-files/` - Historical status updates from various development sessions
- `session-summaries/` - End-of-session completion reports

## Do NOT

- Create new `*_COMPLETE.md` or `*_STATUS.md` files
- Reference these archived files in active development
- Move files back to the root directory

## Do

- Update `PROJECT_PRIORITIES.md` when completing features
- Update `.project/` files when requirements or constraints change
- Keep `CLAUDE.md` as the single source of truth

## Historical Value

These files are preserved for:
- Understanding the project's evolution
- Referencing specific implementation details from past sessions
- Audit trail of development decisions

If you need information from these files, consider:
1. Is this information still accurate?
2. Should it be added to the current authoritative documents?
3. Is it purely historical and should remain archived?
