package com.tq.distributed_chat_android.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.tq.distributed_chat_android.R;
import com.tq.distributed_chat_android.databinding.ActivityCameraBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";
    private ActivityCameraBinding binding;

    private ImageCapture imageCapture;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor;

    private boolean isFrontCamera = false;
    private int flashMode = ImageCapture.FLASH_MODE_OFF;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    setupCameraProvider();
                } else {
                    Toast.makeText(this, "permission_denied_toast", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
    private final ActivityResultLauncher<Intent> previewLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                    String uriString = result.getData().getStringExtra("image_uri");

                    Intent forwardResultIntent = new Intent();
                    forwardResultIntent.putExtra("image_uri", uriString);

                    setResult(RESULT_OK, forwardResultIntent);

                    finish();
                }
            });
    private final ActivityResultLauncher<String> pickGalleryImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("image_uri", uri.toString());
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();

        checkPermissionsAndStart();
        setupClickListeners();
    }

    private void checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            setupCameraProvider();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void setupClickListeners() {
        binding.btnFlipCamera.setOnClickListener(v -> {
            isFrontCamera = !isFrontCamera;
            bindCameraUseCases();
        });

        binding.btnShutter.setOnClickListener(v -> capturePhoto());

        binding.btnFlash.setOnClickListener(v -> toggleFlash());

        binding.btnGallery.setOnClickListener(v -> openGallery());
    }

    private void setupCameraProvider() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (Exception e) {
                Log.e(TAG, "Camera initial allocation failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(flashMode)
                .build();

        CameraSelector cameraSelector = isFrontCamera
                ? CameraSelector.DEFAULT_FRONT_CAMERA
                : CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            Log.e(TAG, "Use case binding assembly failure", e);
        }
    }

    private void toggleFlash() {
        if (flashMode == ImageCapture.FLASH_MODE_OFF) {
            flashMode = ImageCapture.FLASH_MODE_ON;
            binding.btnFlash.setIcon(ContextCompat.getDrawable(this, R.drawable.flash_on_24));
        } else if (flashMode == ImageCapture.FLASH_MODE_ON) {
            flashMode = ImageCapture.FLASH_MODE_AUTO;
            binding.btnFlash.setIcon(ContextCompat.getDrawable(this, R.drawable.flash_auto_24));
        } else {
            flashMode = ImageCapture.FLASH_MODE_OFF;
            binding.btnFlash.setIcon(ContextCompat.getDrawable(this, R.drawable.flash_off_24));
        }

        if (imageCapture != null) {
            imageCapture.setFlashMode(flashMode);
        }
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalCacheDir();

        try {
            File photoFile = File.createTempFile(fileName, ".jpg", storageDir);
            ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                    .Builder(photoFile)
                    .build();

            imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                    Uri savedUri = androidx.core.content.FileProvider.getUriForFile(
                            CameraActivity.this,
                            getApplicationContext().getPackageName() + ".fileprovider",
                            photoFile
                    );

                    navigateToPreview(savedUri);
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e(TAG, "Photo capture execution failed", exception);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed creating secure storage path references", e);
        }
    }

    private void navigateToPreview(Uri imageUri) {
        runOnUiThread(() -> {
            Intent intent = new Intent(CameraActivity.this, ImagePreviewActivity.class);
            intent.putExtra("image_uri", imageUri.toString());
            previewLauncher.launch(intent);
        });
    }

    private void openGallery() {
        pickGalleryImageLauncher.launch("image/*");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        cameraExecutor.shutdown();
    }
}