package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@DatabaseTable(tableName = CashFlow.TABLE_NAME)
public class CashFlow implements Identifiable {

    public static final String TABLE_NAME = "cashFlow";

    public static final String ID_COLUMN_NAME = "id";
    public static final String TYPE_COLUMN_NAME = "type";
    public static final String AMOUNT_COLUMN_NAME = "amount";
    public static final String WALLET_ID_COLUMN_NAME = "wallet_id";
    public static final String COMMENT_COLUMN_NAME = "comment";
    public static final String DATETIME_COLUMN_NAME = "dateTime";
    public static final String COMPLETED_COLUMN_NAME = "completed";

    @DatabaseField(columnName = ID_COLUMN_NAME, generatedId = true)
    private Long id;

    @DatabaseField(columnName = TYPE_COLUMN_NAME, canBeNull = false)
    private Type type;

    @DatabaseField(columnName = AMOUNT_COLUMN_NAME, canBeNull = false)
    private Double amount;

    @DatabaseField(columnName = WALLET_ID_COLUMN_NAME, foreign = true, foreignAutoRefresh = true,
            columnDefinition = "integer REFERENCES " + Wallet.TABLE_NAME +
                    "(" + Wallet.ID_COLUMN_NAME + ") ON DELETE CASCADE")
    private Wallet wallet;

    @DatabaseField(columnName = COMMENT_COLUMN_NAME)
    private String comment;

    @DatabaseField(columnName = DATETIME_COLUMN_NAME, canBeNull = false)
    private Date dateTime;

    @DatabaseField(columnName = COMPLETED_COLUMN_NAME, canBeNull = false)
    private boolean completed;

    private Set<Tag> tags;

    public CashFlow() {
        tags = new TreeSet<>(new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }

    @Override
    public Long getId() {
        return id;
    }

    public CashFlow setId(Long id) {
        this.id = id;
        return this;
    }

    public Type getType() {
        return type;
    }

    public CashFlow setType(Type type) {
        this.type = type;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public CashFlow setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public CashFlow setWallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public CashFlow setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public CashFlow setDateTime(Date dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public Boolean isCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public CashFlow addTag(Collection<? extends Tag> categories) {
        this.tags.addAll(categories);
        return this;
    }

    public CashFlow addTag(Tag tag) {
        tags.add(tag);
        return this;
    }

    public CashFlow removeTag(Tag tag) {
        tags.remove(tag);
        return this;
    }

    public CashFlow clearTags() {
        tags.clear();
        return this;
    }

    public List<Tag> getTags() {
        return new ArrayList<Tag>(tags);
    }

    public enum Type {
        INCOME,
        EXPANSE,
        TRANSFER
    }
}
