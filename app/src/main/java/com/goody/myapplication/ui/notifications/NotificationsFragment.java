package com.goody.myapplication.ui.notifications;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.goody.myapplication.Item;
import com.goody.myapplication.R;
import com.goody.myapplication.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;

    private FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private ProgressBar progressBar;
    private LinearLayout feedRootLinear;
    private ScrollView feedScroll;
    private SwipeRefreshLayout swipe;

    private ArrayList<String> alreadyUIDList = new ArrayList<String>();

    private double firstCategoryLastValue;
    private double secondCategoryLastValue;
    private double thirdCategoryLastValue;

    private String firstOrderByCategory;
    private String secondOrderByCategory;
    private String thirdOrderByCategory;
    private long firstOrderByLimit;
    private long secondOrderByLimit;
    private long thirdOrderByLimit;

    private String CATEGORY1_LIST[] = {
            "패션의류","패션잡화","화장품미용","디지털가전","가구인테리어",
            "출산육아","식품","스포츠레저","생활건강","여가생활편의"
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });

        feedRootLinear = root.findViewById(R.id.feed_root_linear);
        progressBar = root.findViewById(R.id.feed_progress);
        feedScroll = root.findViewById(R.id.feed_scroll);
        feedScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = feedScroll.getScrollY(); // For ScrollView

                View v = feedScroll.getChildAt(feedScroll.getChildCount() - 1);
                if((v.getBottom() - (feedScroll.getHeight() + scrollY)) == 0){
                    // End
                    try {
                        progressBar.setVisibility(View.VISIBLE);

                        addFeedUser();
                    } catch (Exception e) {
                        Log.d("ERROR", e.toString());
                    }
                }
            }
        });
        swipe = root.findViewById(R.id.feed_swipe_layout);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                feedRootLinear.removeAllViews();
                alreadyUIDList.clear();
                progressBar.setVisibility(View.VISIBLE);
                swipe.setRefreshing(false);
                addFeedUser();
            }
        });


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDB = FirebaseFirestore.getInstance();

        addFeedUser();

        return root;
    }

    public void addFeedUser(){
        if(mDB != null && mAuth != null && mUser != null) {
            // is sucesss
            mDB.collection("users").
                    document(mUser.getUid()).
                    get().
                    addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot snap = task.getResult();
                            Map<String,Object> datafiled = snap.getData();

                            Long countValue = (Long)datafiled.get("count");

                            Double categoryPercent[] = new Double[CATEGORY1_LIST.length];
                            int categoryPercentIndex[] = new int[CATEGORY1_LIST.length];
                            for(int i = 0; i < CATEGORY1_LIST.length; i++){
                                categoryPercent[i] = (Double)datafiled.get(CATEGORY1_LIST[i]);
                                categoryPercentIndex[i] = i;
                            }

                            int i,j;

                            for(i = 0; i < categoryPercent.length - 1; i++){
                                for(j = 0; j < categoryPercent.length - 1 - i; j++){
                                    if(categoryPercent[j] < categoryPercent[j + 1]){
                                        Double temp = categoryPercent[j];
                                        categoryPercent[j] = categoryPercent[j + 1];
                                        categoryPercent[j + 1] = temp;

                                        int tempIdx = categoryPercentIndex[j];
                                        categoryPercentIndex[j] = categoryPercentIndex[j + 1];
                                        categoryPercentIndex[j + 1] = tempIdx;
                                    }
                                }
                            }

                            firstOrderByCategory = CATEGORY1_LIST[categoryPercentIndex[0]];
                            secondOrderByCategory = CATEGORY1_LIST[categoryPercentIndex[1]];
                            thirdOrderByCategory = CATEGORY1_LIST[categoryPercentIndex[2]];

                            firstCategoryLastValue = categoryPercent[0];
                            secondCategoryLastValue = categoryPercent[1];
                            thirdCategoryLastValue = categoryPercent[2];

                            firstOrderByLimit = (long)(Math.round(categoryPercent[0] / 10.0));
                            secondOrderByLimit = (long)(Math.round(categoryPercent[1] / 10.0));
                            thirdOrderByLimit = (long)(Math.round(categoryPercent[2] / 10.0));

                            if(firstOrderByLimit > 0){
                                mDB.collection("users").
                                        orderBy(firstOrderByCategory, Query.Direction.DESCENDING).
                                        startAfter(firstCategoryLastValue).
                                        limit(firstOrderByLimit).
                                        get().
                                        addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                try {
                                                    if (task.isSuccessful()) {
                                                        for (final QueryDocumentSnapshot document : task.getResult()) {
                                                            if(alreadyUIDList.contains(document.getId())){
                                                                continue;
                                                            }else{
                                                                alreadyUIDList.add(document.getId());
                                                            }

                                                            final ArrayList<Item> itemlist = new ArrayList<Item>();
                                                            firstCategoryLastValue = (Double)document.get(firstOrderByCategory);

                                                            document.getReference().collection("wishList").
                                                                    get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                                    if (task.isSuccessful()) {
                                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                                            Map<String, Object> datafiled = document.getData();
                                                                            String link = (String) datafiled.get("link");
                                                                            String image = (String) datafiled.get("image");
                                                                            String mallName = (String) datafiled.get("mallName");
                                                                            String price = (String) datafiled.get("price");
                                                                            String title = (String) datafiled.get("title");

                                                                            itemlist.add(new Item(title, mallName, image, link, price));
                                                                        }
                                                                        Map<String, Object> nicknamefiled = document.getData();
                                                                        // UserFeed Add
                                                                        feedRootLinear.addView(makeNewFeed((String) document.get("nickname"), itemlist));
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                progressBar.setVisibility(View.INVISIBLE);
                            }

                            if(secondOrderByLimit > 0) {
                                mDB.collection("users").
                                        orderBy(secondOrderByCategory, Query.Direction.DESCENDING).
                                        startAfter(secondCategoryLastValue).
                                        limit(firstOrderByLimit).
                                        get().
                                        addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                try {
                                                    if (task.isSuccessful()) {
                                                        for (final QueryDocumentSnapshot document : task.getResult()) {
                                                            if(alreadyUIDList.contains(document.getId())){
                                                                continue;
                                                            }else{
                                                                alreadyUIDList.add(document.getId());
                                                            }
                                                            final ArrayList<Item> itemlist = new ArrayList<Item>();

                                                            secondCategoryLastValue = (Double)document.get(secondOrderByCategory);

                                                            document.getReference().collection("wishList").
                                                                    get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                                    if (task.isSuccessful()) {
                                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                                            Map<String, Object> datafiled = document.getData();
                                                                            String link = (String) datafiled.get("link");
                                                                            String image = (String) datafiled.get("image");
                                                                            String mallName = (String) datafiled.get("mallName");
                                                                            String price = (String) datafiled.get("price");
                                                                            String title = (String) datafiled.get("title");

                                                                            itemlist.add(new Item(title, mallName, image, link, price));
                                                                        }

                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                        Map<String, Object> nicknamefiled = document.getData();
                                                                        // UserFeed Add
                                                                        feedRootLinear.addView(makeNewFeed((String) document.get("nickname"), itemlist));
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                            }

                            if(thirdOrderByLimit > 0) {
                                mDB.collection("users").
                                        orderBy(thirdOrderByCategory, Query.Direction.DESCENDING).
                                        startAfter(thirdCategoryLastValue).
                                        limit(firstOrderByLimit).
                                        get().
                                        addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                try {
                                                    if (task.isSuccessful()) {
                                                        for (final QueryDocumentSnapshot document : task.getResult()) {
                                                            if(alreadyUIDList.contains(document.getId())){
                                                                continue;
                                                            }else{
                                                                alreadyUIDList.add(document.getId());
                                                            }
                                                            final ArrayList<Item> itemlist = new ArrayList<Item>();

                                                            thirdCategoryLastValue = (Double)document.get(thirdOrderByCategory);

                                                            document.getReference().collection("wishList").
                                                                    get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                                    if (task.isSuccessful()) {
                                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                                            Map<String, Object> datafiled = document.getData();
                                                                            String link = (String) datafiled.get("link");
                                                                            String image = (String) datafiled.get("image");
                                                                            String mallName = (String) datafiled.get("mallName");
                                                                            String price = (String) datafiled.get("price");
                                                                            String title = (String) datafiled.get("title");

                                                                            itemlist.add(new Item(title, mallName, image, link, price));
                                                                        }

                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                        Map<String, Object> nicknamefiled = document.getData();
                                                                        // UserFeed Add
                                                                        feedRootLinear.addView(makeNewFeed((String) document.get("nickname"), itemlist));
                                                                    }

                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                }
                                                            });
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private View makeNewFeed(String nickname,ArrayList<Item> itemlist){
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout feed_root = (LinearLayout) inflater.inflate(R.layout.feed_layout, null);

        TextView userText = (TextView)feed_root.findViewById(R.id.user_name);
        userText.setText(nickname);

        LinearLayout feed_item_list_linear = (LinearLayout)feed_root.findViewById(R.id.feed_item_list);
        for(Item i : itemlist){
            feed_item_list_linear.addView(makeNewItem(i));
        }

        return feed_root;
    }

    private CardView makeNewItem(Item item) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        CardView view = (CardView) inflater.inflate(R.layout.wishlistcard, null);

        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = HomeFragment.dpToPx(getActivity(), 180);
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;

        layoutParams.setMarginStart(HomeFragment.dpToPx(getActivity(),18));
        layoutParams.setMargins(
                0, HomeFragment.dpToPx(getActivity(),16),
                0, HomeFragment.dpToPx(getActivity(),16));
        view.setLayoutParams(layoutParams);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    /*
                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    int index = grid.getChildCount() - 1;
                    Item item = ItemSingleton.getInstance().itemList.get(index);
                    intent.putExtra("URL",item.link);
                    intent.putExtra("title",item.title);
                    intent.putExtra("mallName",item.mallName);
                    intent.putExtra("imageURL",item.image);
                    intent.putExtra("price",item.price);*/
            }
        });

        LinearLayout linear = ((LinearLayout) view.getChildAt(0));
        FrameLayout frame = (FrameLayout) linear.getChildAt(0);
        ImageView iv = (ImageView) frame.getChildAt(0);
        Glide.with(this.getContext()).load(item.image).into(iv);

        TextView tv1 = (TextView) linear.getChildAt(1);
        tv1.setText(Html.fromHtml(item.title));

        TextView tv2 = (TextView) linear.getChildAt(2);
        tv2.setText(item.mallName);

        TextView tv3 = (TextView) linear.getChildAt(3);
        DecimalFormat formatter = new DecimalFormat("###,###");
        String s = formatter.format(Integer.valueOf(item.price));
        s = s + "원";
        tv3.setText(s);

        return view;
    }
}