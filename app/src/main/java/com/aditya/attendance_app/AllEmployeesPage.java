package com.aditya.attendance_app;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.common.internal.FallbackServiceBroker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AllEmployeesPage extends AppCompatActivity {

    DataSnapshot emps = null;
    DataSnapshot leaves = null;
    DatabaseReference dbremp = FirebaseDatabase.getInstance().getReference("Employees");
    TextView addempbtn, noresult;
    ImageView backbtn, addimage;
    EditText editsearch;
    KProgressHUD khud;
    Python py;
    PyObject pyObject;
    Bitmap myphoto;
    Uri selectedfile;
    StorageReference storageReference;

    LinearLayout cardcontainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_employees_page);

        getSupportActionBar().hide();

        khud=KProgressHUD.create(AllEmployeesPage.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        khud.show();
        getemployees();

        addempbtn = (TextView) findViewById(R.id.addemp);
        cardcontainer = (LinearLayout) findViewById(R.id.empcontainer);
        editsearch = (EditText) findViewById(R.id.editsearch);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        noresult = (TextView) findViewById(R.id.noresult);

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addempbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddempPop();
            }
        });

        editsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String st = s.toString().toLowerCase();
                ArrayList<DataSnapshot> temp = new ArrayList<>();
                for (DataSnapshot emp : emps.getChildren()) {
                    if (emp.getValue().toString().toLowerCase().contains(st) || emp.getKey().toString().toLowerCase().contains(st)) {
                        temp.add(emp);
                    }
                }

                createSearchCards(temp);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void createSearchCards(ArrayList<DataSnapshot> temp) {
        cardcontainer.removeAllViews();
        if (temp.size() != 0) {
            noresult.setVisibility(View.GONE);
            for (DataSnapshot ds : temp) {
                LayoutInflater inflater = LayoutInflater.from(AllEmployeesPage.this);
                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.employee_card, null, false);
                cardcontainer.addView(ll);

                ImageView img = ll.findViewById(R.id.profilepic);

                try {
                    File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Emp_Images");
                    if (directory.exists()) {
                        for (File fl : directory.listFiles()) {
                            if (fl.getAbsolutePath().contains(ds.getKey().toString())) {
                                Bitmap bmp = BitmapFactory.decodeFile(fl.getAbsolutePath());
                                img.setImageBitmap(bmp);
                            }
                        }
                    }
                } catch (Exception e) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.user));
                }

                TextView name = ll.findViewById(R.id.empname);
                name.setText(ds.getValue().toString());

                TextView code = ll.findViewById(R.id.empcode);
                code.setText(ds.getKey().toString());

                ImageView pres = ll.findViewById(R.id.present);
                ImageView absent = ll.findViewById(R.id.absent);

                pres.setVisibility(View.GONE);
                absent.setVisibility(View.GONE);

