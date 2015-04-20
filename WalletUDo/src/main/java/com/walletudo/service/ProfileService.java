package com.walletudo.service;

import com.walletudo.model.Profile;

public interface ProfileService extends BaseService<Profile> {
    Profile findByName(String name);

    Profile getActiveProfile();

    void actualProfileHasChanged();
}
