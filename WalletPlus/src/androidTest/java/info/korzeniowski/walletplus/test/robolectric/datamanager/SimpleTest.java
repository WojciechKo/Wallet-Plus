package info.korzeniowski.walletplus.test.robolectric.datamanager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SimpleTest {

    @Test
    public void testMe() {
        assertThat(true).isTrue();
    }
}
