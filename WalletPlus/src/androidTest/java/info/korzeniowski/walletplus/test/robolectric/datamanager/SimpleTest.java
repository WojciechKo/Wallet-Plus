package info.korzeniowski.walletplus.test.robolectric.datamanager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SimpleTest {

    @Test
    public void testMe() {
        assertThat(true).isTrue();
    }
}
