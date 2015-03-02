package info.korzeniowski.walletplus.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = TagAndCashFlowBind.TABLE_NAME)
public class TagAndCashFlowBind {

    public static final String TABLE_NAME = "tagAndCashFlowBind";

    public static final String TAG_ID_COLUMN_NAME = "tag_id";
    public static final String CASH_FLOW_ID_COLUMN_NAME = "cashFlow_id";

    @DatabaseField(columnName = TAG_ID_COLUMN_NAME, foreign = true, canBeNull = false, uniqueCombo = true, index = true,
            columnDefinition = "integer REFERENCES " + Tag.TABLE_NAME + "(" + Tag.ID_COLUMN_NAME + ") ON DELETE CASCADE")
    Tag tag;

    @DatabaseField(columnName = CASH_FLOW_ID_COLUMN_NAME, foreign = true, canBeNull = false, uniqueCombo = true, index = true,
            columnDefinition = "integer REFERENCES " + CashFlow.TABLE_NAME + "(" + CashFlow.ID_COLUMN_NAME + ") ON DELETE CASCADE")
    CashFlow cashFlow;

    public TagAndCashFlowBind() {

    }

    public TagAndCashFlowBind(Tag tag, CashFlow cashFlow) {
        this.tag = tag;
        this.cashFlow = cashFlow;
    }

}
