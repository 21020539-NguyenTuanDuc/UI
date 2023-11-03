package com.example.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.example.ui.databinding.ActivityQrBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class QRActivity extends AppCompatActivity {
    ActivityQrBinding binding;
    private ListenableFuture cameraProviderFeature;
    private ExecutorService cameraExecutor;
    private PreviewView previewView;
    private MyImageAnalyzer analyzer;

    private SweetAlertDialog sweetAlertDialog;
    private ProcessCameraProvider cameraProvider = null;
    private Camera camera = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
//            .setTitleText("Oops...")
//            .setContentText("Something went wrong!");

        previewView = binding.cameraPreview;
        this.getWindow().setFlags(1024, 1024);

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFeature = ProcessCameraProvider.getInstance(this);
        analyzer = new MyImageAnalyzer(getSupportFragmentManager());


        cameraProviderFeature.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ActivityCompat.checkSelfPermission(QRActivity.this, Manifest.permission.CAMERA) != (PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(QRActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
                    } else {
                        ProcessCameraProvider cameraProvider = (ProcessCameraProvider) cameraProviderFeature.get();
                        bindPreview(cameraProvider);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));

        binding.flashButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                if (camera != null) {
                    if (camera.getCameraInfo().hasFlashUnit()) {
                        Integer torchState = camera.getCameraInfo().getTorchState().getValue();
                        if (torchState != null && torchState == 0) {
                            binding.flashButton.setBackground(getDrawable(R.drawable.flash_off_24));
                            binding.flashText.setText("Turn off Flash");
                        } else {
                            binding.flashButton.setBackground(getDrawable(R.drawable.flash_on_24));
                            binding.flashText.setText("Turn on Flash");
                        }
                        camera.getCameraControl().enableTorch(torchState != null && torchState == 0);
                    }
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0) {
//            cameraProvider = null;
            try {
                cameraProvider = (ProcessCameraProvider) cameraProviderFeature.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            bindPreview(cameraProvider);
        }

    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        ImageCapture imageCapture = new ImageCapture.Builder().build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(cameraExecutor, analyzer);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    public class MyImageAnalyzer implements ImageAnalysis.Analyzer {
        private FragmentManager fragmentManager;

        public MyImageAnalyzer(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
        }

        @Override
        public void analyze(ImageProxy imageProxy) {
            // insert your code here.
            scanBarcode(imageProxy);
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void scanBarcode(ImageProxy imageProxy) {
        Image image1 = imageProxy.getImage();
        assert image1 != null;
        InputImage inputImage = InputImage.fromMediaImage(image1, imageProxy.getImageInfo().getRotationDegrees());
        BarcodeScannerOptions barcodeScannerOptions = new BarcodeScannerOptions.Builder().setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC
        ).build();

        BarcodeScanner scanner = BarcodeScanning.getClient(barcodeScannerOptions);
        Task<List<Barcode>> result = scanner.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        readerBarcodeData(barcodes);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        Toast.makeText(QRActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Barcode>> task) {
                        imageProxy.close();
                    }
                });
    }

    private void readerBarcodeData(List<Barcode> barcodes) {
        for (Barcode barcode : barcodes) {
            Rect bounds = barcode.getBoundingBox();
            Point[] corners = barcode.getCornerPoints();

            String rawValue = barcode.getRawValue();
            String text = barcode.getDisplayValue();

            int valueType = barcode.getValueType();
            // See API reference for complete list of supported types
            if (valueType == Barcode.TYPE_TEXT && text != null) {
                String[] dataArr = text.split(" ");
                if (dataArr.length == 2) {
                    String id = dataArr[0];
                    String data = dataArr[1];
                    Toast.makeText(this, id + " " + data, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "This QR code type is not supported", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }


}