package info.korzeniowski.walletplus.service;

import info.korzeniowski.walletplus.model.Profile;

public interface ProfileService extends BaseService<Profile> {
    Profile findByName(String name);
}
