# SmartBank Feature Implementation Guidelines

This document outlines the guidelines and standards for implementing new features in the SmartBank application. These guidelines should be followed for all feature development work.

## Task Format

Each task in the implementation plan follows this format:
- `[STATUS]` TASK_ID: Task description (Priority: PRIORITY, Effort: EFFORT)

Where:
- STATUS is one of: [ ] (Not Started), [P] (In Progress), [R] (Under Review), [X] (Completed)
- TASK_ID is a unique identifier for the task (e.g., F1.1 = Feature, Phase 1, Task 1)
- PRIORITY is High, Medium, or Low
- EFFORT is estimated in days

## Branching Strategy

- [ ] G1.1: Create feature branches directly from `main` branch
- [ ] G1.2: Use naming format: `feature/F{phase}.{task}-short-description`
- [ ] G1.3: Submit pull requests to `main` when feature is complete
- [ ] G1.4: Merge to `main` after code review and approval

## Commit Guidelines

- [ ] G2.1: Use descriptive commit messages
- [ ] G2.2: Reference task IDs in commits (e.g., "Implements F1.1: Database integration")
- [ ] G2.3: Keep commits focused on single changes

## Documentation Requirements

- [ ] G3.1: Update JavaDoc for all new classes and methods
- [ ] G3.2: Update README.md with new features
- [ ] G3.3: Create user documentation for complex features

## Definition of Done

- [ ] G4.1: Feature implemented according to requirements
- [ ] G4.2: Unit tests written and passing
- [ ] G4.3: Code reviewed by at least one team member
- [ ] G4.4: Documentation updated
- [ ] G4.5: Feature demonstrated and accepted

## Dependency Management

- [ ] G5.1: Document all new dependencies in build.gradle
- [ ] G5.2: Use specific version numbers for dependencies
- [ ] G5.3: Check for security vulnerabilities in dependencies

## Code Style

- [ ] G6.1: Follow Java code style conventions
- [ ] G6.2: Use consistent naming conventions
- [ ] G6.3: Include appropriate comments
- [ ] G6.4: Keep methods small and focused
- [ ] G6.5: Follow SOLID principles
