package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Profile.TABLE_NAME)
public class Profile implements Identifiable {

    public static final String TABLE_NAME = "profile";

    public static final String ID_COLUMN_NAME = "id";
    public static final String DRIVE_ID_COLUMN_NAME = "driveId";
    public static final String ACCOUNT_ID_COLUMN_NAME = "account_id";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String DATABASE_FILE_PATH_COLUMN_NAME = "databaseFilePath";

    @DatabaseField(columnName = ID_COLUMN_NAME, generatedId = true)
    private Long id;

    @DatabaseField(columnName = DRIVE_ID_COLUMN_NAME)
    private String driveId;

    @DatabaseField(columnName = ACCOUNT_ID_COLUMN_NAME, foreign = true, foreignAutoRefresh = true)
    private Account account;

    @DatabaseField(columnName = NAME_COLUMN_NAME, uniqueIndex = true)
    private String name;

    @DatabaseField(columnName = DATABASE_FILE_PATH_COLUMN_NAME, canBeNull = false)
    private String databaseFilePath;

    @Override
    public Long getId() {
        return id;
    }

    public Profile setId(Long id) {
        this.id = id;
        return this;
    }

    public String getDriveId() {
        return driveId;
    }

    public Profile setDriveId(String driveId) {
        this.driveId = driveId;
        return this;
    }

    public Account getAccount() {
        return account;
    }

    public Profile setAccount(Account account) {
        this.account = account;
        return this;
    }

    public String getName() {
        return name;
    }

    public Profile setName(String name) {
        this.name = name;
        return this;
    }

    public String getDatabaseFilePath() {
        return databaseFilePath;
    }

    public Profile setDatabaseFilePath(String databaseFilePath) {
        this.databaseFilePath = databaseFilePath;
        return this;
    }
}
