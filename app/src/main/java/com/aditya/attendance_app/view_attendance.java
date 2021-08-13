package com.aditya.attendance_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class view_attendance extends AppCompatActivity {

    ImageView backbtn;
    EditText searchtext;
    TextView noresult, enterdate, view;
    DataSnapshot emps, atts, leaves;
    LinearLayout containerlayout, datelayout;
    String currentdate = "";
    DatabaseReference dbrEmp = FirebaseDatabase.getInstance().getReference("Employees");
    DatabaseReference dbrAtt = FirebaseDatabase.getInstance().getReference("Attendance");
    DatabaseReference dbrvac = FirebaseDatabase.getInstance().getReference("Leaves");

    KProgressHUD khud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        getSupportActionBar().hide();

        khud = KProgressHUD.create(view_attendance.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);

        khud.show();

        containerlayout = (LinearLayout) findViewById(R.id.containerlayout);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        searchtext = (EditText) findViewById(R.id.editsearch);
        noresult = (TextView) findViewById(R.id.noresult);
        enterdate = (TextView) findViewById(R.id.enterdate);
        view = (TextView) findViewById(R.id.view);
        datelayout = (LinearLayout)findViewById(R.id.datelayout);

        settodaysdate();
        getemps();

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        searchtext.addTextChangedListener(new TextWatcher() {
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

        enterdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showdatepicker();
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createcards();
            }
        });

        if(getIntent().getExtras().getString("type").equals("admin")){
            datelayout.setVisibility(View.VISIBLE);
        }else {
            datelayout.setVisibility(View.GONE);
        }

    }

    private String parsedate(String dt) {
        String[] spl = dt.split("/");
        String d = spl[2];
        String m = spl[1];
        String y = spl[0];

        if (d.length() == 1) {
            d = "0" + d;
        }

        if (m.length() == 1) {
            m = "0" + m;
        }

        String f = spl[2] + spl[1] + spl[0];

        return f;
    }

    private void showdatepicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        String m = String.valueOf(monthOfYear + 1);
                        String d = String.valueOf(dayOfMonth);
                        String y = String.valueOf(year);

                        if (m.length() == 1) {
                            m = "0" + m;
                        }
                        if (d.length() == 1) {
                            d = "0" + d;
                        }

                        enterdate.setText(d + "/" + m + "/" + y);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void settodaysdate() {
        Calendar c = Calendar.getInstance();

        String date = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        String month = String.valueOf(c.get(Calendar.MONTH) + 1);
        String year = String.valueOf(c.get(Calendar.YEAR));

        if (date.length() == 1) {
            date = "0" + date;
        }

        if (month.length() == 1) {
            month = "0" + month;
        }

        currentdate = date + "/" + month + "/" + year;
        enterdate.setText(currentdate);
    }

    private void createSearchCards(ArrayList<DataSnapshot> temp) {
        containerlayout.removeAllViews();
        if (temp.size() != 0) {
            noresult.setVisibility(View.GONE);
            for (DataSnapshot ds : temp) {
                String nm = ds.getValue().toString();
                String cd = ds.getKey().toString();

                String ser = searchtext.getText().toString().toLowerCase();

                if (nm.toLowerCase().contains(ser) || cd.toLowerCase().contains(ser)) {
                    LayoutInflater inflater = LayoutInflater.from(view_attendance.this);
                    LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.employee_card, null, false);

                    containerlayout.addView(ll);

                    ImageView img = ll.findViewById(R.id.profilepic);

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+ File.separator + "Emp_Images");
                        if (directory.exists()) {
                            for (File fl : directory.listFiles()) {
                                if (fl.getAbsolutePath().contains(ds.getKey().toString())) {
                                    Bitmap bmp = BitmapFactory.decodeFile(fl.getAbsolutePath());
                                    img.setImageBitmap(bmp);
                                }
                            }
                        }
                    }else {
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
                    }

                    TextView name = ll.findViewById(R.id.empname);
                    name.setText(ds.getValue().toString());

                    TextView code = ll.findViewById(R.id.empcode);
                    code.setText(ds.getKey().toString());

                    ImageView presimg = ll.findViewById(R.id.present);
                    ImageView absent = ll.findViewById(R.id.absent);

                    String cds = ds.getKey().toString();
                    boolean pr = check_present(cds);

                    if (pr) {
                        presimg.setVisibility(View.VISIBLE);
                        absent.setVisibility(View.GONE);
                    }

                        ll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EmpPop(ds);
                            }
                        });
                }
            }
        } else {
            noresult.setVisibility(View.VISIBLE);
        }
    }

    private void EmpPop(DataSnapshot ds) {
        Dialog dialog = new Dialog(view_attendance.this);
        dialog.setContentView(R.layout.attendance_pop);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        ImageView img = dialog.findViewById(R.id.profilepic);
        TextView date = dialog.findViewById(R.id.date);
        TextView empname = dialog.findViewById(R.id.empname);
        TextView empcode = dialog.findViewById(R.id.empcode);
        TextView intime = dialog.findViewById(R.id.intime);
        TextView outtime = dialog.findViewById(R.id.outtime);
        TextView inloc = dialog.findViewById(R.id.inloc);
        TextView outloc = dialog.findViewById(R.id.outloc);
        TextView close = dialog.findViewById(R.id.close);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

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

        date.setText(enterdate.getText().toString());
        empname.setText(ds.getValue().toString());
        empcode.setText(ds.getKey().toString());

        String it = getintime(ds.getKey().toString());
        String ot = getouttime(ds.getKey().toString());
        intime.setText(it);

        if (!ot.equals("")) {
            outtime.setText(ot);
        }

        String il = getlocin(ds.getKey().toString());
        String ol = getlocout(ds.getKey().toString());

        if (!il.equals("")) {
            inloc.setText(il);
        }

        if (!ol.equals("")) {
            outloc.setText(ol);
        }

        inloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInMap(ds.getKey().toString());
            }
        });
        outloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOutMap(ds.getKey().toString());
            }
        });
    }

    private void openOutMap(String ec) {
        String dt = parsedate(enterdate.getText().toString());
        if (atts.hasChild(dt)) {
            if (atts.child(dt).hasChild(ec)) {
                DataSnapshot user = atts.child(dt).child(ec);
                String lati = "0", longi = "0";
                if (user.hasChild("OUT")) {
                    if (user.child("OUT").hasChild("latitude")) {
                        lati = user.child("OUT").child("latitude").getValue().toString();
                    }
                    if (user.child("OUT").hasChild("longitude")) {
                        longi = user.child("OUT").child("longitude").getValue().toString();
                    }

                    if (!lati.equals("0") || !longi.equals("0")) {
                        Uri gmmIntentUri = Uri.parse("geo:" + lati + "," + longi + "?q=" + lati + "," + longi);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(mapIntent);
                        } else {
                            Toast.makeText(this, "No app was found to open the map", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void openInMap(String ec) {
        String dt = parsedate(enterdate.getText().toString());
        if (atts.hasChild(dt)) {
            if (atts.child(dt).hasChild(ec)) {
                DataSnapshot user = atts.child(dt).child(ec);
                String lati = "0", longi = "0";

                if (user.child("IN").hasChild("latitude")) {
                    lati = user.child("IN").child("latitude").getValue().toString();
                }
                if (user.child("IN").hasChild("longitude")) {
                    longi = user.child("IN").child("longitude").getValue().toString();
                }

                if (!lati.equals("0") || !longi.equals("0")) {
                    Uri gmmIntentUri = Uri.parse("geo:" + lati + "," + longi + "?q=" + lati + "," + longi);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {
                        Toast.makeText(this, "No app was found to open the map", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getlocout(String ec) {
        String il = "";
        String dt = parsedate(enterdate.getText().toString());
        if (atts.hasChild(dt)) {
            for (DataSnapshot ds : atts.child(dt).getChildren()) {
                if (ds.getKey().toString().equals(ec)) {
                    if (ds.hasChild("OUT")) {
                        if (ds.child("OUT").hasChild("address")) {
                            il = ds.child("OUT").child("address").getValue().toString();
                        }
                    }
                }
            }
        }
        return il;
    }

    private String getlocin(String ec) {
        String il = "";
        String dt = parsedate(enterdate.getText().toString());
        if (atts.hasChild(dt)) {
            for (DataSnapshot ds : atts.child(dt).getChildren()) {
                if (ds.getKey().toString().equals(ec)) {
                    if (ds.child("IN").hasChild("address")) {
                        il = ds.child("IN").child("address").getValue().toString();
                    }
                }
            }
        }
        return il;
    }

    private String getouttime(String ec) {
        String ot = "";
        String dt = parsedate(enterdate.getText().toString());
        if (atts.hasChild(dt)) {
            for (DataSnapshot ds : atts.child(dt).getChildren()) {
                if (ds.getKey().toString().equals(ec)) {
                    if (ds.hasChild("OUT")) {
                        ot = ds.child("OUT").child("out_time").getValue().toString();
                    }
                }
            }
        }
        return ot;
    }

    private String getintime(String ec) {
        String it = "";
        String dt = parsedate(enterdate.getText().toString());
        if (atts.hasChild(dt)) {
            for (DataSnapshot ds : atts.child(dt).getChildren()) {
                if (ds.getKey().toString().equals(ec)) {
                    it = ds.child("IN").child("in_time").getValue().toString();
                }
            }
        }
        return it;
    }

    private void getemps() {
        dbrEmp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                emps = snapshot;
                getattendance();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getattendance() {
        dbrAtt.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                atts = snapshot;
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

    private boolean checkifonleave(String ec) {
        boolean ch = false;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date cd = null;
        try {
            cd = dateFormat.parse(gettime(0));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (cd != null) {
            for (DataSnapshot ds : leaves.getChildren()) {
                if (ds.getKey().toString().equals(ec)) {
                    for (DataSnapshot ddd : ds.getChildren()) {
                        String fd = ddd.child("fromdate").getValue().toString();
                        String td = ddd.child("todate").getValue().toString();
                        Date fdd = null, tdd = null;
                        try {
                            fdd = dateFormat.parse(fd);

                            if (td.length() > 2) {
                                tdd = dateFormat.parse(td);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (fdd != null && tdd != null) {
                            if ((cd.after(fdd) || cd.compareTo(fdd) == 0) && (cd.before(tdd) || cd.compareTo(tdd) == 0)) {
                                ch = true;
                            }
                        } else if (fdd != null && tdd == null) {
                            if (cd.compareTo(fdd) == 0) {
                                ch = true;
                            }
                        }
                    }
                }
            }
        }

        return ch;
    }

    private boolean check_present(String code) {
        boolean pres = false;
        String dt = enterdate.getText().toString();
        String[] dt_split = dt.split("/");
        String dt_joint = dt_split[2] + dt_split[1] + dt_split[0];

        try {
            for (DataSnapshot ds : atts.child(dt_joint).getChildren()) {
                if (ds.getKey().toString().equals(code)) {
                    pres = true;
                }
            }
        } catch (Exception e) {

        }

        return pres;
    }

    private void createcards() {
        containerlayout.removeAllViews();
        if (emps != null) {
            for (DataSnapshot ds : emps.getChildren()) {
                LayoutInflater inflater = LayoutInflater.from(view_attendance.this);
                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.employee_card, null, false);
                containerlayout.addView(ll);

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

                ImageView present = ll.findViewById(R.id.present);
                ImageView absent = ll.findViewById(R.id.absent);

                String cd = ds.getKey().toString();
                boolean pr = check_present(cd);

                if (pr) {
                    present.setVisibility(View.VISIBLE);
                    absent.setVisibility(View.GONE);
                }

                ll.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EmpPop(ds);
                    }
                });
            }
        }
        khud.dismiss();
    }
}