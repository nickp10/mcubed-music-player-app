# mcubed-music-player-app

Inactive
----
**Note:** This project is not actively being worked on.

Description
----
A simple music player app built for Android.

Creating Releases
----
1. Ensure these steps are up to date.
1. Ensure all copyright dates are up to date. **Note:** There are currently no copyrights.
1. Update strings.xml so app_version and app_name_version contain the new version.
1. Update strings.xml so about_changelog_text contains details about the changes.
1. Update AndroidManifest.xml so android:versionCode is incremented by 1.
1. Update AndroidManifest.xml so android:versionName matches the app_version value in strings.xml.
1. Update strings.xml (in the Test project) so app_version contains the new version.
1. Update App.java so that the upgradeApp method performs the necessary upgrade from the previous version.
1. Run all unit tests.
1. Commit all changes.
1. Export mCubed as Android Application.
1. Use existing keystore with password (mCubed3Key).
1. Save as mCubed.apk.
1. Upload to GitHub as a new release.
1. Upload to Google Play.
