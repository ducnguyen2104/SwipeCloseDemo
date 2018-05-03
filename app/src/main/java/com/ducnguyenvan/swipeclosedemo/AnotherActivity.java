package com.ducnguyenvan.swipeclosedemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import java.util.ArrayList;

public class AnotherActivity extends AppCompatActivity {

    //private TextView txt;
    private RecyclerView recyclerView;
    private ArrayList<ItemRow> items;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_another);
        //txt = (TextView)findViewById(R.id.txt);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        items = new ArrayList<>();
        for (int i = 0; i < 30; i ++) {
            items.add(new ItemRow("" + (i + 1)));
        }
        final MyAdapter myAdapter = new MyAdapter(items,this);
        recyclerView.setAdapter(myAdapter);
    }
}
