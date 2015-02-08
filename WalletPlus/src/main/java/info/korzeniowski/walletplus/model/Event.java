package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;

public class Event {
    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField
    private String name;
}
