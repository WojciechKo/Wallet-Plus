package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CashFlow implements Identifiable {

    @DatabaseField(generatedId = true)
    private Long id;

    @DatabaseField
    private Type type;

    @DatabaseField(canBeNull = false)
    private Double amount;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnDefinition = "integer REFERENCES wallet(id) ON DELETE CASCADE")
    private Wallet wallet;

    @DatabaseField
    private String comment;

    @DatabaseField(canBeNull = false)
    private Date dateTime;

    @DatabaseField(canBeNull = false)
    private boolean completed;

    private Set<Category> categories;

    public CashFlow() {
        categories = new TreeSet<>(new Comparator<Category>() {
            @Override
            public int compare(Category lhs, Category rhs) {
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

    public CashFlow addCategory(Collection<? extends Category> categories) {
        this.categories.addAll(categories);
        return this;
    }

    public CashFlow addCategory(Category category) {
        categories.add(category);
        return this;
    }

    public CashFlow removeCategory(Category category) {
        categories.remove(category);
        return this;
    }

    public CashFlow clearCategories() {
        categories.clear();
        return this;
    }

    public List<Category> getCategories() {
        return new ArrayList<Category>(categories);
    }

    public enum Type {
        INCOME,
        EXPANSE,
        TRANSFER
    }
}
