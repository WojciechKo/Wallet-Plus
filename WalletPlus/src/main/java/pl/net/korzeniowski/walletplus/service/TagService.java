package pl.net.korzeniowski.walletplus.service;

import pl.net.korzeniowski.walletplus.model.Tag;

public interface TagService extends BaseService<Tag> {

    Tag findByName(String name);
}
