To create a new release:
  1. Ensure these steps are up to date.
  2. Ensure all copyright dates are up to date.
     - There are currently no copyrights.
  3. Update strings.xml so app_version and app_name_version contain the new version.
  4. Update strings.xml so about_changelog_text contains details about the changes.
  5. Update AndroidManifest.xml so android:versionCode is incremented by 1.
  6. Update strings.xml (in the Test project) so app_version contains the new version.
  7. Update AndroidManifest.xml (in the Test project) so android:versionCode is incremented by 1.
  8. Update App.java so that the upgradeApp method performs the necessary upgrade from the previous version.
  9. Run all unit tests through Eclipse and through the "run coverage" script to make sure both work.
  10. Commit all changes.
  11. Export mCubed as Android Application.
  12. Use existing keystore with password (mCubed3Key).
  13. Save as mCubed.apk, commit it to the Releases directory, and upload to Google Play.