package com.aditya.attendance_app;

public class Add_in_time {
    String in_time;
    double latitude, longitude;
    String address;

    public Add_in_time(String in_time, double latitude, double longitude, String address) {
        this.in_time = in_time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public String getIn_time() {
        return in_time;
    }

    public void setIn_time(String in_time) {
        this.in_time = in_time;
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
