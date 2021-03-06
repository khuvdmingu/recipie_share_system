package com.hm.zwap;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.asksira.dropdownview.DropDownView;
import com.asksira.dropdownview.OnDropDownSelectionListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.hm.zwap.Components.QuantityView;
import com.hm.zwap.Model.Recipies;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    TextInputEditText title, description;
    ImageView imageView;
    String current_img_url ;
    ProgressBar progressBar;
    ImageButton backBtn;
    private static final String CAPTURE_PATH = "/zap";
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;
    private String absolutePath;
    private Uri mImageCaptureUri;
    int previous;
    private static final String TAG = "zap";

    private Boolean isPermission = true;

    private Boolean isCamera = false;
    private File tempFile;
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
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressbar);
        backBtn = findViewById(R.id.back_create);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initComponent();
        tedPermission();
        //chip.isChecked()
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebasePush();
                finish();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPicClick();
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



        title = findViewById(R.id.input_text_title);
        description = findViewById(R.id.input_text_description);



        database = FirebaseDatabase.getInstance();

        reference = database.getReference().child("item");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {  //????????? ??? ??????????????? ??????
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
                       Log.d("???????????????", "onCheckedChanged: " + itemCode);
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
            Log.d("?????? ?????????", "addOptionView: " + object.toString());
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
            tv_no.setText("??????");
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
            if (name.contains("(??????)") || name.contains("(???)") || name.contains("(??????)")) {
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

    public void doTakePhotoAction() // ????????? ?????? ??? ????????? ????????????
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // ????????? ????????? ????????? ????????? ??????
        tempFile = null;
        try{
            tempFile = createImageFile();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "????????? ??????", Toast.LENGTH_SHORT);
        }


        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        //mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
        if (tempFile != null) {
            mImageCaptureUri = FileProvider.getUriForFile(this, getPackageName(), tempFile);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }

    }
    private File createImageFile() throws IOException {
        File dir = new File(Environment.getExternalStorageDirectory() + "/path/");
        if (!dir.exists()) { dir.mkdirs(); }

        // ????????? ?????? ?????? ( blackJin_{??????}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "zap" + timeStamp + ".png";

        // ???????????? ????????? ?????? ?????? ( blackJin )
        //File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/path/");

        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/path/" + imageFileName);
        if (!storageDir.exists()) storageDir.mkdirs();
        // ??? ?????? ??????

        return storageDir;
    }

    public void doTakeAlbumAction() // ???????????? ????????? ????????????

    {
        // ?????? ?????? ????????? ??????
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        // ??? ???????????? ??????
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //???????????? ??????
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri=Uri.parse(result.getUri().toString());
                Log.d(TAG, "onActivityResult: ????????? URI " + resultUri.toString());
                imageUpload(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


        if(resultCode != RESULT_OK)
            return;


        switch(requestCode)
        {
            case PICK_FROM_ALBUM:
            {
                // ????????? ????????? ???????????? ???????????? ??????  break?????? ??????

                Cursor cursor = this.getContentResolver().query(data.getData(), new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                cursor.moveToFirst();

                mImageCaptureUri = Uri.fromFile(new File(cursor.getString(0)));
                cursor.close();
                Log.d("????????? ??????",getRealPathFromURI(mImageCaptureUri));
            }


            case PICK_FROM_CAMERA:
            {
                // ???????????? ????????? ????????? ??????????????? ????????? ????????? ??????
                CropImage.activity(mImageCaptureUri)
                        .start(this);
                //imageView.setImageURI(mImageCaptureUri);
                break;
            }

        }
    }
    private String getRealPathFromURI(Uri contentUri) {
        if (contentUri.getPath().startsWith("/storage")) {
            return contentUri.getPath();
        }
        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns = { MediaStore.Files.FileColumns.DATA };
        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null);
        try { int columnIndex = cursor.getColumnIndex(columns[0]);
            if (cursor.moveToFirst()) { return cursor.getString(columnIndex); } }
        finally { cursor.close(); }
        return null; }



    private void storeCropImage(Bitmap bitmap, String filePath) {

        // ????????? ???????????? ???????????? ???????????? ????????????.
        // ?????? ?????? ????????????
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + CAPTURE_PATH;
        File directory_SmartWheel = new File(dirPath);

        if(!directory_SmartWheel.exists()) // SmartWheel ??????????????? ????????? ????????? (?????? ???????????? ????????? ????????? ?????????.)
            //?????? ??????
            directory_SmartWheel.mkdir();
        File copyFile = new File(filePath);

        BufferedOutputStream out = null;

        try {

            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            // sendBroadcast??? ?????? Crop??? ????????? ????????? ???????????? ????????????.
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(copyFile)));
            MediaScannerConnection.scanFile( getApplicationContext(),

                    new String[]{copyFile.getAbsolutePath()},

                    null,

                    new MediaScannerConnection.OnScanCompletedListener(){

                        @Override

                        public void onScanCompleted(String path, Uri uri) {

                            Log.v("File scan", "file:" + path + "was scanned seccessfully");

                        }

                    });
            out.flush();
            out.close();
        } catch (Exception e) {
            // ?????? ??????
            e.printStackTrace();

        }
        Log.d("file path", filePath);
        Uri imageUri = Uri.fromFile(copyFile);
    }



    /**
     *  ?????? ??????
     */
    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // ?????? ?????? ??????
                isPermission = true;

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // ?????? ?????? ??????
                isPermission = false;

            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

    }

    public void onPicClick() {
        // ????????? ?????? ?????????
        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ?????? ????????? doTakePhotoAction() ????????? ??????
                if(isPermission)  doTakePhotoAction();
                else Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();

            }
        };
        //?????? ?????? ?????????
        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ?????? ????????? doTakeAlbumAction() ????????? ??????
                if(isPermission) doTakeAlbumAction();
                else Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();

            }
        };
        // ???????????? ?????????
        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ?????? ????????? dialog.dismiss() ??????
                dialog.dismiss();
            }

        };

        new AlertDialog.Builder(this,R.style.Theme_AppCompat_Dialog_Alert)
                .setTitle("???????????? ????????? ??????")
                .setPositiveButton("????????????", cameraListener)
                .setNeutralButton("??????", cancelListener)
                .setNegativeButton("????????????", albumListener)
                .show();
    }

    public void imageUpload(Uri file){
        imageView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String filename = System.currentTimeMillis() + ".jpg";
        StorageReference mountainsRef = storageRef.child(filename);
        UploadTask uploadTask = mountainsRef.putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String url = uri.toString();
                        Log.d(TAG, "onSuccess:???????????? url" + url);
                        Glide.with(getApplicationContext()).load(url).into(imageView);
                        progressBar.setVisibility(View.INVISIBLE);
                        imageView.setVisibility(View.VISIBLE);
                        current_img_url = url;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
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
            combination_list.add(entry.getValue().toString());
        }
    }
    mDatabase.push().setValue(new Recipies(title.getText().toString(), description.getText().toString(), current_img_url, 0,0,0, combination_list, brandCode));
    }
}
