package com.example.opencvdemo;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Scalar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Scalar scalar;
    private ImageProcessing ip;

    private ImageView iv;
    private int borderWidth = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.solid);
        ip = new ImageProcessing(bitmap);
        scalar = new Scalar(0, 0, 0, 255);
        iv = findViewById(R.id.image);
        Bitmap newBm = ip.resizeBorderOfEdges(borderWidth, scalar);
        iv.setImageBitmap(newBm);

        Slider slider = findViewById(R.id.slider);
        slider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                borderWidth = (int) value;
                Bitmap newBm = ip.resizeBorderOfEdges((int) value, scalar);
                iv.setImageBitmap(newBm);
            }
        });

        Button colorPicker = findViewById(R.id.color_picker);
        colorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupDialog();
            }
        });
    }

    private void setupDialog() {
        new ColorPickerDialog.Builder(this)
                .setTitle("ColorPicker Dialog")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton("Ok",
                        new ColorEnvelopeListener() {
                            @Override
                            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                setLayoutColor(envelope);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                .attachAlphaSlideBar(true) // the default value is true.
                .attachBrightnessSlideBar(true)  // the default value is true.
                .setBottomSpace(12).show(); // set a bottom space between the last slidebar and buttons.
    }

    private void setLayoutColor(ColorEnvelope colorEnvelope) {
        int[] argb = colorEnvelope.getArgb();
        scalar = new Scalar(argb[1], argb[2], argb[3], argb[0]);
        Bitmap newBm = ip.resizeBorderOfEdges(borderWidth, scalar);
        iv.setImageBitmap(newBm);
    }
}