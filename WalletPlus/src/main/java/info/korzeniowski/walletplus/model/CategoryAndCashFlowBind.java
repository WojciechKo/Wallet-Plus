package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;

public class CategoryAndCashFlowBind {
    public final static String CATEGORY_ID_FIELD_NAME = "category_id";
    public final static String CASH_FLOW_ID_FIELD_NAME = "cashFlow_id";

    @DatabaseField(foreign = true, canBeNull = false, uniqueCombo = true, index = true, columnDefinition = "integer REFERENCES category(id) ON DELETE CASCADE")
    Category category;

    @DatabaseField(foreign = true, canBeNull = false, uniqueCombo = true, index = true, columnDefinition = "integer REFERENCES cashFlow(id) ON DELETE CASCADE")
    CashFlow cashFlow;

    public CategoryAndCashFlowBind() {

    }

    public CategoryAndCashFlowBind(Category category, CashFlow cashFlow) {
        this.category = category;
        this.cashFlow = cashFlow;
    }

}
