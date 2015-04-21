gradle assemble --offline
adb install -r ../WalletUDo/build/outputs/apk/WalletUDo-debug.apk
adb shell am start -n com.walletudo/.DashboardActivity
