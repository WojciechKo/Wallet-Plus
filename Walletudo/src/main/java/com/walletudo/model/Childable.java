package com.walletudo.model;

import java.util.List;

public interface Childable<T> {
    public List<T> getChildren();
}
