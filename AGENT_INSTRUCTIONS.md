# Banko AI Context

## Purpose

This file gives an AI agent a quick overview of where the Banko project code lives, both locally and on GitHub. It is meant to help the agent understand which repositories belong to the Banko ecosystem so it can consider all relevant code when answering questions or making suggestions.

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

## Rule

Always consider all Banko-related repositories together. Do not assume the current repo contains the full context.
Never commit anything if you didn't receive an approval for that.

## Pull Request Format

When creating a Pull Request description, always use the following structure:

## What
- Change 1
- Change 2
- Change 3

## Why
- Reason for Change 1
- Reason for Change 2
- Reason for Change 3

Rules:
- The description must contain exactly two sections: `## What` and `## Why`.
- Both sections must use bullet lists.
- `## What` describes the actual changes introduced by the PR.
- `## Why` explains the motivation, business value, bug fix, technical reason, or user benefit behind each corresponding item in `## What`.
- Every item in `## What` must have a matching item in `## Why` in the same order.
- Focus on intent and impact rather than implementation details.
- Keep each bullet concise and clear.
- Do not include sections such as Summary, Testing, Screenshots, Risks, or Notes unless explicitly requested.
- Write in professional, developer-friendly English.
