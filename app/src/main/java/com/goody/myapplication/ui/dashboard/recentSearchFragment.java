package com.goody.myapplication.ui.dashboard;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.goody.myapplication.R;
import com.goody.myapplication.roomdb.AppDatabase;
import com.goody.myapplication.roomdb.SearchListDto;
import com.goody.myapplication.ui.home.HomeFragment;

import java.util.List;

public class recentSearchFragment extends Fragment {

    public recentSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        LinearLayout rootLinear = (LinearLayout)inflater.inflate(R.layout.fragment_recent_search, container, false);

        final LinearLayout recent_search_linear = (LinearLayout)rootLinear.findViewById(R.id.recent_search_linear);

        int dp20 = HomeFragment.dpToPx(getActivity(), 20);
        int dp10 = HomeFragment.dpToPx(getActivity(), 10);

        TextView removeAllText = rootLinear.findViewById(R.id.removeAllText);
        removeAllText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDatabase.getInstance(getActivity()).searchListDao().deltetAllData();
                recent_search_linear.removeViews(1,recent_search_linear.getChildCount() - 1);
            }
        });

        List<SearchListDto> list = AppDatabase.getInstance(getActivity())
                .searchListDao().loadAllSearchList();

        for (final SearchListDto s : list) {
            LayoutInflater inf = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final LinearLayout newSearchLayout = (LinearLayout)inf.inflate(R.layout.recent_search_item, null);
            newSearchLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_frame, new SearchInfoFragment(s.name,"","sim"))
                            .commit();
                }
            });

            TextView tv = newSearchLayout.findViewById(R.id.search_title);
            ImageView iv = newSearchLayout.findViewById(R.id.remove_img);

            tv.setText(s.name);

            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<SearchListDto> list = AppDatabase.getInstance(getActivity())
                            .searchListDao().loadAllSearchList();
                    if (list != null) {
                        LinearLayout parentLinear = (LinearLayout) v.getParent().getParent();
                        String parentText = ((TextView) parentLinear.getChildAt(0)).getText().toString();
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).name.equals(parentText)) {
                                recent_search_linear.removeView((View) v.getParent().getParent());
                                AppDatabase.getInstance(getActivity()).searchListDao().delete(list.get(i));
                                break;
                            }
                        }
                    }
                }
            });

            rootLinear.addView(newSearchLayout);
        }
        // Inflate the layout for this fragment


        return rootLinear;
    }
}