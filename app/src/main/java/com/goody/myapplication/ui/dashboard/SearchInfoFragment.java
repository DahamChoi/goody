package com.goody.myapplication.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.goody.myapplication.Item;
import com.goody.myapplication.R;
import com.goody.myapplication.ui.WebViewActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class SearchInfoFragment extends Fragment {

    private String searchTitle;
    private String mallName;
    private String searchSort;

    private LinearLayout rootLinear;
    private ScrollView searchInfoScroll;
    private ProgressBar progressBar;

    private SwipeRefreshLayout swipe;

    private int searchIndex = 1;
    static final int searchDisplayCount = 30;

    public SearchInfoFragment(String searchTitle,String mallName,String searchSort) {
        // Required empty public constructor
        this.searchTitle = searchTitle;
        this.mallName = mallName;
        this.searchSort = searchSort;
    }

    // TODO: Rename and change types and number of parameters

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root =
                inflater.inflate(R.layout.fragment_search_info, container, false);
        rootLinear = root.findViewById(R.id.search_info_linear);
        searchInfoScroll = root.findViewById(R.id.search_info_scroll);
        progressBar = (ProgressBar)root.findViewById(R.id.progressBar);
        swipe = root.findViewById(R.id.search_info_swipe_layout);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                searchIndex = 30;
                rootLinear.removeAllViews();
                swipe.setRefreshing(false);
                progressBar.setVisibility(View.VISIBLE);

                MainPageTask task = new MainPageTask(mallName + " " + searchTitle,searchSort,searchIndex);
                task.execute();
            }
        });

        searchInfoScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int scrollY = searchInfoScroll.getScrollY(); // For ScrollView

                View v = searchInfoScroll.getChildAt(searchInfoScroll.getChildCount() - 1);
                if((v.getBottom() - (searchInfoScroll.getHeight() + scrollY)) == 0){
                    // End
                    try {
                        progressBar.setVisibility(View.VISIBLE);

                        MainPageTask task = new MainPageTask(mallName + " " + searchTitle,searchSort,searchIndex);
                        task.execute();

                        if(searchIndex < 950) {
                            searchIndex += searchDisplayCount;
                        }

                    } catch (Exception e) {
                        Log.d("ERROR", e.toString());
                    }
                }

            }
        });

        try {
            MainPageTask task = new MainPageTask(mallName + " " + searchTitle,searchSort,searchIndex);
            task.execute();

            searchIndex += searchDisplayCount;

        } catch (Exception e) {
            Log.d("ERROR", e.toString());
        }

        // Inflate the layout for this fragment
        return root;
    }

    private class MainPageTask extends AsyncTask<Void, Void, ArrayList<Item>> {

        String searchObject;
        String searchSort;
        int startIndex;

        MainPageTask(String search,String searchSort,int startIndex) {
            this.searchObject = search;
            this.searchSort = searchSort;
            this.startIndex = startIndex;
        }

        @Override
        protected void onPostExecute(ArrayList<Item> items) {
            super.onPostExecute(items);

            for (final Item info : items) {
                LayoutInflater inf = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                FrameLayout searchData = (FrameLayout)inf.inflate(R.layout.searchdata, null);

                LinearLayout horLinear = searchData.findViewById(R.id.searchdata_horizontal_linear);
                horLinear.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), WebViewActivity.class);
                        intent.putExtra("link",info.link);
                        intent.putExtra("title",info.title);
                        intent.putExtra("mallName",info.mallName);
                        intent.putExtra("image",info.image);
                        intent.putExtra("price",info.price);
                        intent.putExtra("category1",info.category1);
                        intent.putExtra("category2",info.category2);
                        intent.putExtra("category3",info.category3);
                        intent.putExtra("category4",info.category4);
                        getActivity().startActivity(intent);
                    }
                });

                ImageView imageView = searchData.findViewById(R.id.searchdata_img);
                Glide.with(getActivity()).load(info.image).into(imageView);

                TextView tv1 = searchData.findViewById(R.id.searchdata_title);
                TextView tv2 = searchData.findViewById(R.id.searchdata_mallname);
                TextView tv3 = searchData.findViewById(R.id.searchdata_price);

                tv1.setText(Html.fromHtml(info.title));
                tv2.setText(info.mallName);

                DecimalFormat formatter = new DecimalFormat("###,###");
                String s = formatter.format(Integer.valueOf(info.price));
                s = s + "원";

                tv3.setText(s);

                rootLinear.addView(searchData);
            }

            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Item> doInBackground(Void... voids) {

            ArrayList<Item> itemInfoList = new ArrayList<Item>();

            final String clientId = "ntvxCZLKZOsFEP1la0sf";//애플리케이션 클라이언트 아이디값";
            final String clientSecret = "lld52ajljo";//애플리케이션 클라이언트 시크릿값";
            final int display = 30; // 보여지는 검색결과의 수

            try {
                String text = URLEncoder.encode(searchObject, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/search/shop.json?query=" +
                        text + "&display=" + display + "&sort=" + searchSort + "&start=" + startIndex; // json 결과
                // Json 형태로 결과값을 받아옴.
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                con.connect();

                int responseCode = con.getResponseCode();

                BufferedReader br;
                if (responseCode == 200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }

                StringBuilder searchResult = new StringBuilder();
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    searchResult.append(inputLine + "\n");

                }
                br.close();
                con.disconnect();

                String data = searchResult.toString();
                String[] array;
                array = data.split("\"");

                ArrayList<String> title = new ArrayList<String>();
                ArrayList<String> link = new ArrayList<String>();
                ArrayList<String> mallName = new ArrayList<String>();
                ArrayList<String> image = new ArrayList<String>();
                ArrayList<String> lprice = new ArrayList<String>();

                ArrayList<String> category1 = new ArrayList<String>();
                ArrayList<String> category2 = new ArrayList<String>();
                ArrayList<String> category3 = new ArrayList<String>();
                ArrayList<String> category4 = new ArrayList<String>();

                int k = 0;
                for (int i = 0; i < array.length; i++) {
                    if (array[i].equals("title"))
                        title.add(array[i + 2]);
                    if (array[i].equals("link"))
                        link.add(array[i + 2]);
                    if (array[i].equals("mallName"))
                        mallName.add(array[i + 2]);
                    if (array[i].equals("image"))
                        image.add(array[i + 2]);
                    if (array[i].equals("lprice")) {
                        lprice.add(array[i + 2]);
                    }
                    if(array[i].equals("category1")){
                        category1.add(array[i + 2]);
                    }
                    if(array[i].equals("category2")){
                        category2.add(array[i + 2]);
                    }
                    if(array[i].equals("category3")){
                        category3.add(array[i + 2]);
                    }
                    if(array[i].equals("category4")){
                        category4.add(array[i + 2]);
                    }
                }

                for(int i = 0; i < title.size(); i++){
                    title.set(i,title.get(i).replaceAll("\\<.*?\\>", ""));

                    category1.set(i,category1.get(i).replace("/",""));
                    category2.set(i,category2.get(i).replace("/",""));
                    category3.set(i,category3.get(i).replace("/",""));
                    category4.set(i,category4.get(i).replace("/",""));

                    /*
                    DecimalFormat formatter = new DecimalFormat("###,###");
                    lprice.set(i,formatter.format(Integer.valueOf(lprice.get(i))));
                    lprice.set(i,lprice.get(i) + " 원");*/

                    itemInfoList.add(
                            new Item(title.get(i),mallName.get(i),image.get(i),link.get(i),
                                    lprice.get(i),category1.get(i),category2.get(i),
                                    category3.get(i),category4.get(i)));
                }
            } catch (Exception e) {
                Log.e("TAG", "error : " + e);
            }

            return itemInfoList;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);


        }
    }
}