package com.walletudo.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.walletudo.service.ormlite.ProfileDatabaseHelper;

@DatabaseTable(tableName = Profile.TABLE_NAME)
public class Profile implements Identifiable {

    public static final String TABLE_NAME = "profile";

    public static final String ID_COLUMN_NAME = "id";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String DATABASE_FILE_NAME_COLUMN_NAME = "databaseFileName";
    public static final String GOOGLE_ACCOUNT_COLUMN_NAME = "googleAccount";
    public static final String GOOGLE_TOKEN_COLUMN_NAME = "googleToken";
    public static final String DRIVE_ID_COLUMN_NAME = "driveId";
    public static final String SYNCHRONIZED_COLUMN_NAME = "synchronized";

    @DatabaseField(columnName = ID_COLUMN_NAME, generatedId = true)
    private Long id;

    @DatabaseField(columnName = NAME_COLUMN_NAME, uniqueIndex = true)
    private String name;

    @DatabaseField(columnName = DATABASE_FILE_NAME_COLUMN_NAME, canBeNull = false)
    private String databaseFileName;

    @DatabaseField(columnName = GOOGLE_ACCOUNT_COLUMN_NAME)
    private String googleAccount;

    @DatabaseField(columnName = GOOGLE_TOKEN_COLUMN_NAME)
    private String googleToken;

    @DatabaseField(columnName = DRIVE_ID_COLUMN_NAME)
    private String driveId;

    @DatabaseField(columnName = SYNCHRONIZED_COLUMN_NAME, canBeNull = false)
    private boolean synchronize;

    @Override
    public Long getId() {
        return id;
    }

    public Profile setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Profile setName(String name) {
        this.name = name;
        this.databaseFileName = ProfileDatabaseHelper.PROFILE_DATABASE_PREFIX + name;
        return this;
    }

    public String getDatabaseFileName() {
        return databaseFileName;
    }

    public String getGoogleAccount() {
        return googleAccount;
    }

    public Profile setGoogleAccount(String googleAccount) {
        this.googleAccount = googleAccount;
        return this;
    }

    public String getGoogleToken() {
        return googleToken;
    }

    public Profile setGoogleToken(String googleToken) {
        this.googleToken = googleToken;
        return this;
    }

    public String getDriveId() {
        return driveId;
    }

    public Profile setDriveId(String driveId) {
        this.driveId = driveId;
        return this;
    }

    public Boolean isSynchronized() {
        return synchronize;
    }

    public Profile setSynchronized(Boolean synchronize) {
        this.synchronize = synchronize;
        return this;
    }
}
