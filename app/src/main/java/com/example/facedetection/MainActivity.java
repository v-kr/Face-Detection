package com.example.facedetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.FaceDetector;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button button;
    private final static int REQUEST_CAMERA_CAPTURE = 124;
    FirebaseVisionImage image;
    FirebaseVisionFaceDetector detector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        FirebaseApp.initializeApp(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePicyureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePicyureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePicyureIntent, REQUEST_CAMERA_CAPTURE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CAMERA_CAPTURE && resultCode == RESULT_OK) {
            Bundle extra = data.getExtras();
            Bitmap bitmap = (Bitmap) extra.get("data");
            detectFace(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void detectFace(Bitmap bitmap){
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .setTrackingEnabled(true)
                .build();
        try{
            image = FirebaseVisionImage.fromBitmap(bitmap);
            detector = FirebaseVision.getInstance()
                    .getVisionFaceDetector(options);
        }catch(Exception e){
            e.printStackTrace();
        }
        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                String resultText = "";
                int i =1;
                for(FirebaseVisionFace face : firebaseVisionFaces){
                    resultText = resultText.concat("\n"+i+".")
                            .concat("\nsmile: " + face.getSmilingProbability()*100+"%")
                            .concat("\nLeftEye "+ face.getLeftEyeOpenProbability()*100+"%");
                    i++;

                }
                if(firebaseVisionFaces.size()==0){
                    Toast.makeText(MainActivity.this, "NO FACES", Toast.LENGTH_SHORT).show();
                }else{
                    Bundle bundle = new Bundle();
                    bundle.putString("Face",resultText);
                    DialogFragment resultDialog = new ResultDialog();
                    resultDialog.setArguments(bundle);
                    resultDialog.setCancelable(false);
                    resultDialog.show(getSupportFragmentManager(),"Face");
                }
            }
        });
    }
}