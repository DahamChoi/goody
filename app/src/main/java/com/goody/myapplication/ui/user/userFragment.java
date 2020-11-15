package com.goody.myapplication.ui.user;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.goody.myapplication.R;
import com.goody.myapplication.ui.login.LoginActivity2;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;

public class userFragment extends Fragment {

    private UserViewModel mViewModel;

    private FirebaseAuth mAuth;

    private TextView userEmailText;
    private TextView userNicknameText;

    public static userFragment newInstance() {
        return new userFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.user_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        userEmailText = root.findViewById(R.id.user_email_text);
        userNicknameText = root.findViewById(R.id.user_nickname_text);
        userEmailText.setText(currentUser.getEmail());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Map<String,Object> datafeild = task.getResult().getData();
                    String nickname = (String)datafeild.get("nickname");

                    userNicknameText.setText(nickname);
                }
            }
        });

        ArrayList<UserItemData> list = new ArrayList<UserItemData>();
        list.add(new UserItemData(R.drawable.ic_baseline_person_24,"이용약관"));
        list.add(new UserItemData(R.drawable.ic_baseline_lock_24,"로그아웃"));

        ListView listView = root.findViewById(R.id.user_listview);
        final MyAdapter myAdapter = new MyAdapter(getActivity(),list);

        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id){
                if(position == 0){
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://goodyprivacypolicy.web.app/"));
                            startActivity(mIntent);
                        }
                    });
                }else if(position == 1){
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder oDialog = new AlertDialog.Builder(getActivity(),
                                    android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);

                            oDialog.setMessage("로그아웃 하시겠습니까?")
                                    .setPositiveButton("아니오", new DialogInterface.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            Log.i("Dialog", "취소");
                                        }
                                    })
                                    .setNeutralButton("예", new DialogInterface.OnClickListener()
                                    {
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            mAuth.signOut();

                                            Intent intent = new Intent(getActivity(), LoginActivity2.class);
                                            startActivity(intent);

                                            ((BottomNavigationView)getActivity().findViewById(R.id.nav_view)).setSelectedItemId(R.id.navigation_home);
                                        }
                                    })
                                    .show();
                        }
                    });
                }
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

    }

}