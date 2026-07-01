# Publishing To Maven Central

This repository is prepared to publish the library artifacts to Maven Central through the Sonatype Central Publisher Portal.

## Release Inputs

- The release script reads `revision` from the root `pom.xml` by default.
- Pass `-Revision` only if you need to override the version manually.
- The release script uses the `release-assets` and `central-release` Maven profiles.

## Required Secrets

Set these environment variables before running the release script:

- `MAVEN_CENTRAL_USERNAME`
- `MAVEN_CENTRAL_TOKEN`
- `GPG_PRIVATE_KEY`
- `GPG_PASSPHRASE`

## What Gets Published

The following modules are prepared for deployment:

- `a2a-spring-boot-server-autoconfigure`
- `a2a-spring-boot-server-rest`
- `a2a-spring-boot-starter-server-rest`

The release script publishes these three modules in one reactor build and resolves their dependencies through the same release run.

Example applications, integration tests, SUT modules, internal aggregators, and the root parent are not published in this phase.

## Release Command

```bash
powershell -ExecutionPolicy Bypass -File .\scripts\publish-to-maven-central.ps1
```

To override the version explicitly:

```bash
powershell -ExecutionPolicy Bypass -File .\scripts\publish-to-maven-central.ps1 -Revision 0.1.0
```

## Notes

- The release build attaches sources and Javadoc jars.
- The release build signs artifacts with GPG.
- The release script is meant to run on Windows PowerShell.
- The namespace must be verified in Sonatype Central before publication will succeed.

## GitHub Releases

GitHub Releases are separate from Maven Central publication.

The release workflow runs `verify` and publishes the same library jars plus their `sources` and `javadoc` jars as release assets, so the repository has both:

- Maven Central coordinates for consumers;
- GitHub Release downloads for people who prefer a direct artifact.
