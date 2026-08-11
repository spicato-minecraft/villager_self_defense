# Release changelog template

Copy this file to `releases/{minecraft_version}-{mod_version}.md` before creating a GitHub Release.

Write for players, not developers. List **only what changed since the previous released version** — not what the mod already does.

---

## Example (feature release)

```markdown
- Villagers fight back when attacked repeatedly by players or hostile mobs
- Fixed crash when opening the villager trade screen on Minecraft 1.21.11
```

## Example (Minecraft version port, no behavior change)

```markdown
- Minecraft 1.21.11 support
```

## Example (first Modrinth release)

```markdown
- Initial release for Minecraft 1.21.11
```

## Guidelines

- Use plain language ("villagers" not "Villager entity")
- One bullet per player-visible change **new in this release**
- Do not restate existing mod features from README or player guides
- Omit internal refactors, test additions, and CI changes unless players notice them
- Do not copy PR titles or commit messages
