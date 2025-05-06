# SmartBank Planning Documentation

## Overview

This directory contains machine-readable planning documents for the SmartBank application's future development. These documents use a standardized checklist format designed for easy parsing and updating by AI agents.

## Available Planning Documents

- [Feature Implementation Plan](./FEATURE_IMPLEMENTATION_PLAN.md): Task ID format F{phase}.{task} (e.g., F1.2)
- [Testing Implementation Plan](./TESTING_IMPLEMENTATION_PLAN.md): Task ID format T{phase}.{task} (e.g., T3.4)

## Document Structure

Each planning document follows this format:

```
- [ ] {TASK_ID}: {Task description} (Priority: {priority}, Effort: {effort})
```

Where:
- STATUS markers: [ ] (Not Started), [P] (In Progress), [R] (Under Review), [X] (Completed)
- TASK_ID: Unique identifier with format {type}{phase}.{number}
- Priority: High, Medium, or Low
- Effort: Estimated in days (e.g., 2d)

## Task Status Update Instructions

To update task status:
1. Locate the task by its TASK_ID
2. Change the status marker: [ ] → [P] → [R] → [X]

Example of updating task F1.2 to "In Progress":
```diff
- [ ] F1.2: Create JPA entities for accounts and transactions (Priority: High, Effort: 2d)
+ [P] F1.2: Create JPA entities for accounts and transactions (Priority: High, Effort: 2d)
```

## Implementation Phases

Both plans are organized into sequential implementation phases:

1. Phase 1: Foundation/infrastructure setup
2. Phase 2: Core functionality implementation
3. Phase 3: Advanced features and integration
4. Phase 4: UI enhancements and refinement
5. Phase 5/6: Performance optimization and final polish

## Dependency Tracking

Tasks may have dependencies on other tasks. Dependencies are indicated by:

```
- [ ] {TASK_ID}: {Task description} (Priority: {priority}, Effort: {effort}, Depends on: {TASK_ID1}, {TASK_ID2})
```

## Automated Progress Tracking

Progress can be automatically calculated by counting tasks in each status:

```
Phase 1 Progress: [X] 3/5 (60%)
```

This format allows AI agents to easily parse, update, and report on project progress.
