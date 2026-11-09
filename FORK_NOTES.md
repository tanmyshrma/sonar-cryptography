# Fork notes: sonar-cryptography `1.7.0-qresil.1`

This is a fork of [cbomkit/sonar-cryptography](https://github.com/cbomkit/sonar-cryptography) maintained for the
q_resil CBOM tool. It stays Apache-2.0 (see `LICENSE.txt`); all upstream copyright notices are kept.

- **Upstream base:** tag `1.7.0` (`d1d1b8f`, "release 1.7.0").
- **Branch carrying the patch:** `q_resil/phase4-thread-safety`
- **Published coordinates:** `com.ibm:sonar-cryptography-plugin:1.7.0-qresil.1` (plus the parent and sibling modules
  `com.ibm:{sonar-cryptography,common,engine,mapper,output,enricher,rules,java,python,go,csharp}:1.7.0-qresil.1`) on GitHub Packages
  (`https://maven.pkg.github.com/tanmyshrma/sonar-cryptography`)
- **Companion:** [tanmyshrma/cbomkit-lib](https://github.com/tanmyshrma/cbomkit-lib) `1.3.0-qresil.1`, which depends on this plugin.

## What differs from upstream

1. **`JavaAggregator` is safe for concurrent module scans** (`java/src/main/java/com/ibm/plugin/JavaAggregator.java`).
   Upstream keeps one static `ILanguageSupport` (a stateful call-stack tracker that was never designed for concurrent visitors)
   and one static unsynchronized `ArrayList`. Here `javaLanguageSupport` is a `ThreadLocal`, `detectedNodes` is a synchronized list,
   `getDetectedNodes()` returns a copy, and `reset()` clears both. Single-threaded callers see the same behavior as before.
   This is what lets the companion `cbomkit-lib` fork scan several Java modules at once without corrupting or losing detections.
2. **Build housekeeping needed to build on current JDKs:** every module's version is `1.7.0-qresil.1`,
   `google-java-format` is `1.30.0` (the reformat of `Utils.distinctByKey`'s annotation order is that formatter's output), and the
   distribution repository points at `tanmyshrma/sonar-cryptography`.

No detection rules were changed.

## Using it

See the companion fork's `FORK_NOTES.md`; consumers normally only declare `org.pqca:cbomkit-lib:1.3.0-qresil.1`, and this plugin
resolves transitively from the same GitHub Packages login (a `<server><id>github</id>` entry with a `read:packages` token).

## Cutting a new version

GitHub Packages rejects re-publishing an existing version, so bump the version in every `pom.xml` (the parent and all modules,
for example with `mvn versions:set -DnewVersion=1.7.0-qresil.2`), commit, then publish a GitHub Release or run the
**Publish package to GitHub Packages** workflow manually on the `q_resil/phase4-thread-safety` branch.
Then update `sonar.crypto.plugin.version` in the companion `cbomkit-lib` fork.

## Rebasing on a newer upstream

Merge the new upstream tag into `q_resil/phase4-thread-safety`, re-apply the `JavaAggregator` change if upstream touched it, and rebuild.
If upstream makes `JavaAggregator` thread-safe, drop this fork and point the `cbomkit-lib` fork back at the upstream plugin.
