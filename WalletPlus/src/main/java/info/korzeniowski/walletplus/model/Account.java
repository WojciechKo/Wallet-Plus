package info.korzeniowski.walletplus.model;

import com.google.common.collect.Lists;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.List;

public class Account implements Identifiable {

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField(canBeNull = false, uniqueIndex = true)
    private String name;

    @DatabaseField
    private String gmailAccount;

    @ForeignCollectionField(orderColumnName = "name")
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
