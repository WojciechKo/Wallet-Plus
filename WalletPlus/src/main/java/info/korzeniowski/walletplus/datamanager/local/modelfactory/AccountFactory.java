package info.korzeniowski.walletplus.datamanager.local.modelfactory;

import com.google.common.collect.Lists;

import java.util.List;

import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.model.greendao.GreenAccount;

public class AccountFactory {

    private static Account createAccount(GreenAccount greenAccount) {
        if (greenAccount == null) {
            return null;
        }

        Account newAccount = new Account();
        newAccount.setId(greenAccount.getId());
        newAccount.setName(greenAccount.getName());
        newAccount.setPasswordHash(greenAccount.getPasswordHash());

        return newAccount;
    }

    public static GreenAccount createGreenAccount(Account account) {
        if (account == null) {
            return null;
        }

        GreenAccount greenAccount = new GreenAccount();
        greenAccount.setId(account.getId());
        greenAccount.setName(account.getName());
        greenAccount.setPasswordHash(account.getPasswordHash());

        return greenAccount;
    }

    public static List<Account> createAccountList(List<GreenAccount> originalList) {
        if (originalList == null) {
            return null;
        }

        List<Account> result = Lists.newArrayListWithCapacity(originalList.size());
        for(GreenAccount greenAccount : originalList) {
            result.add(createAccount(greenAccount));
        }

        return result;
    }
}
