package info.korzeniowski.walletplus.datamanager.local.modelfactory;

import com.google.common.collect.Lists;

import java.util.List;

import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.model.greendao.GreenWallet;

public class WalletFactory {
    static public Wallet createWallet(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        Wallet newWallet = new Wallet();
        newWallet.setId(wallet.getId());
        newWallet.setName(wallet.getName());
        newWallet.setInitialAmount(wallet.getInitialAmount());
        newWallet.setCurrentAmount(wallet.getCurrentAmount());
        newWallet.setType(wallet.getType());

        return newWallet;
    }

    static public GreenWallet createGreenWallet(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        GreenWallet greenWallet = new GreenWallet();
        greenWallet.setId(wallet.getId());
        greenWallet.setName(wallet.getName());
        greenWallet.setType(wallet.getType().ordinal());
        greenWallet.setInitialAmount(wallet.getInitialAmount());
        greenWallet.setCurrentAmount(wallet.getCurrentAmount());

        return greenWallet;
    }

    public static List<Wallet> createWalletList(List<Wallet> wallets) {
        List<Wallet> copy = Lists.newArrayListWithExpectedSize(wallets.size());
        for(Wallet wallet : wallets) {
            copy.add(createWallet(wallet));
        }
        return copy;
    }
}
