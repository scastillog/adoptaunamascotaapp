package com.enriquecastillo.adopta_mascotas.models;

import android.net.Uri;

import com.enriquecastillo.adopta_mascotas.app.MyApplication;
import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Pet extends RealmObject{
    @PrimaryKey
    private int id;
    private String name;
    private String class_pet;
    private String description;
    private double latitude;
    private double longitude;
    private int genero;
    private String uriPhoto;

    public int getGenero() {
        return genero;
    }

    public void setGenero(int genero) {
        this.genero = genero;
    }

    public Pet() {

    }

    public Pet(String name, String class_pet, String description, int genero, double latitude, double longitude, String uriPhoto) {
        this.id = MyApplication.PetId.incrementAndGet();
        this.name = name;
        this.class_pet = class_pet;
        this.description = description;
        this.genero = genero;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uriPhoto = uriPhoto;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClass_pet() {
        return class_pet;
    }

    public void setClass_pet(String class_pet) {
        this.class_pet = class_pet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getUriPhoto() {
        return uriPhoto;
    }

    public void setUriPhoto(String uriPhoto) {
        this.uriPhoto = uriPhoto;
    }
}
