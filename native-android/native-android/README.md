# Gondwana Prime Native Android

यह Jetpack Compose आधारित native Android starter है। यह public viewer app का पहला UI build है; Admin Panel और backend अभी existing Gondwana Prime web app पर रहेंगे।

## Open और run

Android Studio में यह folder खोलें, Gradle sync करें और emulator/Android phone पर Run करें।

## बिना local setup के build

इस project में `.github/workflows/build-apk.yml` दिया गया है। Project को GitHub repository में डालकर **Actions → Build Gondwana Prime APK → Run workflow** चलाएँ। Build पूरा होने के बाद `gondwana-prime-debug-apk` artifact से APK डाउनलोड किया जा सकता है।

## अगला integration चरण

- existing HTTPS API client जोड़ना (`/api/entries`, `/api/categories`, `/api/events`, `/api/playback`)
- Google/mobile-password session जोड़ना
- R2 signed video playback
- My List और Continue Watching persistence
- Razorpay/UPI checkout
- release keystore से APK/AAB बनाना

Package name: `com.gondwana.prime`
