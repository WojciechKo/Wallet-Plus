package info.korzeniowski.walletplus.ui.cashflow.details;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.astuetz.PagerSlidingTabStrip;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.ui.cashflow.details.tab.CashFlowBaseDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.details.tab.CashFlowExpanseDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.details.tab.CashFlowIncomeDetailsFragment;
import info.korzeniowski.walletplus.ui.cashflow.details.tab.CashFlowTransferDetailsFragment;

public class CashFlowDetailsContainerFragment extends Fragment {
    public static final String TAG = "cashFlowDetailsContainer";
    public static final String CASH_FLOW_ID = "CASH_FLOW_ID";
    private static final String CASH_FLOW_DETAILS_STATE = "cashFlowDetailsState";

    @InjectView(R.id.tabs)
    PagerSlidingTabStrip tabs;

    @InjectView(R.id.pager)
    ViewPager pager;

    @Inject
    @Named("local")
    CashFlowService cashFlowService;

    private CashFlowDetailsParcelableState cashFlowDetailsParcelableState;
    private List<CashFlowDetailsPage> cashFlowDetailsPageList;
    private CashFlowPagerAdapter pagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        setHasOptionsMenu(true);
        cashFlowDetailsParcelableState = initOrRestoreState(savedInstanceState);
        cashFlowDetailsPageList = createCashFlowPageDetails();
    }

    private CashFlowDetailsParcelableState initOrRestoreState(Bundle savedInstanceState) {
        CashFlowDetailsParcelableState restoredState = tryRestore(savedInstanceState);
        return restoredState != null
                ? restoredState
                : initState();
    }

    private CashFlowDetailsParcelableState tryRestore(Bundle savedInstanceState) {
        return savedInstanceState != null
                ? (CashFlowDetailsParcelableState) savedInstanceState.getParcelable(CASH_FLOW_DETAILS_STATE)
                : null;
    }

    private CashFlowDetailsParcelableState initState() {
        Long cashFlowId = getArguments() != null
                ? getArguments().getLong(CASH_FLOW_ID)
                : 0;


        return cashFlowId != 0
                ? getStateFromCashFlow(cashFlowService.findById(cashFlowId))
                : getNewCashFlowState();
    }

    private CashFlowDetailsParcelableState getStateFromCashFlow(CashFlow cashFlow) {
        return cashFlow != null
                ? getEditCashFlowState(cashFlow)
                : getNewCashFlowState();
    }

    private CashFlowDetailsParcelableState getEditCashFlowState(CashFlow cashFlow) {
        CashFlowDetailsParcelableState state = new CashFlowDetailsParcelableState();
        state.setId(cashFlow.getId());
        state.setAmount(cashFlow.getAmount().toString());
        state.setComment(cashFlow.getComment());
        state.setDate(cashFlow.getDateTime().getTime());
        state.setType(cashFlow.getType());

        if (state.getType() == CashFlow.Type.INCOME) {
            state.setIncomeCategory(cashFlow.getCategory());
            state.setExpanseCategory(cashFlowService.getOtherCategory());
            state.setIncomeFromWallet(cashFlow.getFromWallet());
            state.setIncomeToWallet(cashFlow.getToWallet());
        } else if (state.getType() == CashFlow.Type.EXPANSE) {
            state.setIncomeCategory(cashFlowService.getOtherCategory());
            state.setExpanseCategory(cashFlow.getCategory());
            state.setExpanseFromWallet(cashFlow.getFromWallet());
            state.setExpanseToWallet(cashFlow.getToWallet());
        } else if (state.getType() == CashFlow.Type.TRANSFER) {
            state.setIncomeCategory(cashFlowService.getOtherCategory());
            state.setExpanseCategory(cashFlowService.getOtherCategory());
            state.setExpanseFromWallet(cashFlow.getFromWallet());
            state.setIncomeToWallet(cashFlow.getToWallet());
        }
        return state;
    }

    private CashFlowDetailsParcelableState getNewCashFlowState() {
        CashFlowDetailsParcelableState state = new CashFlowDetailsParcelableState();
        state.setDate(Calendar.getInstance().getTimeInMillis());
        state.setIncomeCategory(cashFlowService.getOtherCategory());
        state.setExpanseCategory(cashFlowService.getOtherCategory());
        state.setType(CashFlow.Type.EXPANSE);
        return state;
    }

    private List<CashFlowDetailsPage> createCashFlowPageDetails() {
        List<CashFlowDetailsPage> cashFlowDetailsPageList = Lists.newArrayList();

        Bundle bundle = new Bundle();
        bundle.putParcelable(CashFlowBaseDetailsFragment.CASH_FLOW_DETAILS_STATE, cashFlowDetailsParcelableState);

        cashFlowDetailsPageList.add(new CashFlowDetailsPage(CashFlowExpanseDetailsFragment.class, CashFlow.Type.EXPANSE, "expanse"));
        cashFlowDetailsPageList.add(new CashFlowDetailsPage(CashFlowTransferDetailsFragment.class, CashFlow.Type.TRANSFER, "transfer"));
        cashFlowDetailsPageList.add(new CashFlowDetailsPage(CashFlowIncomeDetailsFragment.class, CashFlow.Type.INCOME, "income"));

        return cashFlowDetailsPageList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.cashflow_details_main_layout, container, false);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    public void setupViews() {
        pagerAdapter = new CashFlowPagerAdapter(this, cashFlowDetailsPageList, cashFlowDetailsParcelableState);

        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(3);

        tabs.setViewPager(pager);
        tabs.setTextColor(getResources().getColor(R.color.white));
        tabs.setBackgroundColor(getResources().getColor(R.color.mainColor));
        tabs.setIndicatorColor(getResources().getColor(R.color.lightMainColor));
        tabs.setTextColor(getResources().getColor(R.color.white));
        tabs.setOnPageChangeListener(pagerAdapter);
        selectPage(getPositionOfFragmentByType(cashFlowDetailsParcelableState.getType()));
    }

    private int getPositionOfFragmentByType(final CashFlow.Type type) {
        return Iterables.indexOf(cashFlowDetailsPageList, new Predicate<CashFlowDetailsPage>() {
            @Override
            public boolean apply(CashFlowDetailsPage input) {
                return input.getType().equals(type);
            }
        });
    }

    private void selectPage(int position) {
        // Little hack for this issue: http://stackoverflow.com/a/17694619/2399340
        if (position != 0) {
            pager.setCurrentItem(position);
        } else {
            pagerAdapter.onPageSelected(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CASH_FLOW_DETAILS_STATE, cashFlowDetailsParcelableState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_save, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public static class CashFlowPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
        private final WeakReference<Fragment> fragment;
        private List<CashFlowDetailsPage> cashFlowDetailsPageList;
        private CashFlowDetailsParcelableState cashFlowDetailsParcelableState;
        private int selectedPosition = -1;

        public CashFlowPagerAdapter(Fragment fragment, List<CashFlowDetailsPage> cashFlowDetailsPageList, CashFlowDetailsParcelableState cashFlowDetailsParcelableState) {
            super(fragment.getChildFragmentManager());
            this.fragment = new WeakReference<Fragment>(fragment);
            this.cashFlowDetailsPageList = cashFlowDetailsPageList;
            this.cashFlowDetailsParcelableState = cashFlowDetailsParcelableState;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return cashFlowDetailsPageList.get(position).getTitle();
        }

        @Override
        public int getCount() {
            return cashFlowDetailsPageList.size();
        }

        @Override
        public Fragment getItem(int position) {
            try {
                Fragment fragment = cashFlowDetailsPageList.get(position).getFragmentClass().newInstance();
                Bundle bundle = new Bundle();
                bundle.putParcelable(CashFlowBaseDetailsFragment.CASH_FLOW_DETAILS_STATE, cashFlowDetailsParcelableState);
                bundle.putBoolean(CashFlowBaseDetailsFragment.IS_SELECTED, selectedPosition == position);
                fragment.setArguments(bundle);
                return fragment;
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            throw new RuntimeException("error while getting CashFlowDetailsFragment instance.");
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int newPosition) {
            if (selectedPosition != -1) {
                hideKeyboard();
                cashFlowDetailsParcelableState.setType(cashFlowDetailsPageList.get(newPosition).getType());
                ((CashFlowBaseDetailsFragment) getFragmentByPosition(selectedPosition)).setFragmentAsInactive();
                ((CashFlowBaseDetailsFragment) getFragmentByPosition(newPosition)).setFragmentAsActive();
            }
            selectedPosition = newPosition;
        }

        private void hideKeyboard() {
            InputMethodManager inputManager = (InputMethodManager) fragment.get().getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            View view = fragment.get().getActivity().getCurrentFocus();
            if (view != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }

        private Fragment getFragmentByPosition(int position) {
            return fragment.get().getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + position);
        }
    }

    public static class CashFlowDetailsPage {
        Class<? extends CashFlowBaseDetailsFragment> fragment;
        CashFlow.Type type;
        String title;

        CashFlowDetailsPage(Class<? extends CashFlowBaseDetailsFragment> fragmentClass, CashFlow.Type type, String title) {
            this.fragment = fragmentClass;
            this.type = type;
            this.title = title;

        }

        public Class<? extends CashFlowBaseDetailsFragment> getFragmentClass() {
            return fragment;
        }

        public CashFlow.Type getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }
    }
}
