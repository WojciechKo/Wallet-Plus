package info.korzeniowski.walletplus.service;

import info.korzeniowski.walletplus.model.Tag;

public interface TagService extends BaseService<Tag> {

    Tag findByName(String name);
}
