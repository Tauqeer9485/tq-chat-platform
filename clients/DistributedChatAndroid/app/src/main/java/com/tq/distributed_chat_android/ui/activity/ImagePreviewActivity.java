package com.tq.distributed_chat_android.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.tq.distributed_chat_android.databinding.ActivityImagePreviewBinding;

public class ImagePreviewActivity extends AppCompatActivity {

    private static final String TAG = "ImagePreviewActivity";
    private ActivityImagePreviewBinding binding;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityImagePreviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parseIntentData();
        setupClickListeners();
    }

    private void parseIntentData() {
        String uriString = getIntent().getStringExtra("image_uri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);

            Glide.with(this)
                    .load(imageUri)
                    .centerInside()
                    .into(binding.ivPreviewImage);
        } else {
            Log.e(TAG, "Image preview requested without target file URI data string reference");
            finish();
        }
    }

    private void setupClickListeners() {
        binding.btnSend.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("image_uri", imageUri.toString());
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        binding.btnCancel.setOnClickListener(v -> finish());
    }
}