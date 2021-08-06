package com.aditya.attendance_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
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

import com.google.android.gms.tasks.OnSuccessListener;
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

public class AllEmployeesPage extends AppCompatActivity {

    DataSnapshot emps = null;
    DataSnapshot attendance = null;
    DatabaseReference dbremp = FirebaseDatabase.getInstance().getReference("Employees");
    DatabaseReference dbratt = FirebaseDatabase.getInstance().getReference("Attendance");
    TextView noresult;
    ImageView backbtn;
    EditText editsearch;
    KProgressHUD khud;

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

        cardcontainer = (LinearLayout) findViewById(R.id.empcontainer);
        editsearch = (EditText) findViewById(R.id.editsearch);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        noresult = (TextView) findViewById(R.id.noresult);

        getemployees();

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                    if (emp.child("name").getValue().toString().toLowerCase().contains(st) || emp.getKey().toString().toLowerCase().contains(st)) {
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

                TextView name = ll.findViewById(R.id.empname);
                name.setText(ds.child("name").getValue().toString());

                TextView code = ll.findViewById(R.id.empcode);
                code.setText(ds.getKey().toString());

                ImageView vacicon = ll.findViewById(R.id.vacicon);
            }
        } else {
            noresult.setVisibility(View.VISIBLE);
        }
    }

    private void getemployees() {
        cardcontainer.removeAllViews();
        dbremp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                emps = snapshot;
                try {
                    dbratt.child(gettime(0)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            attendance = snapshot;
                            createcards();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                catch (Exception e){
                    Toast.makeText(AllEmployeesPage.this, "Couldn't load data", Toast.LENGTH_SHORT).show();
                }
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

    private boolean check_present(String code){
        boolean pres = false;
        for(DataSnapshot ds: attendance.getChildren()){
            if(ds.getKey().toString().equals(code)){
                pres = true;
            }
        }

        return pres;
    }

    private void createcards() {
        File directory = new File(Environment.getExternalStorageDirectory()+File.separator+"Emp_Images");
        if(directory.exists()) {
            for (File fl : directory.listFiles()) {
                Toast.makeText(AllEmployeesPage.this, "" + fl.getName(), Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(AllEmployeesPage.this, "Folder does not exists", Toast.LENGTH_SHORT).show();
        }
        cardcontainer.removeAllViews();
        if (emps != null) {
            for (DataSnapshot ds : emps.getChildren()) {
                LayoutInflater inflater = LayoutInflater.from(AllEmployeesPage.this);
                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.employee_card, null, false);
                cardcontainer.addView(ll);

                TextView name = ll.findViewById(R.id.empname);
                name.setText(ds.getValue().toString());

                TextView code = ll.findViewById(R.id.empcode);
                code.setText(ds.getKey().toString());

                ImageView present = ll.findViewById(R.id.present);

                String cd = ds.getKey().toString();
                boolean pr = check_present(cd);

                if(pr){
                    present.setVisibility(View.VISIBLE);
                }
            }
        }

        khud.dismiss();
    }
}