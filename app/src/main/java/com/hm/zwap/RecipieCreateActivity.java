package com.hm.zwap;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.asksira.dropdownview.DropDownView;
import com.asksira.dropdownview.OnDropDownSelectionListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hm.zwap.Components.QuantityView;
import com.hm.zwap.Model.Recipies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipieCreateActivity extends AppCompatActivity {
    DatabaseReference reference;
    FirebaseDatabase database;
    DataSnapshot mainSnapshot;
    ChipGroup brandGroup, categoryGroup;
    RadioGroup item_radio;
    LinearLayout listView, option_holder, option_wrapper, checkbox_holder;
    String brandCode;
    String categoryCode;
    String itemCode;
    HashMap<String, Integer> optionMap = new HashMap<>();
    String optionCode;
    ColorStateList colorStateList;
    Button create;
    String current_base;
    HashMap<String,Object> combination;
    TextInputEditText title;
    int previous;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chip_filter);
        brandGroup = findViewById(R.id.brand_chip_group);
        categoryGroup = findViewById(R.id.category_chip_group);
        listView = findViewById(R.id.list_holder);
        item_radio = findViewById(R.id.radio_holder);
        option_holder = findViewById(R.id.option_holder);
        option_wrapper = findViewById(R.id.option_wrapper);
        checkbox_holder = findViewById(R.id.checkBox_holder);
        create = findViewById(R.id.create_recipie);
        initComponent();
        //chip.isChecked()
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebasePush();
            }
        });
    }

    private void initComponent() {
        colorStateList = new ColorStateList(
                new int[][]
                        {
                                new int[]{-android.R.attr.state_enabled}, // Disabled
                                new int[]{android.R.attr.state_enabled}   // Enabled
                        },
                new int[]
                        {
                                Color.GRAY, // disabled
                                getResources().getColor(R.color.colorPrimary) // enabled
                        }
        );

        TextInputLayout input_layout_username = findViewById(R.id.input_layout_username);
        //input_layout_username.setError("제목은 20자 이내로 해주세요.");

        title = findViewById(R.id.input_text_title);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().trim().length() > 20){
                    input_layout_username.setErrorEnabled(true);
                    input_layout_username.setError("제목은 20자 이내로 해주세요.");
                } else {
                    input_layout_username.setErrorEnabled(false);
                    input_layout_username.setError("");
                }
            }
        });

        database = FirebaseDatabase.getInstance();

        reference = database.getReference().child("item");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {  //데이터 다 받아왔을때 콜백
                int count = 0;
                mainSnapshot = dataSnapshot;
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                    Chip chip = new Chip(RecipieCreateActivity.this);
                    chip.setText(dataSnapshot1.child("name").getValue().toString());
                    Log.d("TAG1", "onDataChange: " + dataSnapshot1.child("name").getValue().toString());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(2,2,2,2); //substitute parameters for left, top, right, bottom
                    chip.setChipBackgroundColor(colorStateList);
                    chip.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    chip.setElevation(4);
                    chip.setTextColor(getResources().getColor(R.color.white));
                    chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                chip.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            } else {
                                chip.setBackgroundColor(getResources().getColor(R.color.white));
                            }
                        }
                    });
                    chip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            brandCode = dataSnapshot1.getKey().toString();
                            Log.d("TAG", "onDataChange: " + brandCode);
                            addBaseChip(brandCode);
                        }
                    });

                    brandGroup.addView(chip);


                }


                // on item list clicked
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        categoryGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {

            }
        });

    }



    public void addBaseChip(String brandCode) {
        DataSnapshot dataSnapshot = mainSnapshot.child(brandCode).child("item");
        categoryGroup.removeAllViews();
                listView.removeAllViews();
                item_radio.removeAllViews();
                option_holder.removeAllViews();
                checkbox_holder.removeAllViews();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    if (!dataSnapshot1.getKey().equals("O")) {

                        Chip chip = new Chip(RecipieCreateActivity.this);
                        Log.d("TAG2", "onDataChange: " + dataSnapshot1.child("name").getValue().toString());
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(2,2,2,2); //substitute parameters for left, top, right, bottom
                        chip.setChipBackgroundColor(colorStateList);
                        chip.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        chip.setText(dataSnapshot1.child("name").getValue().toString());
                        chip.setLayoutParams(layoutParams);
                        chip.setPadding(5,5,5,5);
                        chip.setElevation(4);
                        chip.setTextColor(getResources().getColor(R.color.white));
                        chip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                categoryCode = dataSnapshot1.getKey().toString();
                                Log.d("TAG3", "onClick: " + categoryCode);
                                addItemList();
                            }
                        });
                        categoryGroup.addView(chip);

                    }


                }

    }


    public void addItemList() {
        DataSnapshot dataSnapshot = mainSnapshot.child(brandCode).child("item").child(categoryCode).child("item");
        listView.removeAllViews();
        item_radio.removeAllViews();
        option_holder.removeAllViews();
        checkbox_holder.removeAllViews();
        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
            RadioButton radioButton = new RadioButton(RecipieCreateActivity.this);
            radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                   if (isChecked) {
                       combination = new HashMap<>();
                       itemCode = dataSnapshot1.getKey().toString();
                       addOptionList();
                       Log.d("아이템코드", "onCheckedChanged: " + itemCode);
                   }
                }
            });

            radioButton.setButtonTintList(colorStateList); // set the color tint lis
            TextView tv = new TextView(RecipieCreateActivity.this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(20, 10, 10, 10); //substitute parameters for left, top, right, bottom
            tv.setLayoutParams(layoutParams);
            tv.setText(dataSnapshot1.child("name").getValue().toString());
            tv.setTextSize(20);
            tv.setTextColor(getResources().getColor(R.color.grey_1000));
            listView.addView(tv);
            item_radio.addView(radioButton);

        }

    }

    public void addOptionList() {
        DataSnapshot dataSnapshot = mainSnapshot.child(brandCode).child("item").child(categoryCode).child("item").child(itemCode).child("option");
        option_holder.removeAllViews();
        checkbox_holder.removeAllViews();
        option_wrapper.removeAllViews();
        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
            String code = dataSnapshot1.getValue().toString();
            if (code.contains("[")) {

                String[] myArray = code.substring(1,code.length()-1).split(",");
                System.out.println("Contents of the array ::"+ Arrays.toString(myArray));
                List <String> myList = Arrays.asList(myArray);
                addOptionView(myList);
            } else {
                addOptionView(code);
            }
        }

    }

    public void addOptionView(Object object) {
        if (object instanceof List) {
            List<String> list = new ArrayList<>();
            Log.d("옵션 리스트", "addOptionView: " + object.toString());
            RadioGroup radioGroup = new RadioGroup(RecipieCreateActivity.this);
            LinearLayout ll = new LinearLayout(RecipieCreateActivity.this);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 6));
            radioGroup.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            RadioButton rd = new RadioButton(RecipieCreateActivity.this);
            rd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    combination.put(object.toString(), -1);
                }
            });
            TextView tv_no = new TextView(RecipieCreateActivity.this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(20, 10, 10, 10); //substitute parameters for left, top, right, bottom
            tv_no.setLayoutParams(layoutParams);
            tv_no.setText("없음");
            tv_no.setTextSize(20);
            tv_no.setTextColor(getResources().getColor(R.color.grey_700));
            tv_no.setSingleLine(true);
            ll.addView(tv_no);
            radioGroup.addView(rd,0);
            for (int i = 0; i < ((List<?>) object).size(); i++) {
                int j = i;
                String code = ((List<?>) object).get(i).toString().replaceAll("\\s+","");
                String name = mainSnapshot.child(code.substring(0,2)).child("item").child(code.substring(2,3)).child("item").child(code.substring(3,5)).child("name").getValue().toString();
                list.add(name);
                RadioButton radioButton = new RadioButton(RecipieCreateActivity.this);
                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            combination.put(object.toString(), code);
                        }
                    }
                });
                TextView tv = new TextView(RecipieCreateActivity.this);
                layoutParams.setMargins(20, 10, 10, 10); //substitute parameters for left, top, right, bottom
                tv.setLayoutParams(layoutParams);
                tv.setText(name);
                tv.setSingleLine(true);
                tv.setTextSize(20);
                tv.setTextColor(getResources().getColor(R.color.grey_1000));
                ll.addView(tv);

                radioGroup.addView(radioButton,i +1);
            }
            LinearLayout wrapper = new LinearLayout(RecipieCreateActivity.this);
            wrapper.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            wrapper.setOrientation(LinearLayout.HORIZONTAL);
            wrapper.addView(ll);
            wrapper.addView(radioGroup);
            option_wrapper.addView(wrapper);
            View view = new View(RecipieCreateActivity.this);
            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
            view.setBackgroundColor(getResources().getColor(R.color.black));
            option_wrapper.addView(view);
