package com.example.handwrittenchemistry;


import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.content.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1888;
    private static final int REQUEST_IMAGE_IMPORT = 103;
    private ImageView imageView;
    private EditText textView;
    private TextView resultView;
    private ImageButton menuButton;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_GALLERY_PERMISSION_CODE = 200;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_RESOLVE = 2;
    private static final String[] notSupported = {"",""};
    private static final String check = "+-=";

    private Context mycontext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //If dell'OpenCV
        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        this.mycontext = this;

        setContentView(R.layout.activity_main);

        textView = (EditText) findViewById(R.id.reactionText);
        resultView = (TextView) findViewById(R.id.resultText);

        ImageButton captureButton = (ImageButton) findViewById(R.id.captureButton);
        ImageButton importButton = (ImageButton) findViewById(R.id.importButton);
        menuButton = (ImageButton) findViewById(R.id.menuButton);
        Button resolveButton = (Button) findViewById(R.id.risolviButton);


        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA},MY_CAMERA_PERMISSION_CODE);
                }else{
                    dispatchTakePictureIntent();
                }
            }
        });
        importButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, MY_GALLERY_PERMISSION_CODE);
                }else{
                    openImageImport();
                }
            }
        });
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openMenu(); }
        });

        resolveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //solve the input reaction
                String str = ((EditText) findViewById(R.id.reactionText)).getText().toString();

                try{
                    Parser myp = new Parser(mycontext);
                    String res="";
                    myp.setTk(str);

                    //res+=myp.isEquation()+"\n";

                    myp.getEquation().divide();
                    myp.getEquation().createMatrix();

                    //res+=myp.getEquation().getMatrix().toString()+"\n";
                    res+= SolveReaction.solve(myp.getEquation().getMatrix());
                    resultView.setText(res);
                }catch(Exception e){
                    resultView.setText("errore nel parsing!");
                    resultView.setText(e.getMessage() + e.getStackTrace());
                }

            }
        });



    }

    ActivityResultLauncher<Intent> takePictureRegistration = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {

                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                        try {
                            //uso dell'immagine catturat

                            Segmentation st = new Segmentation(mycontext, BitmapFactory.decodeResource(
                                    mycontext.getResources(), R.raw.real4));
                            Model mym = new Model(st.segment(), mycontext);
                            resultView.setText(mym.getStringPredicted());

                        } catch (IOException e) {
                            resultView.setText("Errore!");
                        }

                    }
                }
            });

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureRegistration.launch(takePictureIntent);
    }
    ActivityResultLauncher<Intent> importRegistration = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {

                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                        // URI

                        //uso dell'immagine catturata
                    }
                }
            });
    private void openImageImport() {
        Intent importIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        importRegistration.launch(importIntent);

    }
    private void openMenu() {
        PopupMenu popupMenu = new PopupMenu(this,menuButton);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu/*FILE MENU*/,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.menu_settings) {
                    //azione per le impostazioni
                    return true;
                }else if(item.getItemId() == R.id.menu_guide){
                        //azione per la Guida
                        return true;
                }

                else if (item.getItemId() == R.id.menu_WAW) {
                    // azione per il "Chi siamo"
                    return true;

                }else return false;

            }
        });
        popupMenu.show();
    }

    private void resolveReaction(){
        String reaction = textView.getText().toString();
        if(MainActivity.notCorrect(reaction)) {
            resultView.setText("Wrong syntax!");
        }
        else{
            String result = "WIP";
            resultView.setText("Balanced reaction: "+result);
        }
    }



    private static Boolean notCorrect (String reaction){
        boolean res = false;
        if (!reaction.contains("=")){
            res = true;
        }

        return res;
    }

}