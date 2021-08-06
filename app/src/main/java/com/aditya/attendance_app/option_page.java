package com.aditya.attendance_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import cdflynn.android.library.checkview.CheckView;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.SmartLocation;

public class option_page extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_CODE_QR_SCAN = 101;
    LinearLayout intime, outtime;
    private static int REQUEST_LOC_ORDER = 123;
    String project_code;
    ArrayList<DataSnapshot> projectsdata = new ArrayList<>();
    ArrayList<DataSnapshot> empdata = new ArrayList<>();
    ArrayList<DataSnapshot> supdata = new ArrayList<>();
    LinearLayout stats;

    double lati=0, longi=0;
    String address="";

    ImageView backbtn;

    KProgressHUD khud;

    String action = "IN";

    SharedPreferences sharedPreferences;

    boolean start = true;

    LocationManager locationManager;
    Python py;
    PyObject pyObject;
    CheckView check;
    String empcode;

    ImageView settingbtn;

    boolean python_set, location_set;

    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("Attendance");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_page);
        getSupportActionBar().hide();

        khud = KProgressHUD.create(option_page.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"Emp_Images");
        if(!directory.exists()) {
            directory.mkdirs();
        }

        new MyAsynctask(this).execute("10");

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        checkcamera();
        checkIfGPSEnabled();

        intime = (LinearLayout) findViewById(R.id.intime);
        outtime = (LinearLayout) findViewById(R.id.outtime);
        check = (CheckView) findViewById(R.id.check);
        settingbtn = (ImageView)findViewById(R.id.settingbtn);
        stats = (LinearLayout)findViewById(R.id.stats);

        stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(option_page.this, AllEmployeesPage.class));
            }
        });

        intime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = "IN";
                checkcamera();
            }
        });

        settingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(option_page.this, My_settings.class));
            }
        });

        outtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action = "OUT";
                checkcamera();
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

    private void checkcamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            String[] requestloc = new String[]{Manifest.permission.CAMERA};
            requestPermissions(requestloc, REQUEST_LOC_ORDER);
        } else {
            checkLocation();
        }
    }

    private void checkLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] requesLoc = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            requestPermissions(requesLoc, 989);
        } else {
            if (start == false) {
                checkIfGPSEnabled();
                startcamera();
            } else {
                start = false;
            }
        }
    }

    private void startcamera() {
        Intent cameraIntent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 100);
    }

    private void hidecheck(){
        check.uncheck();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            Dialog dialog = new Dialog(option_page.this);
            dialog.setContentView(R.layout.camera_pic_show);
            dialog.show();

            Window window = dialog.getWindow();
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            ImageView myimage = dialog.findViewById(R.id.myimage);
            TextView verify = dialog.findViewById(R.id.verify);
            TextView done = dialog.findViewById(R.id.done);
            TextView cagain = dialog.findViewById(R.id.cagain);
            TextView empname = dialog.findViewById(R.id.empname);

            myimage.setImageBitmap(photo);

            cagain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkcamera();
                }
            });

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    check.check();
                    dialog.dismiss();

                    if(action == "IN") {
                        Add_in_time ait = new Add_in_time(gettime(1), lati, longi, address);

                        dbr.child(gettime(0)).child(empcode).child("IN").setValue(ait).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        hidecheck();
                                    }
                                }, 2000);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(option_page.this, "An error occured while marking the attendance!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    if(action == "OUT") {
                        Add_out_time aot = new Add_out_time(gettime(1), lati, longi, address);

                        dbr.child(gettime(0)).child(empcode).child("OUT").setValue(aot).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        hidecheck();
                                    }
                                }, 2000);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(option_page.this, "An error occured while marking the attendance!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            });

            verify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    khud.show();
                    File filename = new File("/data/user/0/com.aditya.attendance_app/files/chaquopy/AssetFinder/app/test.jpg");
                    try (FileOutputStream out = new FileOutputStream(filename)) {
                        float aspectRatio = photo.getWidth() /
                                (float) photo.getHeight();
                        int width = 250;
                        int height = Math.round(width / aspectRatio);

                        Bitmap.createScaledBitmap(photo, width, height, false).compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                        // PNG is a lossless format, the compression factor (100) is ignored

                        PyObject pobj = pyObject.callAttr("main", "/data/user/0/com.aditya.attendance_app/files/chaquopy/AssetFinder/app/test.jpg");
                        verify.setVisibility(View.GONE);

                        if(!pobj.toString().equals("")) {
                            if(pobj.toString().equals("No Face Found")){
                                empname.setText("No Face Found");
                                done.setVisibility(View.GONE);
                                cagain.setVisibility(View.VISIBLE);
                            }else {
                                String nm = pobj.toString().split(Pattern.quote("."))[0];

                                File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"Emp_Images", nm+".jpg");

                                Bitmap myBitmap = BitmapFactory.decodeFile(directory.getAbsolutePath());

                                myimage.setImageBitmap(myBitmap);

                                empname.setText(pobj.toString().split("!!")[0]);
                                empcode = pobj.toString().split("!!")[1];

                                done.setVisibility(View.VISIBLE);
                                cagain.setVisibility(View.GONE);
                            }
                        }else {
                            empname.setText("Could nor recognize face");
                            done.setVisibility(View.GONE);
                            cagain.setVisibility(View.VISIBLE);
                        }
                        //checkResult(pobj.toString());
                    } catch (IOException e) {
                        Toast.makeText(option_page.this, "Error"+e, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    khud.dismiss();
                }
            });
        }
    }

    private void checkIfGPSEnabled() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(option_page.this);
            builder.setTitle("GPS Disabled!");
            builder.setMessage("GPS should be enabled to mark the attendance");
            builder.setPositiveButton("Enable GPS", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(option_page.this, "Okay", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        }else {
            khud.show();
            //setgpslocation();
            startGettingLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfGPSEnabled();
    }

    @SuppressLint("MissingPermission")
    private void startGettingLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provide = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(provide,5*1000,10,(LocationListener) this);
        setgpslocation();
    }

    private void setgpslocation() {
        SmartLocation.with(option_page.this).location().oneFix().start(new OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location) {
                SmartLocation.with(option_page.this).geocoding().reverse(location, new OnReverseGeocodingListener() {
                    @Override
                    public void onAddressResolved(Location location, List<Address> list) {
                        if(list.size() > 0){
                            lati = location.getLatitude();
                            longi = location.getLongitude();
                            address = (list.get(0).getAddressLine(0));
                            location_set = true;
                            if(python_set == true) {
                                khud.dismiss();
                            }
                        }else {
                            setgpslocation();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOC_ORDER) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 989) {
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();

        Geocoder geo = new Geocoder(option_page.this.getApplicationContext(), Locale.getDefault());

        List<Address> addresses = null;
        try {
            addresses = geo.getFromLocation(lat,lon, 1);
            if(!addresses.isEmpty() && addresses != null){
                lati = lat;
                longi = lon;
                address = addresses.get(0).getAddressLine(0);

            }
        } catch (IOException e) {
            Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}