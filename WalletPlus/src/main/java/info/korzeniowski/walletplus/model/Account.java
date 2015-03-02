package info.korzeniowski.walletplus.model;

import com.google.common.collect.Lists;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.List;

@DatabaseTable(tableName = Account.TABLE_NAME)
public class Account implements Identifiable {

    public static final String TABLE_NAME = "account";

    public static final String ID_COLUMN_NAME = "id";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String GMAIL_ACCOUNT_COLUMN_NAME = "gmailAccount";

    @DatabaseField(columnName = ID_COLUMN_NAME, generatedId = true)
    private Long id;

    @DatabaseField(columnName = NAME_COLUMN_NAME, canBeNull = false, uniqueIndex = true)
    private String name;

    @DatabaseField(columnName = GMAIL_ACCOUNT_COLUMN_NAME)
    private String gmailAccount;

    @ForeignCollectionField(orderColumnName = NAME_COLUMN_NAME)
    private ForeignCollection<Profile> profiles;

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

    public List<Profile> getProfiles() {
        if (profiles == null) {
            return Lists.newArrayList();
        }
        return Lists.newArrayList(profiles);
    }
}
