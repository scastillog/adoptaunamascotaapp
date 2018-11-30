package com.enriquecastillo.adopta_mascotas.models;

import com.enriquecastillo.adopta_mascotas.app.MyApplication;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class User extends RealmObject {
    @PrimaryKey
    private int id;
    @Required
    private String name;
    @Required
    private String email;
    @Required
    private String number;
    @Required
    private String photo;
    private RealmList<Pet> pets;

    public User() {
    }

    public User( String name, String email, String number, String photo) {
        this.id = MyApplication.UserId.incrementAndGet();
        this.name = name;
        this.email = email;
        this.number = number;
        this.photo = photo;
        this.pets = new RealmList<Pet>();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public RealmList<Pet> getPets() {
        return pets;
    }

}
