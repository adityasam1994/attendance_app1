package com.aditya.attendance_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Struct;

public class MyAsynctask extends AsyncTask<String, String, String> {
    Context context;
    String result;
    ProgressDialog progressDialog;
    Python py;
    static PyObject pyObject1;

    public MyAsynctask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //progressDialog = ProgressDialog.show(context, "Progress Dialog", null);
    }

    @Override
    protected String doInBackground(String... args) {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(context));

            py = Python.getInstance();

            pyObject1 = py.getModule("myscript");
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... text) {
        //progressDialog.setMessage(text[0]);
    }

    protected void onPostExecute(String result) {
        //progressDialog.dismiss();
        ((option_page) context).python_set = true;
        ((option_page) context).pyObject = pyObject1;
        if (((option_page) context).location_set == true){
            ((option_page) context).khud.dismiss();
        }

    }
}
