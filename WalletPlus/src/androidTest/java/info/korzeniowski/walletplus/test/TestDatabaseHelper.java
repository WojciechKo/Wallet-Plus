package info.korzeniowski.walletplus.test;

import android.content.Context;

import info.korzeniowski.walletplus.service.local.DatabaseHelper;

public class TestDatabaseHelper extends DatabaseHelper {

    public TestDatabaseHelper(Context context) {
        super(context, null);
    }
}
