package com.example.whereto;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CheckInActivity extends AppCompatActivity {

    private ImageView camereIv;
    private Button bt_upload;

    private TextView tv_camera;
    private TextView tv_photo;

    private PopupWindow pw_image;
    private Button bt_privacy;
    private PopupWindow pw_privacy;

    private String TAG = "tag";
    //need the permission to read the storage and camera
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        //get the permission to read the camera
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        // click camera icon
        bt_upload = findViewById(R.id.bt_upload);
        bt_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.activity_image_popup_window,null);
                pw_image = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                pw_image.setOutsideTouchable(true);
                pw_image.showAsDropDown(bt_upload);

                //set Camera:
                tv_camera = view.findViewById(R.id.tv_camera);
                tv_photo = view.findViewById(R.id.tv_album);
                initView();
            }
        });

        //click share privacy
        bt_privacy = findViewById(R.id.bt_share);
        bt_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getLayoutInflater().inflate(R.layout.activity_sharing_privacy_setting,null);
                pw_privacy = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                pw_privacy.setOutsideTouchable(true);
                pw_privacy.showAsDropDown(bt_upload);

                //set public onclick
                TextView textView = view.findViewById(R.id.tv_public);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pw_privacy.dismiss();
                        bt_privacy.setText(textView.getText());
                    }
                });
                //set friends onclick
                TextView textView_fiend = view.findViewById(R.id.tv_friend);
                textView_fiend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pw_privacy.dismiss();
                        bt_privacy.setText("Friend");
                    }
                });
            }
        });


    }


    private Uri ImageUri;
    public static final int TAKE_PHOTO = 101;
    public static final int TAKE_CAMARA = 100;

    private void initView() {


        camereIv = (ImageView) findViewById(R.id.camere_iv);


        tv_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check whether get the permission to use camera
                if (verifyPermissions(CheckInActivity.this, PERMISSIONS_STORAGE[2]) == 0) {
                    Log.i(TAG, "need permission");
                    ActivityCompat.requestPermissions(CheckInActivity.this, PERMISSIONS_STORAGE, 3);
                } else {
                    //got the permission already
                    toCamera();  //open camera
                }
            }
        });
        tv_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPicture();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        //show photo to the image View from camera
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(ImageUri));
                        camereIv.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case TAKE_CAMARA:
                if (resultCode == RESULT_OK) {
                    try {
                        //show photo to the image View from album
                        Uri uri_photo = data.getData();
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri_photo));
                        camereIv.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }


    /**
     * check the permission
     *
     * @param activity
     * @param permission
     * @return
     */
    public int verifyPermissions(Activity activity, java.lang.String permission) {
        int Permission = ActivityCompat.checkSelfPermission(activity, permission);
        if (Permission == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "got the permission already");
            return 1;
        } else {
            Log.i(TAG, "please access the permission");
            return 0;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "got the permission");
            toCamera();
        } else {
            Log.i(TAG, "no permission");
        }
    }

    //use album
    private void toPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);  //跳转到 ACTION_IMAGE_CAPTURE
        intent.setType("image/*");
        startActivityForResult(intent, TAKE_CAMARA);
        Log.i(TAG, "success to use album");
    }

    //use camera
    private void toCamera() {
        //create the file to store the camera
//        File outputImage = new File(getExternalCacheDir(), "outputImage.jpg");
        File outputImage = new File(getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
        if (outputImage.exists()) {
            outputImage.delete();
        } else {
            try {
                outputImage.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //get SDK version
        if (Build.VERSION.SDK_INT >= 24) {
            ImageUri = FileProvider.getUriForFile(CheckInActivity.this, "com.mooc.uploadfile4.fileprovider", outputImage);
        } else {
            ImageUri = Uri.fromFile(outputImage);
        }

        //launch the camera program
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }
}
