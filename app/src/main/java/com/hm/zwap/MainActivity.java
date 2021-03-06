package com.hm.zwap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hm.zwap.utils.Tools;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private View parent_view;

    private RecyclerView recyclerView;
    private AdapterListSectioned mAdapter;
    DatabaseReference reference;
    List<Thumbnail> items = new ArrayList<>();
    TabLayout tabLayout;
    EditText search;
    Button createBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_caller);
        parent_view = findViewById(android.R.id.content);
        createBtn = findViewById(R.id.createBtn);
        search = (EditText) findViewById(R.id.search_text);
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setUi(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecipieCreateActivity.class);
                startActivity(intent);
            }
        });
        initComponent();
        Tools.setSystemBarColor(this);
    }


    private void initComponent() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

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
                int count = 0;
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                    if (dataSnapshot1.child("img").getValue().toString().equals("") ) {

                        int count_clone = count;
                        StorageReference ref = FirebaseStorage.getInstance().getReference().child("starbucks/default.jpeg");
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Thumbnail tb = new Thumbnail(dataSnapshot1.child("title").getValue().toString(), dataSnapshot1.child("description").getValue().toString(), uri.toString(), dataSnapshot1.child("brandCode").getValue().toString());
                                items.add(tb);
                                Log.d("URI", "onSuccess: " + uri.toString());
                                mAdapter.notifyDataSetChanged();
                            }
                        });
                    } else {
                        Thumbnail tb = new Thumbnail(dataSnapshot1.child("title").getValue().toString(), dataSnapshot1.child("description").getValue().toString(), dataSnapshot1.child("img").getValue().toString(), dataSnapshot1.child("brandCode").getValue().toString());
                        items.add(tb);
                    }
                }

                mAdapter = new AdapterListSectioned(getApplicationContext(), items);
                recyclerView.setAdapter(mAdapter);

                // on item list clicked
                mAdapter.setOnItemClickListener(new AdapterListSectioned.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, Thumbnail obj, int position) {
                        Snackbar.make(parent_view, obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, RecipieContentActivity.class);
                        intent.putExtra("title", items.get(position).name);
                        intent.putExtra("url", items.get(position).img);
                        startActivity(intent);
                    }
                });
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

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

    public void setUi(String tab) {
        switch(tab) {
            case "??????":
                mAdapter.resetItems(items);
                createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, RecipieCreateActivity.class);
                        startActivity(intent);
                    }
                });
                break;
            case "????????????":
                List<Thumbnail> result = items.stream().filter(e -> e.brandCode.equals("ST")).collect(Collectors.toList());
                mAdapter.resetItems(result);
                createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, RecipieCreateActivity.class);
                        startActivity(intent);
                    }
                });


                break;
            default:
                List<Thumbnail> def = items.stream().filter(e -> e.brandCode.equals("others")).collect(Collectors.toList());
                mAdapter.resetItems(def);
                createBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, RecipieOthersCreateActivity.class);
                        startActivity(intent);
                    }
                });
                break;
        }

    }



}
