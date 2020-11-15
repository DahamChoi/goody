package com.goody.myapplication.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.goody.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SelectCategoryActivity extends AppCompatActivity {

    private CardView cardView[] = new CardView[12];
    private boolean isSelected[] = new boolean[12];

    private GridLayout catagoryGrid;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private FirebaseAuth mAuth;

    private String CATEGORY1_LIST[] = {
            "패션의류","패션잡화","화장품미용","디지털가전","가구인테리어",
            "출산육아","식품","스포츠레저","생활건강","여가생활편의"
    };

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        mAuth = FirebaseAuth.getInstance();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        catagoryGrid = findViewById(R.id.catagory_grid);
        for(int i = 0; i < catagoryGrid.getChildCount() - 1; i++){
            final CardView card = (CardView)catagoryGrid.getChildAt(i);
            final int finalI = i;
            card.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    TextView tv = (TextView)card.getChildAt(0);

                    if(isSelected[finalI]){                             //  선택 해제
                        card.setCardBackgroundColor(ContextCompat.getColor(SelectCategoryActivity.this,R.color.colorBackGround));
                        tv.setTextColor(ContextCompat.getColor(SelectCategoryActivity.this,R.color.colorEmpty));

                        isSelected[finalI] = false;

                        boolean check = false;
                        for(int i = 0; i < isSelected.length; i++){
                            if(isSelected[i]){
                                check = true;
                            }
                        }

                    }else{                                              // 선택
                        card.setCardBackgroundColor(ContextCompat.getColor(SelectCategoryActivity.this,R.color.colorEmpty));
                        tv.setTextColor(ContextCompat.getColor(SelectCategoryActivity.this,R.color.colorBackGround));

                        isSelected[finalI] = true;
                    }
                }
            });
        }

        findViewById(R.id.nav_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = false;
                for(int i = 0; i < isSelected.length; i++){
                    if(isSelected[i]){
                        check = true;
                    }
                }

                if(check) {
                    saveCatagoryData();
                }
            }
        });
    }

    public void saveCatagoryData(){
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        int count = 0;

        Map<String,Object> datafiled = new HashMap<>();
        for(int i = 0; i < isSelected.length - 2; i++){
            if(isSelected[i]) {
                count += 10;
            }
        }

        for(int i = 0; i < isSelected.length - 2; i++){
            if(isSelected[i]) {
                datafiled.put(CATEGORY1_LIST[i],10.f / count * 100.f);
            }
            else{
                datafiled.put(CATEGORY1_LIST[i],0.f);
            }
        }

        datafiled.put("count",count);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("잠시만 기다려주세요!");
        progressDialog.setProgressStyle(R.style.Widget_AppCompat_ProgressBar_Horizontal);
        progressDialog.show();

        db.collection("users").
                document(user.getUid()).
                update(datafiled).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    finish();
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(),"오류가 발생했습니다",Toast.LENGTH_SHORT);
                }
            }
        });
    }
}