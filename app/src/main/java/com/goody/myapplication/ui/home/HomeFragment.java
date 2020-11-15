package com.goody.myapplication.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import com.goody.myapplication.Item;
import com.goody.myapplication.R;
import com.goody.myapplication.ui.SelectCategoryActivity;
import com.goody.myapplication.ui.WebViewActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private ArrayList<Item> itemList = new ArrayList<Item>();

    private boolean isDeleteState = false;

    private FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private ProgressBar progressBar;
    private TextView emptyText1;
    private TextView emptyText2;

    private SwipeRefreshLayout swipe;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

            }
        });

        activity_home = root.findViewById(R.id.home_GridLayout);

        progressBar = root.findViewById(R.id.progress);
        emptyText1 = root.findViewById(R.id.empty_text1);
        emptyText2 = root.findViewById(R.id.empty_text2);
        swipe = root.findViewById(R.id.home_swipe_layout);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                progressBar.setVisibility(View.VISIBLE);
                swipe.setRefreshing(false);
                refresh();
            }
        });

        progressBar.setVisibility(View.VISIBLE);
        emptyText1.setVisibility(View.INVISIBLE);
        emptyText2.setVisibility(View.INVISIBLE);

        Toolbar tb = root.findViewById(R.id.home_toolbar);
        TextView edittv = tb.findViewById(R.id.home_toolbar_edit);
        edittv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < itemList.size(); i++) {
                    CardView c = (CardView) activity_home.getChildAt(i);
                    LinearLayout l = (LinearLayout) c.getChildAt(0);
                    FrameLayout f = (FrameLayout) l.getChildAt(0);
                    Button b = (Button) f.getChildAt(1);

                    // 편집상태 해제
                    if (isDeleteState) {
                        Animation animation = AnimationUtils.loadAnimation(getActivity(),R.anim.alpha_remove);
                        b.startAnimation(animation);
                    } else {  // 편집상태로
                        Animation animation = AnimationUtils.loadAnimation(getActivity(),R.anim.alpha);
                        b.startAnimation(animation);
                    }
                }

                isDeleteState = !isDeleteState;
            }
        });

        return root;
    }

    public void refresh(){
        if(mDB != null && mAuth != null && mUser != null) {

            activity_home.removeAllViews();

            mDB.collection("users")
                    .document(mUser.getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        Double d = (Double)document.get("패션의류");
                        if(d == null){
                            Intent intent = new Intent(getActivity(), SelectCategoryActivity.class);
                            startActivity(intent);
                        }
                    }
                }
            });

            mDB.collection("users").document(mUser.getUid()).
                    collection("wishList").get().
                    addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            try {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Map<String, Object> datafiled = document.getData();
                                        String link = (String) datafiled.get("link");
                                        String image = (String) datafiled.get("image");
                                        String mallName = (String) datafiled.get("mallName");
                                        String price = (String) datafiled.get("price");
                                        String title = (String) datafiled.get("title");
                                        String category1 = (String) datafiled.get("category1");
                                        String category2 = (String) datafiled.get("category2");
                                        String category3 = (String) datafiled.get("category3");
                                        String category4 = (String) datafiled.get("category4");

                                        Item item = new Item(title, mallName, image, link, price,
                                                category1,category2,category3,category4);

                                        itemList.add(item);
                                        makeNewItem(item);

                                        emptyText1.setVisibility(View.INVISIBLE);
                                        emptyText2.setVisibility(View.INVISIBLE);
                                    }

                                    if(itemList.size() == 0){
                                        emptyText1.setVisibility(View.VISIBLE);
                                        emptyText2.setVisibility(View.VISIBLE);
                                    }
                                } else {

                                }
                            } catch (Exception e) {
                                Log.e("ERROR", e.toString());
                            }
                        }
                    });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDB = FirebaseFirestore.getInstance();

        refresh();
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

    }

    private GridLayout activity_home;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    static public int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private CardView makeNewItem(Item item) {

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        CardView view = (CardView)inflater.inflate(R.layout.wishlistcard, null);
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = dpToPx(getActivity(),180);
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;

        layoutParams.setMarginStart(dpToPx(getActivity(),18));
        layoutParams.setMargins(
                dpToPx(getActivity(),8), dpToPx(getActivity(),10),0, dpToPx(getActivity(),10));
        view.setLayoutParams(layoutParams);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                int index = activity_home.getChildCount() - 1;
                Item item = itemList.get(index);
                intent.putExtra("link",item.link);
                intent.putExtra("title",item.title);
                intent.putExtra("mallName",item.mallName);
                intent.putExtra("imageURL",item.image);
                intent.putExtra("price",item.price);
                intent.putExtra("category1",item.category1);
                intent.putExtra("category2",item.category2);
                intent.putExtra("category3",item.category3);
                intent.putExtra("category4",item.category4);
                getActivity().startActivity(intent);
            }
        });

        LinearLayout linear = ((LinearLayout)view.getChildAt(0));
        FrameLayout frame = (FrameLayout)linear.getChildAt(0);
        ImageView iv = (ImageView)frame.getChildAt(0);
        Glide.with(this.getContext()).load(item.image).into(iv);

        //  데이터 삭제
        Button button = (Button)frame.getChildAt(1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDeleteState){
                    ViewParent card = v.getParent().getParent().getParent();
                    activity_home.removeView((View)card);

                    for(int i = 0; i < itemList.size(); i++){
                        TextView tv = (TextView)((LinearLayout)v.getParent().getParent()).getChildAt(1);
                        final String name = tv.getText().toString();
                        if(name == itemList.get(i).title){
                            itemList.remove(i);
                            if(itemList.size() == 0){
                                emptyText1.setVisibility(View.VISIBLE);
                                emptyText2.setVisibility(View.VISIBLE);
                            }

                            // Firestore 데이터 삭제
                            mDB.collection("users").document(mUser.getUid()).
                                    collection("wishList").get().
                                    addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            try {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        Map<String,Object> datafiled = document.getData();
                                                        String title = (String)datafiled.get("title");
                                                        if(title.equals(name)){
                                                            mDB.collection("users").document(mUser.getUid()).
                                                                    collection("wishList").document(document.getId()).delete();
                                                        }
                                                    }
                                                } else {

                                                }
                                            }catch(Exception e){
                                                Log.e("ERROR",e.toString());
                                            }
                                        }
                                    });
                            break;
                        }
                    }
                }
            }
        });

        TextView tv1 = (TextView)linear.getChildAt(1);
        tv1.setText(item.title);

        TextView tv2 = (TextView)linear.getChildAt(2);
        tv2.setText(item.mallName);

        TextView tv3 = (TextView)linear.getChildAt(3);

        DecimalFormat formatter = new DecimalFormat("###,###");
        String s = formatter.format(Integer.valueOf(item.price));
        s = s + "원";

        tv3.setText(s);

        activity_home.addView(view);

        return view;
    }
}