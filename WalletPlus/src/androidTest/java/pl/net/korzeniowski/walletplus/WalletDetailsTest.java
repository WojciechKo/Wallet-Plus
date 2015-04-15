package pl.net.korzeniowski.walletplus;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.mockito.Mockito;

import javax.inject.Inject;

import pl.net.korzeniowski.walletplus.model.Wallet;
import pl.net.korzeniowski.walletplus.service.WalletService;
import pl.net.korzeniowski.walletplus.ui.wallets.details.WalletDetailsActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

@LargeTest
public class WalletDetailsTest extends ActivityInstrumentationTestCase2<WalletDetailsActivity> {

    @Inject
    WalletService walletServiceMock;

    public WalletDetailsTest() {
        super(WalletDetailsActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        WalletPlus app = (WalletPlus) getInstrumentation().getTargetContext().getApplicationContext();
        app.component().inject(this);
        getActivity().getApplication();
    }

    public void testCurrentAmountBeInvisible() {
        onView(withId(R.id.walletCurrentAmountLabel)).check(matches(not(isDisplayed())));
        onView(withId(R.id.walletCurrentAmount)).check(matches(not(isDisplayed())));
    }

    public void testIfNameLabelAppearsAfterTextInput() {
        onView(withId(R.id.walletNameLabel)).check(matches(not(isDisplayed())));

        onView(withId(R.id.walletName)).perform(typeText("test"));
        onView(withId(R.id.walletNameLabel)).check(matches(isDisplayed()));

        onView(withId(R.id.walletName)).perform(typeText(""));
        onView(withId(R.id.walletNameLabel)).check(matches(isDisplayed()));
    }

    public void testIfInitialAmountLabelAppearsAfterTextInput() {
        onView(withId(R.id.walletInitialAmountLabel)).check(matches(not(isDisplayed())));

        onView(withId(R.id.walletInitialAmount)).perform(typeText("1.2"));
        onView(withId(R.id.walletInitialAmountLabel)).check(matches(isDisplayed()));

        onView(withId(R.id.walletInitialAmount)).perform(typeText(""));
        onView(withId(R.id.walletInitialAmountLabel)).check(matches(isDisplayed()));
    }

    public void shouldCallUpdateWallet() {
        String walletInitialAmount = "150.0";
        String walletName = "Wallet name";
        onView(withId(R.id.walletInitialAmount)).perform(typeText(walletInitialAmount));
        onView(withId(R.id.walletName)).perform(typeText(walletName));

        Wallet toInsert = new Wallet()
                .setName(walletName)
                .setInitialAmount(Double.parseDouble(walletInitialAmount));
        
        onView(withId(R.id.menu_save)).perform(click());

        Mockito.verify(walletServiceMock, Mockito.times(1)).insert(toInsert);
    }

    public void shouldNotCallUpdateWhenErrors() {
//        walletName.setError("simple error");
//        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_save));
//        Mockito.verify(walletServiceMock, Mockito.never()).insert(Mockito.any(Wallet.class));
//
//        walletName.setError(null);
//        walletInitialAmount.setError("other error");
//        activity.onOptionsItemSelected(new RoboMenuItem(R.id.menu_save));
//        Mockito.verify(walletServiceMock, Mockito.never()).insert(Mockito.any(Wallet.class));
    }
}
