To create a new release:
  1. Ensure these steps are up to date.
  2. Ensure all copyright dates are up to date.
     - There are currently no copyrights.
  3. Update strings.xml so app_version and app_name_version contain the new version.
  4. Update strings.xml so about_changelog_text contains details about the changes.
  5. Update AndroidManifest.xml so android:versionCode is incremented by 1.
  6. Update AndroidManifest.xml so android:versionName matches the app_version value in strings.xml.
  7. Update strings.xml (in the Test project) so app_version contains the new version.
  8. Update AndroidManifest.xml (in the Test project) so android:versionCode is incremented by 1.
  9. Update AndroidManifest.xml (in the Test project) so android:versionName matches the app_version value in strings.xml (in the Test project).
  10. Update App.java so that the upgradeApp method performs the necessary upgrade from the previous version.
  11. Run all unit tests through Eclipse and through the "run coverage" script to make sure both work.
  12. Commit all changes.
  13. Export mCubed as Android Application.
  14. Use existing keystore with password (mCubed3Key).
  15. Save as mCubed.apk, commit it to the Releases directory, and upload to Google Play.