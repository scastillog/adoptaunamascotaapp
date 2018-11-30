package com.enriquecastillo.adopta_mascotas.app;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.enriquecastillo.adopta_mascotas.models.Pet;
import com.enriquecastillo.adopta_mascotas.models.User;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class MyApplication extends Application{


    public static AtomicInteger UserId = new AtomicInteger();
    public static AtomicInteger PetId = new AtomicInteger();


    @Override
    public void onCreate() {
        super.onCreate();

        setUpRealConfiguration();

        Realm realm = Realm.getDefaultInstance();
        UserId = getIdByTable(realm, User.class);
        PetId = getIdByTable(realm, Pet.class);
        realm.close();

    }

    private void setUpRealConfiguration() {
        Realm.init(getApplicationContext());

        RealmConfiguration config = new RealmConfiguration
                .Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }

    private <T extends RealmObject> AtomicInteger getIdByTable(Realm realm , Class<T> anyClass){
        RealmResults<T> results = realm.where(anyClass).findAll();
        if ((results.size() > 0 )){
            return new AtomicInteger(results.max("id").intValue());
        } else {
            return new AtomicInteger() ;
        }
    }

}
