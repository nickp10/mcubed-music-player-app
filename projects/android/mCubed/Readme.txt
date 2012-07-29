To create a new release:
  1. Ensure these steps are up to date.
  2. Update strings.xml so app_version and app_name_version contain the new version.
  3. Update strings.xml so about_changelog_text contains details about the changes.
  4. Update AndroidManifest.xml so android:versionCode is incremented by 1.
  5. Update strings.xml (in the Test project) so app_version contains the new version.
  6. Update AndroidManifest.xml (in the Test project) so android:versionCode is incremented by 1.
  7. Commit all changes.
  8. Export mCubed as Android Application.
  9. Use existing keystore with password (mCubed3Key).
  10. Save as mCubed.apk and upload to Google Play.