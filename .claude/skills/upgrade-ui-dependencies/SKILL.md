---
name: upgrade-ui-dependencies
description: Upgrade the Next.js frontend (`ui/`) dependencies to latest stable versions using pnpm, respecting cross-package version locks, then verify codegen/lint/build.
user-invocable: true
---

# Upgrade UI Dependencies

This skill upgrades the `ui/` Next.js frontend dependency versions. It is the frontend counterpart to `/upgrade-deps` (which covers Gradle/Docker and never touches `ui/`).

All commands run from the `ui/` directory.

## Ground Rules

- **Package manager is pnpm.** Netlify builds with `pnpm build` (see `ui/netlify.toml`) and `ui/pnpm-lock.yaml` is the live lockfile. Use `pnpm`, never `npm`.
- **Never run `npm install`.** A `package-lock.json` is not used by the build, drifts from reality, and shadows the real lockfile. If one appears, delete it.
- **Some packages are pinned exactly (no caret) on purpose.** `next`, `react`, `react-dom`, and `eslint-config-next` are exact-pinned because they move together. Keep the exact-pin style when bumping them.

## Phase 1: Discover Current Versions

Read `ui/package.json` `dependencies` and `devDependencies`. Note the installed toolchain:

```bash
node -v          # currently Node 22.x; there is no .nvmrc — Netlify uses its default
pnpm -v          # currently pnpm 10.x
```

List what is behind:

```bash
pnpm outdated
```

`pnpm outdated` shows current → wanted (within range) → latest. The "latest" column is what this skill targets.

## Phase 2: Check Latest Stable Versions

Verify latest stable releases. Prefer the npm registry / official release notes over guessing, and **read changelogs for major bumps** — Next.js and Tailwind majors carry breaking changes.

Key sources:
- Next.js: https://github.com/vercel/next.js/releases
- React: https://github.com/facebook/react/releases
- Tailwind CSS v4: https://tailwindcss.com/blog
- Radix UI: https://www.radix-ui.com/primitives/docs/overview/releases
- urql / @urql/next: https://github.com/urql-graphql/urql/releases
- GraphQL Code Generator: https://github.com/dotansimha/graphql-code-generator/releases
- shadcn: https://ui.shadcn.com/docs/changelog
- TypeScript: https://github.com/microsoft/TypeScript/releases

## Phase 3: Cross-Package Consistency Locks

These groups **must** be bumped together to the matching version, or the build breaks subtly:

| Group | Packages | Rule |
|-------|----------|------|
| Next.js | `next`, `eslint-config-next` | Same exact version. |
| React | `react`, `react-dom` | Same exact version. |
| React types | `@types/react`, `@types/react-dom` | Major must match React major (React 19 → `@types/react@^19`). |
| Tailwind | `tailwindcss`, `@tailwindcss/postcss` | Same major/minor (both v4). PostCSS plugin tracks the core version. |
| GraphQL Codegen | `@graphql-codegen/cli`, `@graphql-codegen/client-preset` | Bump together; the preset must be compatible with the CLI major. |

`graphql` itself (peer of urql + codegen) should stay on a version all three accept — check peer ranges before a major bump.

## Phase 4: Apply Updates

Edit `ui/package.json` directly to set the new versions, preserving each entry's existing range style (exact pin vs `^`). Then refresh the lockfile and install:

```bash
pnpm install
```

For a quick interactive pass you may use `pnpm up --latest` (respects `package.json` edits), but for major bumps prefer editing versions explicitly so the consistency locks in Phase 3 stay correct.


## Phase 5: Verify

Run the full frontend pipeline in order — each must pass:

```bash
pnpm install          # lockfile resolves cleanly, no peer-dep errors
pnpm codegen          # GraphQL types regenerate (codegen.ts)
pnpm lint             # eslint (flat config, eslint.config.mjs)
pnpm build            # next build — the real gate; matches Netlify
```

If a major bump (Next.js, Tailwind, React) breaks the build, consult that release's upgrade guide and apply codemods (e.g. `npx @next/codemod@latest`) before retrying. Note breaking changes to the user rather than silently working around them.

## Phase 6: Documentation Review

Update version references if the upgrade changed anything documented:

- `ui/README.md` — any pinned versions or setup notes
- Root `CLAUDE.md` — the frontend row of the dependency table and the Prerequisites section

### Checklist
- [ ] `next` and `eslint-config-next` are identical versions
- [ ] `react` and `react-dom` are identical versions
- [ ] `@types/react*` majors match the React major
- [ ] `tailwindcss` and `@tailwindcss/postcss` share a major
- [ ] `graphql` satisfies urql + codegen peer ranges
- [ ] `pnpm build` passes
- [ ] no `package-lock.json` was created
