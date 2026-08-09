# Versioning and Releases

This service is versioned independently of the other OpenTube services. A backend release does not require a frontend or proxy release, and vice versa.

## Scheme

[Semantic Versioning 2.0.0](https://semver.org): `MAJOR.MINOR.PATCH`.

Git tags are `vX.Y.Z` — annotated, never lightweight. The tag is the source of truth for what was released; the version in `pom.xml` mirrors it and is verified by CI at publish time.

## Deciding the bump

The question that decides the number is **"does the person deploying this have to do anything?"**

| Bump | Trigger |
|---|---|
| **MAJOR** | An environment variable is renamed or newly required; the compose file must change; an API response shape changes such that the frontend or proxy must be updated in step; a data migration is needed |
| **MINOR** | A new endpoint or feature; new optional configuration; anything additive that older clients can ignore |
| **PATCH** | Bug fix, dependency bump, internal refactor, test changes — no observable change to anything consuming the service |

If a change needs a matching release in another service, say so in the release notes: *"requires frontend >= 2.1.0"*. Semver within one service cannot express cross-service compatibility, and with three services a sentence in the notes is enough.

Pre-1.0 rules are deliberately not used here. Once a version is deployed, breaking changes get a MAJOR bump regardless of how small the number is.

## Where the version lives

| Location | Purpose |
|---|---|
| Git tag `vX.Y.Z` | Source of truth. Triggers the publish workflow. |
| `pom.xml` `<version>` | Build metadata. Verified against the tag by CI. |
| `CHANGELOG.md` | Human-readable history of what changed and why. |
| Docker image tags | Derived from the git tag by CI — never set by hand. |

## Docker Hub tags

The image tag is the git tag without the leading `v`. CI derives every tag below; none are pushed manually.

| Git tag | Image tags pushed |
|---|---|
| `v1.2.3` | `1.2.3`, `1.2`, `1`, `latest` |
| `v1.3.0-rc.1` | `1.3.0-rc.1` only |

Pre-releases never receive `latest` or the rolling `1.3` / `1` tags, so nothing pinned to a major or minor line will pick one up by accident.

Images carry `org.opencontainers.image.version` and `org.opencontainers.image.revision` labels, so `docker inspect` on a running container reports the exact version and commit it was built from.

## Branching

`main` is always releasable. Work happens on short-lived branches (`feat/...`, `fix/...`, `refactor/...`) merged into `main` through a pull request. There is no `develop` branch: with a single maintainer and CI on every PR it adds a merge step without adding stability.

Releases are cut from `main` by tagging. A pre-release is a tag, not a branch — tag `v1.3.0-rc.1`, deploy it, and tag `v1.3.0` when satisfied.

## Release procedure

Run from a clean checkout of `main` with everything merged.

```bash
git checkout main && git pull
```

**1. Set the version** — decide the bump using the table above.

```bash
mvn versions:set -DnewVersion=1.2.0 -DgenerateBackupPoms=false
```

**2. Update the changelog.** Rename the `## [Unreleased]` heading to `## [1.2.0] - YYYY-MM-DD`, then add a fresh empty `## [Unreleased]` section above it. Update the link definitions at the bottom of the file.

**3. Commit both together.**

```bash
git commit -am "chore(release): v1.2.0"
```

**4. Tag the release commit.** Annotated, so it carries a date, author and message, and so `git describe` finds it.

```bash
git tag -a v1.2.0 -m "v1.2.0"
```

**5. Push the commit and tag in one step.**

```bash
git push origin main --follow-tags
```

`--follow-tags` pushes the commit along with annotated tags reachable from it, so the two cannot get out of step.

The tag push triggers `.github/workflows/publish.yml`, which verifies the `pom.xml` version matches the tag, builds and pushes the image, and opens a GitHub Release using the matching changelog section as its notes.

### If a release goes wrong

Do not move or delete a pushed tag — anything that already pulled the image will silently diverge. Fix forward with a new PATCH release.

## Keeping the changelog

Add to `## [Unreleased]` as changes merge, not at release time. One line per user-visible change, grouped under `Added`, `Changed`, `Fixed`, `Deprecated`, `Removed` or `Security`.

Writing the line is what tells you the bump: a change nobody deploying needs to know about is a PATCH, and a change you cannot describe without mentioning a config or contract change is a MAJOR.