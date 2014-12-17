package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;

public class Account implements Identifiable {

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false, uniqueIndex = true)
    private String name;

    @DatabaseField
    private String gmailAccount;

    @DatabaseField
    private String databaseFileName;

    public Account() {

    }

    public Account(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Account setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Account setName(String name) {
        this.name = name;
        return this;
    }

    public String getGmailAccount() {
        return gmailAccount;
    }

    public Account setGmailAccount(String gmailAccount) {
        this.gmailAccount = gmailAccount;
        return this;
    }

    public String getDatabaseFileName() {
        return databaseFileName;
    }

    public Account setDatabaseFileName(String databaseFileName) {
        this.databaseFileName = databaseFileName;
        return this;
    }
}
