package info.korzeniowski.walletplus.test.java;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsFragment;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class RegexTest {

    @Test
    public void testDigitRegex() {
        String regex = CashFlowDetailsFragment.digitRegex;

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
