package com.example.myapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

public class Camera2 extends AppCompatActivity {

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private ImageView imageView;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        imageView = findViewById(R.id.imageView);
        Button btnCamera = findViewById(R.id.btnCamera);
        Button btnLibrary = findViewById(R.id.btnLibrary);

        // Yêu cầu quyền camera
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Quyền truy cập camera bị từ chối!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Chọn ảnh từ gallery
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                startImageCrop(uri);
            } else {
                Toast.makeText(this, "Không chọn được ảnh!", Toast.LENGTH_SHORT).show();
            }
        });

        // Crop ảnh
        cropImageLauncher = registerForActivityResult(new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                Uri croppedUri = result.getUriContent();
                if (croppedUri != null) {
                    imageView.setImageURI(croppedUri);
                    Toast.makeText(this, "Ảnh đã cắt!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Exception error = result.getError();
                if (error != null) {
                    Toast.makeText(this, "Lỗi crop ảnh: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Chụp ảnh từ camera
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                startImageCrop(imageUri);
            } else {
                Toast.makeText(this, "Chụp ảnh thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCamera.setOnClickListener(v -> checkCameraPermission());
        btnLibrary.setOnClickListener(v -> openGallery());
    }

    private void checkCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void openCamera() {
        imageUri = createImageUri();
        if (imageUri != null) {
            takePictureLauncher.launch(imageUri);
        } else {
            Toast.makeText(this, "Không tạo được URI!", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri createImageUri() {
        String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    private void startImageCrop(Uri imageUri) {
        CropImageOptions cropImageOptions = new CropImageOptions();
        cropImageOptions.guidelines = CropImageView.Guidelines.ON;
        cropImageOptions.aspectRatioX = 1;
        cropImageOptions.aspectRatioY = 1;
        cropImageOptions.outputCompressFormat = Bitmap.CompressFormat.PNG;
        cropImageOptions.outputCompressQuality = 90;

        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(imageUri, cropImageOptions);
        cropImageLauncher.launch(cropImageContractOptions);
    }
}