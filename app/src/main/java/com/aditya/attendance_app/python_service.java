package com.aditya.attendance_app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class python_service extends Service {
    Python py;
    public PyObject pyObject;
    public python_service() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!Python.isStarted()){
            Python.start(new AndroidPlatform(this));
            py = Python.getInstance();

            pyObject = py.getModule("myscript");
        }

        return START_STICKY;
    }
}