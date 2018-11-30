package com.enriquecastillo.adopta_mascotas.ui.map;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.enriquecastillo.adopta_mascotas.R;
import com.enriquecastillo.adopta_mascotas.ui.post.PostEditActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.toolbar) Toolbar myToolbar;

    private GoogleMap mMap;
    private LatLng location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Bundle bundle = getIntent().getExtras();
        double lat = bundle.getDouble("Lat");
        double lng = bundle.getDouble("Lng");
        location = new LatLng(lat, lng);

        ButterKnife.bind(this);
        setUpToolbar();

    }

    private void setUpToolbar() {
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Ubicacion Mascota");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.btn_save_ubicacion)
    void onSaveLocation(){
        Toast.makeText(this, "Posicion boton" + location, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MapsActivity.this, PostEditActivity.class);
        intent.putExtra("key", "maps");
        intent.putExtra("latitude", location.latitude);
        intent.putExtra("longitude", location.longitude);
        startActivity(intent);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Tu ubicacion actual")
                .draggable(false))
                .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                location = mMap.getCameraPosition().target;
            }
        });
    }

}
