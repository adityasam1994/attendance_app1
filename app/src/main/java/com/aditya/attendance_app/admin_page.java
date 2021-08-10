package com.aditya.attendance_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.opencsv.CSVWriter;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class admin_page extends AppCompatActivity {

    ImageView logout;
    TextView getatt, manageemp, viewatt;
    SharedPreferences sharedPreferences;
    ArrayList<DataSnapshot> attdata = new ArrayList<>();
    KProgressHUD khud;
    boolean allper;
    DatabaseReference dbr = FirebaseDatabase.getInstance().getReference("Attendance");
    DatabaseReference dbremp = FirebaseDatabase.getInstance().getReference("Employees");

    ArrayList<DataSnapshot> empdata = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        getSupportActionBar().hide();

        khud=KProgressHUD.create(admin_page.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setCancellable(true)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        khud.show();
        getemployees();
        checkpermissions();

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        logout = (ImageView) findViewById(R.id.logoutadmin);
        getatt = (TextView) findViewById(R.id.getatt);
        manageemp = (TextView) findViewById(R.id.manageemp);
        viewatt = (TextView)findViewById(R.id.viewatt);

        viewatt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(admin_page.this, view_attendance.class);
                intent.putExtra("type", "admin");
                startActivity(intent);
            }
        });

        getatt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showmonthpicker();
            }
        });

        manageemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(admin_page.this, AllEmployeesPage.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean t = sharedPreferences.edit().putString("username", "").commit();
                if (t) {
                    startActivity(new Intent(admin_page.this, loginpage.class));
                }
            }
        });
    }

    private void checkpermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            Permissions.check(this, permissions, null, null, new PermissionHandler() {
                @Override
                public void onGranted() {
                    Toast.makeText(admin_page.this, "Permissions granted", Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            allper = true;
        }
    }

    private void getemployees() {
        dbremp.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot d : snapshot.getChildren()) {
                    empdata.add(d);
                    khud.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showmonthpicker() {
        int yearSelected;
        int monthSelected;

        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        monthSelected = calendar.get(Calendar.MONTH);

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(monthSelected, yearSelected, "Select the month");

        dialogFragment.show(getSupportFragmentManager(), null);

        dialogFragment.setOnDateSetListener(new MonthYearPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear) {
                khud.show();
                loaddata(year, monthOfYear);
            }
        });
    }

    private void loaddata(int year, int month) {
        dbr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String m = String.valueOf(month + 1);
                if (m.length() == 1) {
                    m = "0" + m;
                }
                String match = String.valueOf(year) + m;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (ds.getKey().toString().contains(match)) {
                        attdata.add(ds);
                    }
                }
                createfileExcel(String.valueOf(year), m);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createfileExcel(String year, String month) {
        String fname = "Attendance_" + year + month;
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator , fname + ".xls");
        File filePath = new File(getExternalFilesDir(null) + "/" + fname + ".xls");

        String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        int rn = 0;
        List<String[]> data = new ArrayList<String[]>();
        for (DataSnapshot att : attdata) {
            int c = 0;
            HSSFSheet hssfSheet = hssfWorkbook.createSheet(att.getKey().toString());
            data.add(new String[]{"S.No.", "Employee Name", "Employee Code", "In Time", "Out Time"});

            HSSFRow hssfRowHead = hssfSheet.createRow(0);
            HSSFCell hssfCell00 = hssfRowHead.createCell(0);
            hssfCell00.setCellValue("S.No.");

            HSSFCell hssfCell11 = hssfRowHead.createCell(1);
            hssfCell11.setCellValue("Employee Name");

            HSSFCell hssfCell22 = hssfRowHead.createCell(2);
            hssfCell22.setCellValue("Employee Code");

            HSSFCell hssfCell33 = hssfRowHead.createCell(3);
            hssfCell33.setCellValue("In Time");

            HSSFCell hssfCell44 = hssfRowHead.createCell(4);
            hssfCell44.setCellValue("Out Time");

            int c1 = 0;

            for (DataSnapshot empds : empdata) {
                c1++;
                HSSFRow hssfRow = hssfSheet.createRow(c1);
                String empid = empds.getKey().toString();
                if (att.hasChild(empid)) {
                    HSSFCell hssfCell0 = hssfRow.createCell(0);
                    hssfCell0.setCellValue(c1);

                    HSSFCell hssfCell1 = hssfRow.createCell(1);
                    hssfCell1.setCellValue(getempname(empid).replace("_", " "));

                    HSSFCell hssfCell2 = hssfRow.createCell(2);
                    hssfCell2.setCellValue(empid.replace("_", " "));

                    HSSFCell hssfCell3 = hssfRow.createCell(3);
                    hssfCell3.setCellValue(att.child(empid).child("IN").child("in_time").getValue().toString());

                    if (att.child(empid).hasChild("OUT")) {
                        HSSFCell hssfCell4 = hssfRow.createCell(4);
                        hssfCell4.setCellValue(att.child(empid).child("OUT").child("out_time").getValue().toString());
                    } else {
                        HSSFCell hssfCell4 = hssfRow.createCell(4);
                        hssfCell4.setCellValue("");
                    }
                } else {
                    HSSFCell hssfCell0 = hssfRow.createCell(0);
                    hssfCell0.setCellValue(c1);

                    HSSFCell hssfCell1 = hssfRow.createCell(1);
                    hssfCell1.setCellValue(getempname(empid).replace("_", " "));

                    HSSFCell hssfCell2 = hssfRow.createCell(2);
                    hssfCell2.setCellValue(empid.replace("_", " "));
                }
            }
            rn++;
        }

        try {
            OutputStream ops = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + "/" +fname + ".xls");
            hssfWorkbook.write(ops);
            khud.dismiss();
            Toast.makeText(this, "File created", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getempname(String ec) {
        String c = "";
        for (DataSnapshot d : empdata) {
            if (d.getKey().toString().equals(ec)) {
                c = d.getValue().toString();
            }
        }

        return c;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}