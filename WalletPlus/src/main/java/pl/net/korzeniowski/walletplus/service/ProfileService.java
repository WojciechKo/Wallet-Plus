package pl.net.korzeniowski.walletplus.service;

import pl.net.korzeniowski.walletplus.model.Profile;

public interface ProfileService extends BaseService<Profile> {
    Profile findByName(String name);

    Profile getActiveProfile();
}
