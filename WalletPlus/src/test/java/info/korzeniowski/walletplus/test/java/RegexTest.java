package info.korzeniowski.walletplus.test.java;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import info.korzeniowski.walletplus.util.KorzeniowskiUtils;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class RegexTest {

    @Test
    public void shouldSplitCategoryName1() {
        String[] splited = splitCategory(""); //
        assertThat(splited[0]).isEqualTo("");
        assertThat(splited).hasSize(1);
    }

    @Test
    public void shouldSplitCategoryName2() {
        String[] splited = splitCategory("tag"); // T
        assertThat(splited[0]).isEqualTo("tag");
        assertThat(splited).hasSize(1);
    }

    @Test
    public void shouldSplitCategoryName3() {
        String[] splited = splitCategory("tag1 "); // [T1]_
        assertThat(splited[0]).isEqualTo("tag1");
        assertThat(splited[1]).isEqualTo("");
        assertThat(splited).hasSize(2);
    }

    @Test
    public void shouldSplitCategoryName4() {
        String[] splited = splitCategory("tag1 ta"); // [T1]_T2
        assertThat(splited[0]).isEqualTo("tag1");
        assertThat(splited[1]).isEqualTo("ta");
        assertThat(splited).hasSize(2);
    }

    @Test
    public void shouldSplitCategoryName5() {
        String[] splited = splitCategory("tag1 tag2 "); // [T1]_[T2]_
        assertThat(splited[0]).isEqualTo("tag1");
        assertThat(splited[1]).isEqualTo("tag2");
        assertThat(splited[2]).isEqualTo("");
        assertThat(splited).hasSize(3);
    }

    @Test
    public void shouldSplitCategoryName6() {
        String[] splited = splitCategory("tag1   tag2   "); // [T1]_[T2]_
        assertThat(splited[0]).isEqualTo("tag1");
        assertThat(splited[1]).isEqualTo("tag2");
        assertThat(splited[2]).isEqualTo("");
        assertThat(splited).hasSize(3);
    }

    private String[] splitCategory(String categoryName) {
        return categoryName.replaceAll("\\s+", " ").split(" ", -1);
    }

    @Test
    public void testFileNameExtractor() {
        assertThat(KorzeniowskiUtils.Files.getBaseName("TestName.db")).isEqualTo("TestName");
    }

    @Test
    public void testDigitRegex() {
        String regex = "^(\\+|\\-)?(([0-9]+(\\.[0-9]{0,4})?)|(\\.[0-9]{0,4}))$";

        assertThat(".".matches(regex)).isTrue();
        assertThat(".2".matches(regex)).isTrue();
        assertThat("-.2".matches(regex)).isTrue();
        assertThat("0.".matches(regex)).isTrue();
        assertThat("0".matches(regex)).isTrue();
        assertThat("1.1221".matches(regex)).isTrue();
        assertThat("-0.21".matches(regex)).isTrue();
        assertThat(".21".matches(regex)).isTrue();
        assertThat("10.21".matches(regex)).isTrue();
        assertThat("+30.2123".matches(regex)).isTrue();
        assertThat("+0.2123".matches(regex)).isTrue();
        assertThat("+3253".matches(regex)).isTrue();
        assertThat("-325312352".matches(regex)).isTrue();
        assertThat("-0.2123".matches(regex)).isTrue();
        assertThat("-3253".matches(regex)).isTrue();

        assertThat("x".matches(regex)).isFalse();
        assertThat("+".matches(regex)).isFalse();
        assertThat("x325312352".matches(regex)).isFalse();
        assertThat("x0.212".matches(regex)).isFalse();
        assertThat("x3253".matches(regex)).isFalse();
        assertThat("333252..21".matches(regex)).isFalse();
        assertThat("-333252..21".matches(regex)).isFalse();
        assertThat("0.212352gfdd".matches(regex)).isFalse();
        assertThat("fdsa0.2123".matches(regex)).isFalse();
    }
}
