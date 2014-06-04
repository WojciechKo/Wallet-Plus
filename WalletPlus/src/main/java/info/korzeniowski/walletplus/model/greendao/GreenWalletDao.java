package info.korzeniowski.walletplus.model.greendao;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;

import info.korzeniowski.walletplus.model.greendao.GreenWallet;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table Wallet.
*/
public class GreenWalletDao extends AbstractDao<GreenWallet, Long> {

    public static final String TABLENAME = "Wallet";

    /**
     * Properties of entity GreenWallet.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property AccountId = new Property(1, Long.class, "accountId", false, "ACCOUNT_ID");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property InitialAmount = new Property(3, double.class, "initialAmount", false, "INITIAL_AMOUNT");
        public final static Property CurrentAmount = new Property(4, Double.class, "currentAmount", false, "CURRENT_AMOUNT");
        public final static Property Type = new Property(5, int.class, "type", false, "TYPE");
    };

    private Query<GreenWallet> greenAccount_GreenWalletListQuery;

    public GreenWalletDao(DaoConfig config) {
        super(config);
    }
    
    public GreenWalletDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'Wallet' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'ACCOUNT_ID' INTEGER," + // 1: accountId
                "'NAME' TEXT NOT NULL ," + // 2: name
                "'INITIAL_AMOUNT' REAL NOT NULL ," + // 3: initialAmount
                "'CURRENT_AMOUNT' REAL," + // 4: currentAmount
                "'TYPE' INTEGER NOT NULL );"); // 5: type
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'Wallet'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, GreenWallet entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long accountId = entity.getAccountId();
        if (accountId != null) {
            stmt.bindLong(2, accountId);
        }
        stmt.bindString(3, entity.getName());
        stmt.bindDouble(4, entity.getInitialAmount());
 
        Double currentAmount = entity.getCurrentAmount();
        if (currentAmount != null) {
            stmt.bindDouble(5, currentAmount);
        }
        stmt.bindLong(6, entity.getType());
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public GreenWallet readEntity(Cursor cursor, int offset) {
        GreenWallet entity = new GreenWallet( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // accountId
            cursor.getString(offset + 2), // name
            cursor.getDouble(offset + 3), // initialAmount
            cursor.isNull(offset + 4) ? null : cursor.getDouble(offset + 4), // currentAmount
            cursor.getInt(offset + 5) // type
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, GreenWallet entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setAccountId(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setName(cursor.getString(offset + 2));
        entity.setInitialAmount(cursor.getDouble(offset + 3));
        entity.setCurrentAmount(cursor.isNull(offset + 4) ? null : cursor.getDouble(offset + 4));
        entity.setType(cursor.getInt(offset + 5));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(GreenWallet entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(GreenWallet entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "greenWalletList" to-many relationship of GreenAccount. */
    public List<GreenWallet> _queryGreenAccount_GreenWalletList(Long accountId) {
        synchronized (this) {
            if (greenAccount_GreenWalletListQuery == null) {
                QueryBuilder<GreenWallet> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.AccountId.eq(null));
                greenAccount_GreenWalletListQuery = queryBuilder.build();
            }
        }
        Query<GreenWallet> query = greenAccount_GreenWalletListQuery.forCurrentThread();
        query.setParameter(0, accountId);
        return query.list();
    }

}