//            DropDownView dd = new DropDownView(RecipieCreateActivity.this);
//            //dd.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            dd.setDropDownListItem(list);
//
//            ViewGroup.LayoutParams lp =dd.getFilterArrow().getLayoutParams();
//            lp.width = 0;
//            dd.getFilterArrow().setLayoutParams(lp);
//            dd.getFilterContainer().setHorizontalGravity(Gravity.LEFT);
//            dd.getFilterTextView().setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
//            dd.getFilterTextView().setGravity(Gravity.LEFT);
//            dd.getFilterTextView().setText(list.get(0));
//            dd.getFilterTextView().setTextColor(getResources().getColor(R.color.grey_600));
//            dd.setDropdownItemGravity(Gravity.LEFT);
//            dd.setEnabled(false);
//            dd.setOnSelectionListener(new OnDropDownSelectionListener() {
//                @Override
//                public void onItemSelected(DropDownView view, int position) {
//                    ((List<?>) object).get(position).toString().replaceAll("\\s+","");
//
//                    //Do something with the selected position
//                    //If position is -1, it means nothing is selected. This should happen only if deselectable is true
//                }
//            });


//            CheckBox cb = new CheckBox(RecipieCreateActivity.this);
//            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                    if (isChecked) {
//                        dd.setEnabled(true);
//                        dd.expand(true);
//                        dd.getFilterTextView().setTextColor(getResources().getColor(R.color.black));
//
//                    } else {
//                        dd.setEnabled(false);
//                        dd.collapse(true);
//                        dd.getFilterTextView().setTextColor(getResources().getColor(R.color.grey_600));
//
//                    }
//                }
//            });
//            cb.setButtonTintList(colorStateList);
//            option_holder.addView(dd);
//            checkbox_holder.addView(cb);
        } else {
            String code = String.valueOf(object);
            QuantityView qv = new QuantityView(RecipieCreateActivity.this);
            CheckBox cb = new CheckBox(RecipieCreateActivity.this);
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        optionCode = String.valueOf(object).substring(3,5) ;
                        combination.put(code, 1);
                    } else {
                        combination.put(code, 0);
                    }
                }
            });
            String name = mainSnapshot.child(code.substring(0,2)).child("item").child(code.substring(2,3)).child("item").child(code.substring(3,5)).child("name").getValue().toString();
            TextView textView = (TextView) qv.findViewById(R.id.tv_qty);
            textView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int n = Integer.valueOf(s.toString());
                    combination.put(code, n);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            cb.setButtonTintList(colorStateList); // set the color tint lis
            TextView tv = new TextView(RecipieCreateActivity.this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(20, 10, 1 + 0, 10); //substitute parameters for left, top, right, bottom
            tv.setLayoutParams(layoutParams);
            tv.setText(name);
            tv.setTextSize(20);
            tv.setSingleLine(true);
            tv.setTextColor(getResources().getColor(R.color.grey_1000));
            option_holder.addView(tv);
            if (name.contains("(횟수)") || name.contains("(샷)") || name.contains("(펌프)")) {
                checkbox_holder.addView((qv));
            } else {
                checkbox_holder.addView(cb);
            }
        }
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

    public void onChipClick(View view) {
//        Chip chip = (Chip) view;
//        chip.setChecked(!chip.isChecked());
    }

    public void firebasePush() {
    DatabaseReference mDatabase = database.getReference().child("recipies");

    List<String> combination_list = new ArrayList<>();
    combination_list.add(brandCode + categoryCode + itemCode);

    for (Map.Entry<String, Object> entry : combination.entrySet()) {
        System.out.println("[Key]:" + entry.getKey() + " [Value]:" + entry.getValue());
        if(entry.getValue() instanceof Integer ) {
            if((Integer)entry.getValue()>0) {
                for (int i = 0; i < (Integer)entry.getValue(); i++) {
                    combination_list.add(entry.getKey());
                }
            }

        } else {

        }
    }

    mDatabase.push().setValue(new Recipies(title.getText().toString(), "설명", "http://coffee.dankook.ac.kr/html_portlet_repositories/images/ExtImgFile/10158/1766093/1952287/42012.png", 0,0,0, combination_list));
    }
}
