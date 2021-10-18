fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew install fastlane`

# Available Actions
## Android
### android build_and_screengrab
```
fastlane android build_and_screengrab
```
Build debug and test APK for screenshots
### android pre_release
```
fastlane android pre_release
```
Import translations and generate screenshots
### android release
```
fastlane android release
```
Deploy a new version to the Google Play
### android upload_to_play
```
fastlane android upload_to_play
```

### android upload_to_gitlab
```
fastlane android upload_to_gitlab
```


----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
