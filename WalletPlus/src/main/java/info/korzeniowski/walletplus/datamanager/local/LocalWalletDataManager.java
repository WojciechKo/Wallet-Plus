package info.korzeniowski.walletplus.datamanager.local;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.datamanager.local.validation.WalletValidator;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.model.greendao.GreenCategoryDao;
import info.korzeniowski.walletplus.model.greendao.GreenWallet;
import info.korzeniowski.walletplus.model.greendao.GreenWalletDao;

import static com.google.common.base.Preconditions.checkNotNull;

public class LocalWalletDataManager implements WalletDataManager{
    private final GreenWalletDao greenWalletDao;
    WalletValidator walletValidator;

    private final List<Wallet> wallets;
    private final List<Wallet> myWallets;
    private final List<Wallet> contractors;

    @Inject
    public LocalWalletDataManager(GreenWalletDao greenWalletDao) {
        this.greenWalletDao = greenWalletDao;
        this.walletValidator = new WalletValidator(this);
        wallets = getWalletListFromGreenWalletList(greenWalletDao.loadAll());
        myWallets = filter(Wallet.Type.MY_WALLET);
        contractors = filter(Wallet.Type.CONTRACTOR);
    }

    private List<Wallet> filter(final Wallet.Type type) {
        return Lists.newArrayList(Iterables.filter(wallets, new Predicate<Wallet>() {
            @Override
            public boolean apply(Wallet wallet) {
                return type.equals(wallet.getType());
            }
        }));
    }

    private List<Wallet> getWalletListFromGreenWalletList(List<GreenWallet> greenWallets) {
        List<Wallet> wallets = Lists.newArrayList();
        for (GreenWallet greenWallet : greenWallets) {
            wallets.add(getWalletFromGreenWallet(greenWallet));
        }
        return wallets;
    }

    private Wallet getWalletFromGreenWallet(GreenWallet greenWallet) {
        if (greenWallet == null) {
            return null;
        }
        Wallet wallet = new Wallet();
        wallet.setName(greenWallet.getName());
        wallet.setInitialAmount(greenWallet.getInitialAmount());
        wallet.setCurrentAmount(greenWallet.getCurrentAmount());
        wallet.setId(greenWallet.getId());
        wallet.setType(Wallet.Type.values()[greenWallet.getType()]);
        return wallet;
    }

    @Override
    public Long insert(Wallet wallet) {
        checkNotNull(wallet);
        walletValidator.validateInsert(wallet);

        wallet.setId(greenWalletDao.insert(new GreenWallet(wallet)));
        if (wallet.getType().equals(Wallet.Type.CONTRACTOR)) {
            contractors.add(wallet);
        } else if (wallet.getType().equals(Wallet.Type.MY_WALLET)) {
            myWallets.add(wallet);
        }
        wallets.add(wallet);

        return wallet.getId();
    }

    @Override
    public Long count() {
        return greenWalletDao.count();
    }

    @Override
    public Wallet findById(Long id) {
        Wallet found = Wallet.findById(getMyWallets(), id);
        return new Wallet(found);
    }

    @Override
    public List<Wallet> getAll() {
       return Wallet.copyList(wallets);
    }

    @Override
    public void update(Wallet newValue) {
        Wallet toUpdate = Wallet.findById(wallets, newValue.getId());
        walletValidator.validateUpdate(newValue, toUpdate);
        greenWalletDao.update(new GreenWallet(toUpdate));
        updateWalletLists(newValue, toUpdate);
    }

    private void updateWalletLists(Wallet newValue, Wallet toUpdate) {
        toUpdate.setInitialAmount(newValue.getInitialAmount());
        toUpdate.setName(newValue.getName());
    }

    @Override
    public void deleteById(Long id) {
        Wallet walletToDelete = Wallet.findById(wallets, id);
        walletValidator.validateDelete(walletToDelete);
        greenWalletDao.deleteByKey(id);
        removeWalletFromLists(walletToDelete);
    }

    private void removeWalletFromLists(Wallet walletToDelete) {
        if (walletToDelete.getType().equals(Wallet.Type.MY_WALLET)) {
            myWallets.remove(walletToDelete);
        } else if (walletToDelete.getType().equals(Wallet.Type.CONTRACTOR)) {
            contractors.remove(walletToDelete);
        }
        wallets.remove(walletToDelete);
    }

    @Override
    public List<Wallet> getMyWallets() {
        return myWallets;
    }

    @Override
    public List<Wallet> getContractors() {
        return contractors;
    }

    @Override
    public Wallet findByNameAndType(String name, Wallet.Type type) {
        List<GreenWallet> greenWalletList = greenWalletDao.queryBuilder().
                where(GreenCategoryDao.Properties.Name.eq(name)).
                where(GreenCategoryDao.Properties.Type.eq(type.ordinal())).
                build().list();
        if (greenWalletList.isEmpty()) {
            return null;
        }
        return getWalletFromGreenWallet(greenWalletList.get(0));
    }
}
