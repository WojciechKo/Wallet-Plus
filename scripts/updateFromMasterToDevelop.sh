adb uninstall com.walletudo

git checkout master
gradle clean assemble
adb install ../WalletUDo/build/outputs/apk/WalletUDo-debug.apk
adb shell am start -n com.walletudo/.DashboardActivity

git checkout develop
gradle clean assemble
adb install -r ../WalletUDo/build/outputs/apk/WalletUDo-debug.apk
adb shell am start -n com.walletudo/.DashboardActivity
