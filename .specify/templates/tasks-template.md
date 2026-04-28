---

description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are REQUIRED whenever behavior changes. Every bug fix MUST include a regression test. If a task intentionally has no automated test, the reason MUST be stated explicitly.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions
- Include validation work for code quality, performance, and Chinese copy where relevant

## Path Conventions

- **Single project**: `src/`, `tests/` at repository root
- **Web app**: `backend/src/`, `frontend/src/`
- **Mobile**: `api/src/`, `ios/src/` or `android/src/`
- Paths shown below assume single project - adjust based on plan.md structure

<!-- 
  ============================================================================
  IMPORTANT: The tasks below are SAMPLE TASKS for illustration purposes only.

  The /speckit-tasks command MUST replace these with actual tasks based on:
  - User stories from spec.md (with their priorities P1, P2, P3...)
  - Feature requirements from plan.md
  - Entities from data-model.md
  - Endpoints from contracts/

  Tasks MUST be organized by user story so each story can be:
  - Implemented independently
  - Tested independently
  - Delivered as an MVP increment

  DO NOT keep these sample tasks in the generated tasks.md file.
  ============================================================================
-->

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create project structure per implementation plan
- [ ] T002 Initialize [language] project with [framework] dependencies
- [ ] T003 [P] Configure linting, formatting, and static analysis tools
- [ ] T004 [P] Establish performance measurement approach and baseline if this feature touches critical flows

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

Examples of foundational tasks (adjust based on your project):

- [ ] T005 Setup database schema and migrations framework
- [ ] T006 [P] Implement authentication/authorization framework
- [ ] T007 [P] Setup API routing and middleware structure
- [ ] T008 Create base models/entities that all stories depend on
- [ ] T009 Configure logging, diagnostics, and error reporting infrastructure
- [ ] T010 Setup environment configuration management
- [ ] T011 Define Chinese copy conventions or shared localization resources for user-facing output

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - [Title] (Priority: P1) 🎯 MVP

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T012 [P] [US1] Unit or contract test for changed behavior in [exact file path]
- [ ] T013 [P] [US1] Integration test for the main user journey in [exact file path]
- [ ] T014 [US1] Add regression test for the primary failure mode in [exact file path]

### Implementation for User Story 1

- [ ] T015 [P] [US1] Implement core model or state changes in [exact file path]
- [ ] T016 [US1] Implement service or business logic in [exact file path]
- [ ] T017 [US1] Implement endpoint, page, or interaction in [exact file path]
- [ ] T018 [US1] Update Chinese user-facing copy, labels, errors, and empty states in [exact file path]
- [ ] T019 [US1] Measure and verify performance target for this story, record result in [artifact or note]

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - [Title] (Priority: P2)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 2 ⚠️

- [ ] T020 [P] [US2] Unit or contract test for changed behavior in [exact file path]
- [ ] T021 [P] [US2] Integration test for the main user journey in [exact file path]

### Implementation for User Story 2

- [ ] T022 [P] [US2] Implement model or state changes in [exact file path]
- [ ] T023 [US2] Implement service or business logic in [exact file path]
- [ ] T024 [US2] Implement endpoint, page, or interaction in [exact file path]
- [ ] T025 [US2] Verify Chinese copy and performance budget for this story

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - [Title] (Priority: P3)

**Goal**: [Brief description of what this story delivers]

**Independent Test**: [How to verify this story works on its own]

### Tests for User Story 3 ⚠️

- [ ] T026 [P] [US3] Unit or contract test for changed behavior in [exact file path]
- [ ] T027 [P] [US3] Integration test for the main user journey in [exact file path]

### Implementation for User Story 3

- [ ] T028 [P] [US3] Implement model or state changes in [exact file path]
- [ ] T029 [US3] Implement service or business logic in [exact file path]
- [ ] T030 [US3] Implement endpoint, page, or interaction in [exact file path]
- [ ] T031 [US3] Verify Chinese copy and performance budget for this story

**Checkpoint**: All user stories should now be independently functional

---

[Add more user story phases as needed, following the same pattern]

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] TXXX [P] Documentation updates in docs/
- [ ] TXXX Code cleanup and simplification
- [ ] TXXX Performance optimization across all impacted stories
- [ ] TXXX [P] Additional automated regression coverage in tests/
- [ ] TXXX Review Chinese terminology consistency across the site
- [ ] TXXX Run quickstart.md or equivalent end-to-end validation

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - May integrate with US1 but should be independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - May integrate with US1/US2 but should be independently testable

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Models or state changes before services
- Services before endpoints or UI wiring
- Code quality checks before final review
- Performance verification before story completion
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch validation tasks for User Story 1 together:
Task: "Unit or contract test for changed behavior in [exact file path]"
Task: "Integration test for the main user journey in [exact file path]"
Task: "Measure and verify performance target for this story"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently, confirm Chinese copy, verify performance budget
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Verify performance/copy → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Verify performance/copy → Deploy/Demo
4. Add User Story 3 → Test independently → Verify performance/copy → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Verify code quality gates and performance budgets before closing a story
- User-facing default output should remain in Chinese unless the spec explicitly states otherwise
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
