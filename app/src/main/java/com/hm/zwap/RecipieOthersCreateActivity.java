package com.hm.zwap;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
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

public class RecipieOthersCreateActivity extends AppCompatActivity {
    DatabaseReference reference;
    FirebaseDatabase database;
    DataSnapshot mainSnapshot;
    HashMap<String, Integer> optionMap = new HashMap<>();
    String optionCode;
    ColorStateList colorStateList;
    Button create;
    String current_base;
    TextInputEditText title, combination, description;
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
        setContentView(R.layout.activity_create_other);
        create = findViewById(R.id.create_recipie);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressbar);
        backBtn = findViewById(R.id.back_create_other);
        initComponent();
        tedPermission();
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
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initComponent() {

        title = findViewById(R.id.input_text_title);
        combination = findViewById(R.id.input_text_combination);
        description = findViewById(R.id.input_text_description);


        database = FirebaseDatabase.getInstance();

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
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
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
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
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

                Cursor cursor = this.getContentResolver().query(data.getData(), new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null);
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
    combination_list.add("custom");
    combination_list.add(combination.getText().toString());
    mDatabase.push().setValue(new Recipies(title.getText().toString(), description.getText().toString(), current_img_url, 0,0,0, combination_list, "others"));
    }
}
