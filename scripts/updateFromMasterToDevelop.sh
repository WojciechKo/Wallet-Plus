adb uninstall info.korzeniowski.walletplus

git checkout master
gradle clean assemble
adb install ../WalletPlus/build/outputs/apk/WalletPlus-debug.apk
adb shell am start -n info.korzeniowski.walletplus/.MainActivity

git checkout develop
gradle clean assemble
adb install -r ../WalletPlus/build/outputs/apk/WalletPlus-debug.apk
adb shell am start -n info.korzeniowski.walletplus/.MainActivity
