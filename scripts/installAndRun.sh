gradle assemble --offline
adb install -r ../WalletPlus/build/outputs/apk/WalletPlus-debug.apk
adb shell am start -n info.korzeniowski.walletplus/.MainActivity
