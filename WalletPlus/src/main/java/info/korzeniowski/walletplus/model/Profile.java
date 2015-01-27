package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;

public class Profile implements Identifiable {

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField
    private String driveId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Account account;

    @DatabaseField(uniqueIndex = true)
    private String name;

    @DatabaseField(canBeNull = false)
    private String databaseFileName;

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

    public String getDatabaseFileName() {
        return databaseFileName;
    }

    public Profile setDatabaseFileName(String databaseFileName) {
        this.databaseFileName = databaseFileName;
        return this;
    }
}
