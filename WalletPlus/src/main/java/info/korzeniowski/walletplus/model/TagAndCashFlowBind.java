package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;

public class TagAndCashFlowBind {
    public final static String TAG_ID_FIELD_NAME = "tag_id";
    public final static String CASH_FLOW_ID_FIELD_NAME = "cashFlow_id";

    @DatabaseField(foreign = true, canBeNull = false, uniqueCombo = true, index = true, columnDefinition = "integer REFERENCES tag(id) ON DELETE CASCADE")
    Tag tag;

    @DatabaseField(foreign = true, canBeNull = false, uniqueCombo = true, index = true, columnDefinition = "integer REFERENCES cashFlow(id) ON DELETE CASCADE")
    CashFlow cashFlow;

    public TagAndCashFlowBind() {

    }

    public TagAndCashFlowBind(Tag tag, CashFlow cashFlow) {
        this.tag = tag;
        this.cashFlow = cashFlow;
    }

}
