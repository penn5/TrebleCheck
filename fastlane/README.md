fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android build_and_screengrab

```sh
[bundle exec] fastlane android build_and_screengrab
```

Build debug and test APK for screenshots

### android pre_release

```sh
[bundle exec] fastlane android pre_release
```

Import translations and generate screenshots

### android release

```sh
[bundle exec] fastlane android release
```

Deploy a new version

### android upload_to_play

```sh
[bundle exec] fastlane android upload_to_play
```



### android release_to_gitlab

```sh
[bundle exec] fastlane android release_to_gitlab
```



----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
