package info.korzeniowski.walletplus.test.robolectric.datamanager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.EnumSet;
import java.util.Set;

import info.korzeniowski.walletplus.model.Category;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CategoryTypeTest {

    @Test
    public void testEnumToBitwiseTypeConversion() {
        assertThat(Category.Type.convertEnumToBitwise(EnumSet.noneOf(Category.Type.class))).
                isEqualTo(0);
        assertThat(Category.Type.convertEnumToBitwise(EnumSet.of(Category.Type.INCOME))).
                isEqualTo(1);
        assertThat(Category.Type.convertEnumToBitwise(EnumSet.of(Category.Type.EXPENSE))).
                isEqualTo(2);
        assertThat(Category.Type.convertEnumToBitwise(EnumSet.of(Category.Type.INCOME, Category.Type.EXPENSE))).
                isEqualTo(3);
    }

    @Test
    public void testBitwiseToEnumTypeConversion() {
        Set<Category.Type> testSet = Category.Type.convertBitwiseToEnumSet(0);

        assertThat(testSet).doesNotContain(Category.Type.INCOME);
        assertThat(testSet).doesNotContain(Category.Type.EXPENSE);

        testSet = Category.Type.convertBitwiseToEnumSet(1);
        assertThat(testSet).contains(Category.Type.INCOME);
        assertThat(testSet).doesNotContain(Category.Type.EXPENSE);

        testSet = Category.Type.convertBitwiseToEnumSet(2);
        assertThat(testSet).doesNotContain(Category.Type.INCOME);
        assertThat(testSet).contains(Category.Type.EXPENSE);

        testSet = Category.Type.convertBitwiseToEnumSet(3);
        assertThat(testSet).contains(Category.Type.INCOME);
        assertThat(testSet).contains(Category.Type.EXPENSE);
    }
}
