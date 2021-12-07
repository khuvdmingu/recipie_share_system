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
    }

    private void initComponent() {
        TextInputLayout input_layout_username = findViewById(R.id.input_layout_username);
        //input_layout_username.setError("제목은 20자 이내로 해주세요.");

        title = findViewById(R.id.input_text_title);
        combination = findViewById(R.id.input_text_combination);
        description = findViewById(R.id.input_text_description);
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

    public void doTakePhotoAction() // 카메라 촬영 후 이미지 가져오기
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // 임시로 사용할 파일의 경로를 생성
        tempFile = null;
        try{
            tempFile = createImageFile();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "이미지 오류", Toast.LENGTH_SHORT);
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

        // 이미지 파일 이름 ( blackJin_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "zap" + timeStamp + ".png";

        // 이미지가 저장될 폴더 이름 ( blackJin )
        //File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/path/");

        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/path/" + imageFileName);
        if (!storageDir.exists()) storageDir.mkdirs();
        // 빈 파일 생성

        return storageDir;
    }

    public void doTakeAlbumAction() // 앨범에서 이미지 가져오기

    {
        // 앨범 호출 인텐트 생성
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        // 후 액티비티 실행
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //액티비티 결과
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri=Uri.parse(result.getUri().toString());
                Log.d(TAG, "onActivityResult: 이미지 URI " + resultUri.toString());
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
                // 이후의 처리가 카메라와 같으므로 일단  break없이 진행

                Cursor cursor = this.getContentResolver().query(data.getData(), new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null);
                cursor.moveToFirst();

                mImageCaptureUri = Uri.fromFile(new File(cursor.getString(0)));
                cursor.close();
                Log.d("이미지 경로",getRealPathFromURI(mImageCaptureUri));
            }


            case PICK_FROM_CAMERA:
            {
                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정
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

        // 폴더를 생성하여 이미지를 저장하는 방식이다.
        // 저장 경로 불러오기
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + CAPTURE_PATH;
        File directory_SmartWheel = new File(dirPath);

        if(!directory_SmartWheel.exists()) // SmartWheel 디렉터리에 폴더가 없다면 (새로 이미지를 저장할 경우에 속한다.)
            //새로 생성
            directory_SmartWheel.mkdir();
        File copyFile = new File(filePath);

        BufferedOutputStream out = null;

        try {

            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            // sendBroadcast를 통해 Crop된 사진을 앨범에 보이도록 갱신한다.
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
            // 예외 처리
            e.printStackTrace();

        }
        Log.d("file path", filePath);
        Uri imageUri = Uri.fromFile(copyFile);
    }



    /**
     *  권한 설정
     */
    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true;

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
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
        // 카메라 버튼 리스너
        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 버튼 클릭시 doTakePhotoAction() 메서드 실행
                if(isPermission)  doTakePhotoAction();
                else Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();

            }
        };
        //앨범 버튼 리스너
        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 버튼 클릭시 doTakeAlbumAction() 메서드 실행
                if(isPermission) doTakeAlbumAction();
                else Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_2), Toast.LENGTH_LONG).show();

            }
        };
        // 취소버튼 리스너
        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 버튼 클릭시 dialog.dismiss() 실행
                dialog.dismiss();
            }

        };

        new AlertDialog.Builder(this,R.style.Theme_AppCompat_Dialog_Alert)
                .setTitle("업로드할 이미지 선택")
                .setPositiveButton("사진촬영", cameraListener)
                .setNeutralButton("취소", cancelListener)
                .setNegativeButton("앨범선택", albumListener)
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
                        Log.d(TAG, "onSuccess:다운로드 url" + url);
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
