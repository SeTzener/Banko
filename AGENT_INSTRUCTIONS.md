# Banko AI Context

## Purpose

This file gives an AI agent a quick overview of where the Banko project code lives, both locally and on GitHub. It is meant to help the agent understand which repositories belong to the Banko ecosystem so it can consider all relevant code when answering questions, making suggestions, or implementing changes.

---

## Local Paths

* Banko Mobile:
  `../Banko`

* Banko API:
  `../BankoApi`

---

## GitHub Repositories

* Banko Mobile:
  https://github.com/setzener/Banko

* Banko API:
  https://github.com/setzener/BankoApi

---

## Repository Rules

### Repository Awareness

* Always consider all Banko-related repositories together.
* Do not assume the current repository contains the full context.
* Review relevant code and architecture across both the Mobile and API repositories when possible.
* Consider cross-repository impacts when proposing or implementing changes.
* Ensure solutions remain consistent with the overall Banko architecture.

### Git Operations

* Never commit changes without explicit approval.
* Never push, merge, rebase, squash, tag, or modify Git history without explicit approval.
* If proposing commits, present the proposed commit structure first and wait for confirmation.

### Commit Quality

* Create clear, human-readable commit histories.
* Use focused commits that represent logical units of work.
* Avoid large commits containing unrelated changes.
* Write commit messages that explain the intent of the change.
* Structure commits so reviewers can understand the evolution of the work by reading the commit history.

### Code Style

#### No Loose Strings

* Avoid hardcoded string literals ("magic strings").
* Use enums, sealed classes, constants, value classes, or other dedicated types whenever appropriate.
* Represent domain concepts with types rather than raw strings.
* Prefer type-safe comparisons over string comparisons.

#### UI Strings (Banko Mobile Only)

* Any text that can be displayed to the user must be stored in `Res.string`.
* UI-facing strings must never be hardcoded in composables, view models, or business logic.

### Feedback and Decision Making

* Do not default to agreement.
* Challenge assumptions when appropriate.
* If a proposed solution has weaknesses, risks, tradeoffs, or better alternatives, explain them.
* Prioritize correctness, maintainability, scalability, and long-term value over validation.
* When asked for an opinion, provide an honest assessment supported by reasoning.

---

## Pull Request Format

When creating a Pull Request description, always use the following structure:

## What

* Change 1
* Change 2
* Change 3

## Why

* Reason for Change 1
* Reason for Change 2
* Reason for Change 3

### PR Rules

* The description must contain exactly two sections: `## What` and `## Why`.
* Both sections must use bullet lists.
* `## What` describes the actual changes introduced by the PR.
* `## Why` explains the motivation, business value, bug fix, technical reason, or user benefit behind each corresponding item in `## What`.
* Every item in `## What` must have a matching item in `## Why` in the same order.
* Focus on intent and impact rather than implementation details.
* Keep each bullet concise and clear.
* Do not include sections such as Summary, Testing, Screenshots, Risks, or Notes unless explicitly requested.
* Write in professional, developer-friendly English.
