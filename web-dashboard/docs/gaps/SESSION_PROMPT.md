# Session Prompt: Continue Gap Remediation

Copy and paste this prompt at the start of each new session to continue working on the gap remediation plan.

---

## The Prompt

```
# CONTEXT

I am working on the EdgeRush LootMan web-dashboard project. There is an ongoing gap remediation effort with 12 identified gaps.

## Key Files to Reference

1. **Gap Tracker:** `web-dashboard/docs/gaps/GAP_TRACKER.md`
   - Shows current status of all gaps (TODO/IN PROGRESS/DONE)
   - Contains work log of completed actions
   - Identifies which gap to work on next

2. **Implementation Plan:** `web-dashboard/docs/gaps/IMPLEMENTATION_PLAN.md`
   - Detailed instructions for each gap
   - Code samples and patterns to follow
   - Test requirements and verification steps

3. **Gap Analysis:** `web-dashboard/docs/gaps/GAP_ANALYSIS.md`
   - Original audit findings
   - Domain-specific requirements

## YOUR TASK

1. Read `docs/gaps/GAP_TRACKER.md` to see current progress
2. Identify the next gap to work on (look for "Next Up" section or first `⬜ TODO`)
3. Read the corresponding section in `docs/gaps/IMPLEMENTATION_PLAN.md`
4. Implement the gap following TDD workflow:
   - Write/update tests first
   - Implement the code
   - Run tests to verify
5. Update `GAP_TRACKER.md`:
   - Change status to `🔄 IN PROGRESS` when starting
   - Change status to `✅ DONE` when verified
   - Add entry to Work Log
6. Report what was completed and what's next

## EXISTING PATTERNS TO FOLLOW

- Use TanStack Query (`useQuery`, `useMutation`) for data fetching
- Use `useToast` composable for notifications
- Follow existing test patterns in `src/**/*.test.ts` (Vitest)
- Follow existing E2E patterns in `e2e/*.spec.ts` (Playwright)
- Use existing components: `SkeletonCard`, `SkeletonTable`, alert classes

## TEST COMMANDS

```bash
# Unit tests
npm run test

# E2E tests
npx playwright test

# Specific test file
npm run test -- [filename]
npx playwright test [spec-name]
```

Let's continue the gap remediation. Start by reading the tracker and telling me what gap you'll work on.
```

---

## Alternative: Quick Status Check Prompt

```
Read `web-dashboard/docs/gaps/GAP_TRACKER.md` and give me a summary of:
1. How many gaps are done vs remaining
2. Which gap is currently in progress (if any)
3. What the recommended next gap is
```

---

## Alternative: Specific Gap Prompt

Replace `GAP-XXX` with the gap ID you want to work on:

```
I want to work on GAP-XXX from the gap remediation plan.

1. Read `web-dashboard/docs/gaps/IMPLEMENTATION_PLAN.md` for the details
2. Update `web-dashboard/docs/gaps/GAP_TRACKER.md` to mark it IN PROGRESS
3. Implement following TDD workflow
4. Run tests and verify
5. Update tracker to DONE when complete
```
