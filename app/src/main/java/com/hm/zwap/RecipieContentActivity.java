package com.hm.zwap;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hm.zwap.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecipieContentActivity extends AppCompatActivity {

    private View parent_view;

    DatabaseReference reference;
    DatabaseReference reference_item;
    List<Thumbnail> items = new ArrayList<>();
    String title, url;

    TextView description, goodCount, sosoCount, badCount, titleView;
    ImageView mainImage;
    LinearLayout combinationLayout;
    ImageButton goodBtn, badBtn, sosoBtn, backBtn;

    String key;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipie_content);
        title = getIntent().getStringExtra("title");
        url = getIntent().getStringExtra("url");
        titleView = findViewById(R.id.title);
        titleView.setText(title);
        description = findViewById(R.id.description_content);
        goodCount = findViewById(R.id.goodCount);
        sosoCount = findViewById(R.id.sosoCount);
        badCount = findViewById(R.id.badCount);
        goodBtn = findViewById(R.id.goodBtn);
        badBtn = findViewById(R.id.badBtn);
        sosoBtn = findViewById(R.id.sosoBtn);

        backBtn = findViewById(R.id.back_arrow);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        goodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                update("good");
            }
        });

        badBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update("bad");
            }
        });

        sosoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update("soso");
            }
        });

        mainImage = findViewById(R.id.main_image);
        Glide.with(getApplicationContext())
                .load(url)
                .into(mainImage);
        combinationLayout = findViewById(R.id.combination_holder);
        initComponent(title);
        Tools.setSystemBarColor(this);
    }

    public void update(String btnName) {
        switch(btnName) {
            case "good":
                reference.child(key).child("good").setValue(Integer.parseInt(goodCount.getText().toString())+1);
                goodCount.setText(String.valueOf(Integer.parseInt(goodCount.getText().toString()) +1));
                break;
            case "soso":
                reference.child(key).child("soso").setValue(Integer.parseInt(sosoCount.getText().toString())+1);
                sosoCount.setText(String.valueOf(Integer.parseInt(sosoCount.getText().toString()) +1));
                break;
            case "bad":
                reference.child(key).child("bad").setValue(Integer.parseInt(badCount.getText().toString())+1);
                badCount.setText(String.valueOf(Integer.parseInt(badCount.getText().toString()) +1));
                break;
            default:
                break;
        }
    }


    private void initComponent( String title) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();


        reference = database.getReference().child("recipies");

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("????????? ???????????? ???...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {  //????????? ??? ??????????????? ??????

                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    if(dataSnapshot1.child("title").getValue().toString().equals(title)) {
                        key = dataSnapshot1.getKey();

                        description.setText(dataSnapshot1.child("description").getValue().toString());
                        badCount.setText(dataSnapshot1.child("bad").getValue().toString());
                        sosoCount.setText(dataSnapshot1.child("soso").getValue().toString());
                        goodCount.setText(dataSnapshot1.child("good").getValue().toString());
                        HashMap<String, JSONObject> dataSnapshotValue = (HashMap<String, JSONObject>) dataSnapshot1.getValue();
                        String jsonString = new Gson().toJson(dataSnapshotValue);

                        Log.d("????????????", "onDataChange: " + jsonString);

                        try {
                            JSONObject jsonObject = new JSONObject(jsonString) ;
                            String first = jsonObject.getJSONArray("combination").get(0).toString();

                            if (first.equals("custom")) {
                                TextView tv = new TextView(getApplicationContext());
                                tv.setText(jsonObject.getJSONArray("combination").get(1).toString());
                                tv.setTextSize(16);
                                combinationLayout.addView(tv);
                            } else {
                                for (int i = 0; i < jsonObject.getJSONArray("combination").length(); i++) {
                                    String item = jsonObject.getJSONArray("combination").get(i).toString();

                                    int j = i;

                                    reference_item = database.getReference().child("item").child(item.substring(0,2)).child("item").child(item.substring(2,3)).child("item").child(item.substring(3)).child("name");
                                    reference_item.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            //Log.d("??????", "onDataChange: " + snapshot.getValue().toString());
                                            LinearLayout ll = new LinearLayout(getApplicationContext());
                                            ll.setOrientation(LinearLayout.HORIZONTAL);
                                            if (j != 0) {
                                                TextView plus = new TextView(getApplicationContext());
                                                plus.setText("+ ");
                                                plus.setTextSize(20);
                                                plus.setTypeface(Typeface.DEFAULT_BOLD);
                                                plus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                                                ll.addView(plus);
                                            }
                                            TextView tv = new TextView(getApplicationContext());
                                            tv.setText(snapshot.getValue().toString());
                                            tv.setTextSize(16);

                                            ll.addView(tv);
                                            combinationLayout.addView(ll);

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }
                            }


                            } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_search_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}
