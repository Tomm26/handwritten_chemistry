package com.example.ciocoiu_faggiano;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.content.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1888;
    private static final int REQUEST_IMAGE_IMPORT = 103;
    private ImageView imageView;
    private EditText textView;
    private TextView resultView;
    private ImageButton captureButton;
    private ImageButton importButton;
    private ImageButton menuButton;
    private Button resolveButton;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_GALLERY_PERMISSION_CODE = 200;
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_RESOLVE = 2;
    private static String[] notSupported = {"",""};
    private static String check = "+-=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        If dell'OpenCV
        if (!OpenCVLoader.initDebug()) {
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, null);
            }
         */
        setContentView(R.layout.activity_fullscreen);

        textView = (EditText) findViewById(R.id.reactionText);
        resultView = (TextView) findViewById(R.id.resultText);

        captureButton =(ImageButton) findViewById(R.id.captureButton);
        importButton = (ImageButton) findViewById(R.id.importButton);
        menuButton = (ImageButton) findViewById(R.id.menuButton);
        resolveButton = (Button) findViewById(R.id.risolviButton);

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
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_GALLERY_PERMISSION_CODE);
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
                resolveReaction();
            }
        });



    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);

    }
    private void openImageImport() {
        Intent importIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(importIntent, REQUEST_IMAGE_IMPORT);
    }
    private void openMenu() {
        PopupMenu popupMenu = new PopupMenu(this,menuButton);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu/*FILE MENU*/,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_settings:
                        //azione per le impostazioni
                        return true;
                    case R.id.menu_guide:
                        //azione per la Guida
                        return true;
                    case R.id.menu_WAW:
                        // azione per il "Chi siamo"
                    default:
                        return false;
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            //uso dell'immagine catturata
            imageView.setImageBitmap(imageBitmap);
        }else if (requestCode == REQUEST_IMAGE_IMPORT && data != null){
            Uri selectedImage = data.getData();
            //Uso dell'immagine catturata
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