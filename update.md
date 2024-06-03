# Update checklist

This is for me when releasing updates, because otherwise I forget things

1. Update version in build.gradle.kts & commit
2. Create tag for update (e.g. 2.6.0)
3. Push tag & ver bump commit (ensure to select checkbox to push tag)
4. Merge dev -> master
5. Create release (draft release notes in release)
    * Use github compare for reference: https://github.com/ajgeiss0702/ajQueue/compare/2.5.0...2.6.0
6. Double-check release notes
7. Release ajQueuePlus on Spigot & Polymart (copy-paste release notes from github release)
8. Release ajQueue (free) on
    * Modrinth
    * Hangar
    * Polymart

All plugin pages:
 - Spigot (+)
 - Spigot (free)
 - Polymart (+)
 - Polymart (free)
 - Modrinth (free)
 - Hangar (free)