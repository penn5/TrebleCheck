1. [ ] Check for `TODO` and `STOPSHIP`
2. [ ] Commit and push
3. [ ] Bump the version in `app/version.properties`
4. [ ] Write a changelog on poeditor and tag it with `ignore-string-android` and `fastlane-android`
5. [ ] Run `bundle exec fastlane pre_release`
6. [ ] Verify the screenshots
7. [ ] Commit and tag
8. [ ] Push and push tags
9. [ ] Run `bundle exec fastlane release`

See `fastlane/Fastfile` comments for debugging information on Fastlane crashes.
