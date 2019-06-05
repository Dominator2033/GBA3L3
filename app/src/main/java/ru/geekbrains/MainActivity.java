package ru.geekbrains;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    public static final int SELECT_IMAGE = 1;
    public static final String OUTPUT_FILE = "result.png";
    private ImageView imageView, imageViewPng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatButton selectImageBtn = findViewById(R.id.select_image_btn);
        imageView = findViewById(R.id.image_view);
        imageViewPng = findViewById(R.id.image_view_png);
        selectImage(selectImageBtn);
    }

    private void selectImage(AppCompatButton selectImageBtn) {
        selectImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_IMAGE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                Uri path = Objects.requireNonNull(data).getData();
                imageView.setImageURI(path);
                Disposable disposable = Completable.fromAction(() -> {
                    InputStream inputStream = getContentResolver().openInputStream(Objects.requireNonNull(path));
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    FileOutputStream fos = new FileOutputStream(getImagePath(OUTPUT_FILE));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    imageViewPng.setImageBitmap(bitmap);
                    fos.close();
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableCompletableObserver() {

                            @Override
                            public void onComplete() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
            }
        }

    }

    public String getImagePath(String file) {
        return String.format(Locale.ENGLISH, "%s/%s",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath(),
                file);
    }
}
