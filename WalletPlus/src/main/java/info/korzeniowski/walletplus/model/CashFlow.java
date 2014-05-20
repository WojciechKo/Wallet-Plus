package info.korzeniowski.walletplus.model;

import java.util.Date;

public class CashFlow {
    private Long id;
    private Wallet from;
    private Wallet to;
    private float amount;
    private Long categoryId;
    private String comment;
    private Date dateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Wallet getFrom() {
        return from;
    }

    public void setFrom(Wallet from) {
        this.from = from;
    }

    public Wallet getTo() {
        return to;
    }

    public void setTo(Wallet to) {
        this.to = to;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }
}
