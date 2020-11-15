package com.goody.myapplication.ui.dashboard;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.goody.myapplication.R;
import com.goody.myapplication.roomdb.AppDatabase;
import com.goody.myapplication.roomdb.SearchListDto;
import com.marozzi.segmentedtab.SegmentedGroup;
import com.marozzi.segmentedtab.SegmentedTab;

import java.util.Date;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;

    private String mallName[] = {"","네이버","다나와","쿠팡","스타일쉐어","마켓컬리","인터파크","옥션","11번가","위메트","G마켓","이마트몰"};
    private static int mallIndex = 0;
    private String searchSort[] = {"sim","date","dsc","asc"};
    private static int searchSortIndex = 0;

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });

        // 검색창과 최근 검색창의 프레그먼트 분리 (초기화면 : 최근 검색창)
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_frame, new recentSearchFragment())
                .commit();

        // 검색 툴바 설정
        Toolbar tb = root.findViewById(R.id.dashboard_toolbar);
        EditText editText = tb.findViewById(R.id.searchbar_edit);
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    EditText edit = (EditText)v;
                    final String editTextString = edit.getText().toString();
                    AppDatabase.getInstance(getActivity()).searchListDao().insert(
                            new SearchListDto(editTextString,mallName[mallIndex],
                                    new Date(System.currentTimeMillis())));

                    Fragment fr = new SearchInfoFragment(
                            editTextString,mallName[mallIndex],searchSort[searchSortIndex]);

                    transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragment_frame, fr)
                            .addToBackStack(null)
                            .commit();

                    return true;
                }
                return false;
            }
        });

        // 검색 툴바 : 검색 엔진 선택 Dialog
        final ImageView iv = root.findViewById(R.id.search_mall_imageview);
        iv.setBackground(new ShapeDrawable(new OvalShape()));
        iv.setClipToOutline(true);
        iv.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.malldialog, null);

                AlertDialog.Builder oDialog = new AlertDialog.Builder(getActivity(),
                        android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
                oDialog.setView(dialogView);
                AlertDialog alert = oDialog.create();
                alert.show();

                SegmentedGroup s = ((SegmentedGroup) alert.findViewById(R.id.group_one));
                s.setOnSegmentedGroupListener(
                        new SegmentedGroup.OnSegmentedGroupListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onSegmentedTabSelected(SegmentedTab tab, int checkedId) {
                                String s = tab.getText().toString();
                                if(s.equals("랭킹순")){
                                    searchSortIndex = 0;
                                }else if(s.equals("최신순")){
                                    searchSortIndex = 1;
                                }else if(s.equals("높은 가격순")){
                                    searchSortIndex = 2;
                                }else{
                                    searchSortIndex = 3;
                                }
                            }
                        });

                final LinearLayout mallLinear = alert.findViewById(R.id.mall_linearlayout);
                for(int i = 0; i < mallLinear.getChildCount(); i++){
                    final ImageView malliv = (ImageView)mallLinear.getChildAt(i);
                    // image round shape 적용
                    malliv.setBackground(new ShapeDrawable(new OvalShape()));
                    malliv.setClipToOutline(true);

                    // image click Listener
                    final int finalI = i;

                    malliv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mallLinear.getChildAt(mallIndex).setAlpha(0.5f);
                            mallIndex = finalI;
                            mallLinear.getChildAt(mallIndex).setAlpha(1.0f);
                            iv.setImageDrawable(malliv.getDrawable());
                        }
                    });
                }
            }
        });

        return root;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

        fragmentManager = getActivity().getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
    }

}