//                ll.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        EmpPop(ds, "edit");
//                    }
//                });
            }
        } else {
            noresult.setVisibility(View.VISIBLE);
        }
    }

    private void showAddempPop() {
        Dialog dialog = new Dialog(AllEmployeesPage.this);
        dialog.setContentView(R.layout.addemp_pop);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        storageReference = FirebaseStorage.getInstance().getReference();
        addimage = dialog.findViewById(R.id.addimage);
        EditText empname = dialog.findViewById(R.id.empname);
        EditText empcode = dialog.findViewById(R.id.empcode);
        ImageView close = dialog.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView savebtn = dialog.findViewById(R.id.save);

        addimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkcamera();
            }
        });

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                khud.show();
                String nm = empname.getText().toString().trim();
                String cd = empcode.getText().toString().trim();

                nm = nm.replace(" ", "_");
                cd = cd.replace(" ","_");

                if (!Python.isStarted()) {
                    Python.start(new AndroidPlatform(AllEmployeesPage.this));

                    py = Python.getInstance();

                    pyObject = py.getModule("myscript");
                }

                float aspectRatio1 = myphoto.getWidth() /
                        (float) myphoto.getHeight();
                int width1 = 250;
                int height1 = Math.round(width1 / aspectRatio1);

                File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Emp_Images", nm+"!!"+cd+".jpg");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(directory);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap.createScaledBitmap(myphoto, width1, height1, false).compress(Bitmap.CompressFormat.JPEG, 100, fos);

                File filename = new File("/data/user/0/com.aditya.attendance_app/files/chaquopy/AssetFinder/app/test.jpg");

                try (FileOutputStream out = new FileOutputStream(filename)) {
                    float aspectRatio = myphoto.getWidth() /
                            (float) myphoto.getHeight();
                    int width = 250;
                    int height = Math.round(width / aspectRatio);

                    Bitmap.createScaledBitmap(myphoto, width, height, false).compress(Bitmap.CompressFormat.JPEG, 100, out);


                    PyObject pobj = pyObject.callAttr("convert", "/data/user/0/com.aditya.attendance_app/files/chaquopy/AssetFinder/app/test.jpg");

                    if(pobj.toString().equals("done")){
                        File filepkl = new File("/data/user/0/com.aditya.attendance_app/files/chaquopy/AssetFinder/app/toupload.pkl");
                        String finalCd = cd;
                        String finalNm = nm;
                        storageReference.child("workers_pkl").child(nm+"!!"+cd+".pkl").putFile(Uri.fromFile(filepkl)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                dbremp.child(finalCd).setValue(finalNm).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        storageReference.child("worker_images").child(finalNm+"!!"+finalCd+".jpg").putFile(Uri.fromFile(filename)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                khud.dismiss();
                                                dialog.dismiss();
                                                Toast.makeText(AllEmployeesPage.this, "Done", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                }
                catch (Exception e){

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            selectedfile = data.getData();
            myphoto = (Bitmap) data.getExtras().get("data");
            addimage.setImageBitmap(myphoto);
        }
    }

    private void checkcamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            String[] requestloc = new String[]{Manifest.permission.CAMERA};
            requestPermissions(requestloc, 980);
        } else {
            Intent cameraIntent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, 100);
        }
    }

    private void checkStorage() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            String[] requestloc = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(requestloc, 123);
        } else {

        }
    }

    private void getemployees() {
        dbremp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                emps = snapshot;
                createcards();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String gettime(int len) {
        Calendar instance = Calendar.getInstance();

        int mod = instance.get(Calendar.MINUTE) % 15;
        instance.add(Calendar.MINUTE, mod < 8 ? -mod : (15 - mod));

        String year = String.valueOf(instance.get(Calendar.YEAR));
        String month = String.valueOf(instance.get(Calendar.MONTH) + 1);
        String day = String.valueOf(instance.get(Calendar.DATE));
        String hour = String.valueOf(instance.get(Calendar.HOUR_OF_DAY));
        String minute = String.valueOf(instance.get(Calendar.MINUTE));

        if (month.length() == 1) {
            month = "0" + month;
        }
        if (day.length() == 1) {
            day = "0" + day;
        }
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        if (minute.length() == 1) {
            minute = "0" + minute;
        }

        if (len == 1) {
            return day + "/" + month + "/" + year + " " + hour + ":" + minute + ":00";
        } else {
            return year + month + day;
        }
    }

    private void createcards() {
        cardcontainer.removeAllViews();
        if (emps != null) {
            for (DataSnapshot ds : emps.getChildren()) {
                LayoutInflater inflater = LayoutInflater.from(AllEmployeesPage.this);
                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.employee_card, null, false);
                cardcontainer.addView(ll);

                ImageView img = ll.findViewById(R.id.profilepic);

                try {
                    File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Emp_Images");
                    if (directory.exists()) {
                        for (File fl : directory.listFiles()) {
                            if (fl.getAbsolutePath().contains(ds.getKey().toString())) {
                                Bitmap bmp = BitmapFactory.decodeFile(fl.getAbsolutePath());
                                img.setImageBitmap(bmp);
                            }
                        }
                    }
                } catch (Exception e) {
                    img.setImageDrawable(getResources().getDrawable(R.drawable.user));
                }

                TextView name = ll.findViewById(R.id.empname);
                name.setText(ds.getValue().toString());

                TextView code = ll.findViewById(R.id.empcode);
                code.setText(ds.getKey().toString());

                ImageView pres = ll.findViewById(R.id.present);
                ImageView absent = ll.findViewById(R.id.absent);

                pres.setVisibility(View.GONE);
                absent.setVisibility(View.GONE);

//                ll.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        EmpPop(ds, "edit");
//                    }
//                });
//            }
        }

        khud.dismiss();
    }

//    private void EmpPop(DataSnapshot emp, String type) {
//        Dialog dialog = new Dialog(AllEmployeesPage.this);
//        dialog.setContentView(R.layout.addemp_pop);
//        dialog.show();
//
//        Window window = dialog.getWindow();
//        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//
//        TextView label = dialog.findViewById(R.id.toplabel);
//        EditText empname = dialog.findViewById(R.id.empname);
//        EditText empcode = dialog.findViewById(R.id.empcode);
//
//        ImageView close = dialog.findViewById(R.id.close);
//        close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
//
//        TextView savebtn = dialog.findViewById(R.id.save);
//
//        empname.setText(emp.child("name").getValue().toString());
//        empcode.setText(emp.getKey().toString());
//
//        if (type.equals("edit")) {
//            label.setText("Edit Employee");
//        }
//
//        savebtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                khud.show();
//                String nm = empname.getText().toString().trim();
//                String cd = empcode.getText().toString().trim();
//
//                dbremp.child(cd).setValue(nm).addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        khud.dismiss();
//                        dialog.dismiss();
//                        Toast.makeText(AllEmployeesPage.this, "Saved", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
    }
}