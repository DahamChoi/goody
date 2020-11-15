package com.goody.myapplication.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.goody.myapplication.Item;
import com.goody.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class WebViewActivity extends AppCompatActivity {

    boolean saveChecked = false;

    private Item mItem;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mDB;

    private Drawable nonselect_icon;
    private Drawable select_icon;

    private MenuItem saveMenuItem;

    private String CATEGORY1_LIST[] = {
            "패션의류","패션잡화","화장품미용","디지털가전","가구인테리어",
            "출산육아","식품","스포츠레저","생활건강","여가생활편의"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDB = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        saveMenuItem = bottomNavigationView.getMenu().getItem(3);

        nonselect_icon = getResources().getDrawable(R.drawable.ic_outline_save_24);
        select_icon = getResources().getDrawable(R.drawable.ic_baseline_save_24);

        final Intent intent = getIntent();

        mItem = new Item(
                intent.getStringExtra("title"),intent.getStringExtra("mallName"),
                intent.getStringExtra("image"),intent.getStringExtra("link"),
                intent.getStringExtra("price"),intent.getStringExtra("category1"),
                intent.getStringExtra("category2"),intent.getStringExtra("category3"),
                intent.getStringExtra("category4"));

        WebView mWebView = findViewById(R.id.webview);
        WebSettings mWebSettings;
        mWebView.setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
        mWebSettings = mWebView.getSettings(); //세부 세팅 등록
        mWebSettings.setJavaScriptEnabled(true); // 웹페이지 자바스클비트 허용 여부
        mWebSettings.setSupportMultipleWindows(false); // 새창 띄우기 허용 여부
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true); // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        mWebSettings.setLoadWithOverviewMode(true); // 메타태그 허용 여부
        mWebSettings.setUseWideViewPort(true); // 화면 사이즈 맞추기 허용 여부
        mWebSettings.setSupportZoom(false); // 화면 줌 허용 여부
        mWebSettings.setBuiltInZoomControls(false); // 화면 확대 축소 허용 여부
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 컨텐츠 사이즈 맞추기
        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 브라우저 캐시 허용 여부
        mWebSettings.setDomStorageEnabled(true); // 로컬저장소 허용 여부

        mWebView.loadUrl(mItem.link); // 웹뷰에 표시할 웹사이트 주소, 웹뷰 시작

        // Need Already WishList Item
        mDB.collection("users").
                document(mUser.getUid()).
                collection("wishList").
                get().
                addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        Map<String,Object> datafiled = document.getData();
                        String link = (String)datafiled.get("link");
                        if(link.equals(mItem.link)){
                            saveChecked = true;
                            saveMenuItem.setIcon(select_icon);
                        }else{
                            saveChecked = false;
                        }
                    }
                }
            }
        });

        // Bottom Navigation View Control
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem item) {

                switch(item.getItemId()){
                    case R.id.navigation_back:
                        finish();
                        break;
                    case R.id.navigation_save:
                        Map<String, Object> datafiled = new HashMap<>();
                        datafiled.put("link",mItem.link);
                        datafiled.put("title",mItem.title);
                        datafiled.put("mallName",mItem.mallName);
                        datafiled.put("price",mItem.price);
                        datafiled.put("image",mItem.image);
                        datafiled.put("category1",mItem.category1);
                        datafiled.put("category2",mItem.category2);
                        datafiled.put("category3",mItem.category3);
                        datafiled.put("category4",mItem.category4);

                        int value = 0;

                        if(saveChecked) {                   // 저장해제
                            saveMenuItem.setIcon(nonselect_icon);
                            value = -1;
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
                                                        if(title.equals(mItem.title)){
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

                            saveChecked = false;
                        }else{                              // 저장
                            saveMenuItem.setIcon(select_icon);

                            value = 1;

                            mDB.collection("users").document(mUser.getUid()).
                                    collection("wishList").
                                    document().
                                    set(datafiled).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    try {
                                        if(task.isSuccessful()) {
                                            LayoutInflater inflater = getLayoutInflater();
                                            View toastDesign = inflater.inflate(R.layout.custometoastmessage, null);

                                            GradientDrawable drawable =
                                                    (GradientDrawable) getApplicationContext().getDrawable(R.drawable.rounddrawable);
                                            drawable.setColor(Color.rgb(240, 240, 240));
                                            toastDesign.setBackground(drawable);
                                            toastDesign.setClipToOutline(true);

                                            TextView tv1 = toastDesign.findViewById(R.id.toast_title);
                                            tv1.setText(mItem.title);
                                            TextView tv2 = toastDesign.findViewById(R.id.toast_price);
                                            DecimalFormat formatter = new DecimalFormat("###,###");
                                            String s = formatter.format(Integer.valueOf(mItem.price));
                                            s = s + "원";
                                            tv2.setText(s);
                                            ImageView iv = toastDesign.findViewById(R.id.toast_image);
                                            Glide.with(getApplicationContext()).load(mItem.image).into(iv);

                                            Toast toast = new Toast(getApplicationContext());
                                            toast.setView(toastDesign);
                                            toast.show();
                                        }
                                    } catch (Exception e) {
                                        Log.e("ERROR", e.toString());
                                    }
                                }
                            });

                            saveChecked = true;
                        }

                        if(!mItem.category1.equals("면세점")) {
                            final int finalValue = value;
                            mDB.collection("users").document(mUser.getUid()).
                                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot snap = task.getResult();
                                        Map<String, Object> categoryDataFiled = new HashMap<>();
                                        Map<String, Object> readDataFiled = snap.getData();

                                        Long countValue = (Long) readDataFiled.get("count");
                                        Double categoryPercentList[] = new Double[10];

                                        for (int i = 0; i < categoryPercentList.length; i++) {
                                            categoryPercentList[i] = (Double) readDataFiled.get(
                                                    CATEGORY1_LIST[i]);

                                            categoryPercentList[i] = categoryPercentList[i] *
                                                    countValue / 100.0;
                                        }

                                        countValue += finalValue;

                                        for (int i = 0; i < categoryPercentList.length; i++) {
                                            if (CATEGORY1_LIST[i].equals(mItem.category1)) {
                                                categoryPercentList[i] += finalValue;
                                            }

                                            categoryPercentList[i] = categoryPercentList[i] / countValue
                                                    * 100.f;

                                            categoryDataFiled.put(CATEGORY1_LIST[i],
                                                    categoryPercentList[i]);
                                        }

                                        categoryDataFiled.put("count", countValue);

                                        mDB.collection("users").
                                                document(mUser.getUid()).
                                                update(categoryDataFiled).
                                                addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });
                                    }
                                }
                            });
                        }
                        break;
                    case R.id.navigation_share:
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("text/plain");

                        String text = mItem.link;
                        intent.putExtra(Intent.EXTRA_TEXT, text);

                        Intent chooser = Intent.createChooser(intent, "친구에게 공유하기");
                        startActivity(chooser);
                        break;
                    case R.id.navigation_web:
                        Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mItem.link));
                        startActivity(mIntent);
                        break;
                }

                return false;
            }
        });
    }

}