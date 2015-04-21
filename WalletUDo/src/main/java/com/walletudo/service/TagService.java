package com.walletudo.service;

import com.walletudo.model.Tag;

public interface TagService extends BaseService<Tag> {

    Tag findByName(String name);
}
