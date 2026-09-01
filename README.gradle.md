# Gradle development build

The Soong build remains the source of truth for UotanOS system images. The Gradle build reuses
the production CatShare sources and includes `uwu-sdk/uwuCompose` as the `:uwu-compose` source
module for Android Studio indexing and Compose previews.

Open this directory in Android Studio or build the development APK:

```bash
./gradlew :app:assembleDebug
```

The Gradle build uses `app/src/main/AndroidManifest.xml` and generates its own `BuildConfig`. The
Soong build instead uses `app/src/platform/AndroidManifest.xml` and the platform-only
`app/src/platform/java` sources. The debug application ID is `moe.reimu.catshare.debug`; it is not
a privileged platform-signed app, so functionality requiring `LOCAL_MAC_ADDRESS`, `READ_LOGS`, or
other privileged access cannot be exercised from the development APK.

The `:uwu-compose` module references `uwu-sdk/uwuCompose` from the surrounding AOSP checkout, so
this directory must remain at `packages/apps/CatShare`.
