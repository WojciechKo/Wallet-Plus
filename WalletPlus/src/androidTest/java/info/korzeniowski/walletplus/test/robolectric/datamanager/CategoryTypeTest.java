package info.korzeniowski.walletplus.test.robolectric.datamanager;

import android.database.sqlite.SQLiteOpenHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalCategoryDataManager;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.greendao.DaoMaster;
import info.korzeniowski.walletplus.model.greendao.DaoSession;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CategoryTypeTest {

    CategoryDataManager categoryDataManager;

    @Before
    public void setUp() {
        // ((TestWalletPlus) Robolectric.application).injectMocks(this);
        SQLiteOpenHelper dbHelper = new DaoMaster.DevOpenHelper(Robolectric.application, null, null);
        DaoSession daoSession = new DaoMaster(dbHelper.getWritableDatabase()).newSession();
        categoryDataManager = new LocalCategoryDataManager(daoSession.getGreenCategoryDao());
    }

    /**
     * Test Main category manipulations.
     */
    @Test
    public void setOfencodingCategoryType() {
        Set<EnumSet<Category.Type>> testCases = new HashSet<EnumSet<Category.Type>>();
        testCases.add(EnumSet.of(Category.Type.EXPENSE));
        testCases.add(EnumSet.of(Category.Type.INCOME, Category.Type.EXPENSE));
        testCases.add(EnumSet.of(Category.Type.EXPENSE, Category.Type.INCOME));
        testCases.add(EnumSet.of(Category.Type.INCOME));
        int index = 1;
        for (EnumSet<Category.Type> testCase : testCases) {
            Category category = new Category().setName("Main " + index++).setTypes(testCase);
            Long categoryId = categoryDataManager.insert(category);
            category = categoryDataManager.getById(categoryId);

            for (Category.Type type : testCase) {
                assertThat(category.getTypes(), hasItem(type));
            }

            for (Category.Type type : Category.Type.values()) {
                if (!testCase.contains(type)) {
                    assertThat(category.getTypes(), not(hasItem(type)));
                }
            }
        }
    }

    @Test
    public void enumToBitwise() {
        assertThat(
                Category.Type.convertEnumToBitwise(EnumSet.noneOf(Category.Type.class)),
                is(0)
        );
        assertThat(
                Category.Type.convertEnumToBitwise(EnumSet.of(Category.Type.INCOME)),
                is(1)
        );
        assertThat(
                Category.Type.convertEnumToBitwise(EnumSet.of(Category.Type.EXPENSE)),
                is(2)
        );
        assertThat(
                Category.Type.convertEnumToBitwise(EnumSet.of(Category.Type.INCOME, Category.Type.EXPENSE)),
                is(3)
        );

    }

        @Test
        public void bitwiseToEnum() {
        Set<Category.Type> testSet = Category.Type.convertBitwiseToEnumSet(0);
        assertThat(testSet, not(hasItem(Category.Type.INCOME)));
        assertThat(testSet, not(hasItem(Category.Type.EXPENSE)));

        testSet = Category.Type.convertBitwiseToEnumSet(1);
        assertThat(testSet, hasItem(Category.Type.INCOME));
        assertThat(testSet, not(hasItem(Category.Type.EXPENSE)));

        testSet = Category.Type.convertBitwiseToEnumSet(2);
        assertThat(testSet, not(hasItem(Category.Type.INCOME)));
        assertThat(testSet, hasItem(Category.Type.EXPENSE));

        testSet = Category.Type.convertBitwiseToEnumSet(3);
        assertThat(testSet, hasItem(Category.Type.INCOME));
        assertThat(testSet, hasItem(Category.Type.EXPENSE));
    }
}
