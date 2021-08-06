
package com.aditya.attendance_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.File;
import java.util.regex.Pattern;

public class My_settings extends AppCompatActivity {

    StorageReference sref;
    TextView dbtn;
    ImageView backbtn;

    boolean updated;
    KProgressHUD khud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().hide();

        khud= KProgressHUD.create(My_settings.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        dbtn = (TextView)findViewById(R.id.download);
        sref = FirebaseStorage.getInstance().getReference();
        backbtn = (ImageView)findViewById(R.id.backbtn);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(updated){
                    startActivity(new Intent(My_settings.this, option_page.class));
                }else {
                    finish();
                }
            }
        });

        dbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download_images();
            }
        });
    }

    private void download_images() {
        khud.show();
        updated = true;
        File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"Emp_Images");
        if(!directory.exists()) {
            directory.mkdirs();
        }
        sref.child("worker_images").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference item : listResult.getItems()) {
                    String[] sp = item.toString().split("/");
                    String nm = sp[sp.length -1].split(Pattern.quote("."))[0];

                    for(File fl : directory.listFiles()){
                        fl.delete();
                    }

                    File imgfile = new File(directory, nm+".jpg");
                    item.getFile(imgfile);
                }
            }
        });

        sref.child("workers_pkl").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for(StorageReference item : listResult.getItems()){
                    String[] sp = item.toString().split("/");
                    String nm = sp[sp.length -1];

                    File dir = new File("/data/user/0/com.aditya.attendance_app/files/chaquopy/AssetFinder/app");

                    for(File fl : dir.listFiles()){
                        fl.delete();
                    }

                    File pklfile = new File("/data/user/0/com.aditya.attendance_app/files/chaquopy/AssetFinder/app", nm);

                    item.getFile(pklfile);
                }

                khud.dismiss();

                Toast.makeText(My_settings.this, "Data Updated", Toast.LENGTH_SHORT).show();
            }
        });
    }
}