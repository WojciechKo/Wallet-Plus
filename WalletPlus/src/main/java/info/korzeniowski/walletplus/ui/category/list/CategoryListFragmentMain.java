package info.korzeniowski.walletplus.ui.category.list;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.common.collect.Lists;

import java.util.List;

import butterknife.OnItemClick;
import info.korzeniowski.walletplus.R;

public class CategoryListFragmentMain extends Fragment {
    public static final String TAG = "CategoryListFragmentMain";

    public enum CategoryType {
        INCOME,
        EXPANSE,
        BOTH;

        public static List<String> valuesString() {
            List<String> result = Lists.newArrayListWithCapacity(CategoryType.values().length);
            for (CategoryType type : CategoryType.values()) {
                result.add(type.name().toLowerCase());
            }
            return result;
        }
    }

    public enum Period {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }

    private Spinner spinner;
    private CategoryType selectedType;
    private Period selectedPeriod = Period.WEEK;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.category_type_spinner_item, CategoryType.valuesString());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner = (Spinner) getActivity().findViewById(R.id.toolbarSubtitle);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = CategoryType.values()[position];
                showCategoryStatsGroupByPeriod(selectedType, selectedPeriod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setVisibility(View.VISIBLE);
    }

    private void showCategoryStatsGroupByPeriod(CategoryType selectedType, Period selectedPeriod) {
        Toast.makeText(getActivity(), "Odpalam fragment:" + selectedType + ":" + selectedPeriod, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStop() {
        spinner.setVisibility(View.INVISIBLE);
        spinner = null;
        super.onStop();
    }

    @OnItemClick(R.id.toolbar)
    public void toolbarClicked() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

}
