package com.aditya.attendance_app;

public class Add_out_time {
    String out_time;
    double latitude, longitude;
    String address;

    public Add_out_time(String out_time, double latitude, double longitude, String address) {
        this.out_time = out_time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public String getOut_time() {
        return out_time;
    }

    public void setOut_time(String out_time) {
        this.out_time = out_time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
