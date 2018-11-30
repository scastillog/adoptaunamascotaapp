package com.enriquecastillo.adopta_mascotas.ui.post;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.enriquecastillo.adopta_mascotas.MainActivity;
import com.enriquecastillo.adopta_mascotas.R;
import com.enriquecastillo.adopta_mascotas.models.Pet;
import com.enriquecastillo.adopta_mascotas.models.User;
import com.enriquecastillo.adopta_mascotas.ui.login.loginemail.EmailLoginActivity;
import com.enriquecastillo.adopta_mascotas.ui.map.MapsActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;

public class PostEditActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    @BindView(R.id.toolbar)
    Toolbar myToolbar;
    @BindView(R.id.imageview_pet_post)
    ImageView imageViewPet;
    @BindView(R.id.radioButtonDog)
    RadioButton radioButtonDog;
    @BindView(R.id.radioButtonCat)
    RadioButton radioButtonCat;
    @BindView(R.id.radioButtonOther)
    RadioButton radioButtonOther;
    @BindView(R.id.radioButtonMale)
    RadioButton radioButtonMale;
    @BindView(R.id.radioButtonFemale)
    RadioButton radioButtonFemale;
    @BindView(R.id.et_pet_name)
    TextInputLayout edtPetName;
    @BindView(R.id.et_pet_description)
    TextInputLayout edtPetDescription;
    @BindView(R.id.progress_bar_save)
    ProgressBar progressBarSave;
    @BindView(R.id.mLinearLayout_Post)
    LinearLayout linearLayout;
    @BindView(R.id.btn_save_post)
    Button buttonSavePost;

    private MapView mapViewPost;
    private GoogleMap gMap;
    private LocationManager manager;
    private Location location;
    private Circle circle;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private final int RC_SELECT_PICTURE = 10;
    private final int RC_CAMERA = 20;
    private final int RC_PERMISSION = 90;

    private final LatLng mDefaultLocation = new LatLng(4.6097102, -74.081749);
    private static final int DEFAULT_ZOOM = 10;

    private String key = "";

    //Creando feature camara
    private String mCurrentPhotoPath;

    private Realm realm;
    private User user;
    private RealmList<Pet> pets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit);
        ButterKnife.bind(this);
        setUpToolbar();
        realm = Realm.getDefaultInstance();

        initializeFirebase();

        if (getIntent().getExtras() != null) {
            key = getIntent().getStringExtra("key");
        }

        if (myRequestPermission()) {
            buttonSavePost.setEnabled(true);
        } else {
            buttonSavePost.setEnabled(false);
        }

        checkIfGPSSignalIsEnabled();

        mapViewPost = findViewById(R.id.map_post);

        if (mapViewPost != null) {
            mapViewPost.onCreate(null);
            mapViewPost.onResume();
            mapViewPost.getMapAsync(this);
        }

    }

    @OnClick(R.id.imageview_pet_post)
    void addPhotoPet() {
        showOptions();
    }

    private void showOptions() {
        final CharSequence[] option = {"Tomar foto", "Elegir galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(PostEditActivity.this);
        builder.setTitle("Elige una Opcion");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        takePictureScene();
                        break;
                    case 1:
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Selecciona app de imagen"), RC_SELECT_PICTURE);
                        break;
                    case 2:
                        dialogInterface.dismiss();
                        break;
                }
            }
        })
                .create().show();
    }

    private void takePictureScene() {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intentCamera.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.enriquecastillo.adopta_mascotas", photoFile);
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoUri );
                startActivityForResult(intentCamera, RC_CAMERA);
            }
        }
    }

    private File createImage() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String imageFileName = "JPEG_" + timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_CAMERA:
                    File f = new File(mCurrentPhotoPath);
                    Uri contentUri = Uri.fromFile(f);
                    Picasso.get().load(contentUri).into(imageViewPet);
                    break;
                case RC_SELECT_PICTURE:
                    Uri path = data.getData();
                    mCurrentPhotoPath = path.toString();
                    Picasso.get().load(mCurrentPhotoPath).into(imageViewPet);
                    break;
            }
        }
    }

    private void checkIfGPSSignalIsEnabled() {
        try {
            int gpsSignal = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);
            if (gpsSignal == 0) {
                //El gps no esta activado
                AlertDialog.Builder builder = new AlertDialog.Builder(PostEditActivity.this);
                builder.setTitle("Ubicacion es requerida")
                        .setMessage("Por favor activa tu ubicacion, solo proporciona tu ubicacion cuando entren en contacto contigo.")
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(PostEditActivity.this, "Por favor activa tu ubicacion", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean myRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            Snackbar.make(linearLayout, "Los permisos son necesarios para poder usar la aplicacion", Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, RC_PERMISSION);
                        }
                    }).show();
        } else {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, RC_PERMISSION);
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case RC_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buttonSavePost.setEnabled(true);
                } else {
                    showExplanation();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    private void showExplanation() {
        new AlertDialog.Builder(PostEditActivity.this)
                .setTitle("Permisos denegados")
                .setMessage("Para usar las funciones de la app debes aceptar los permisos")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
    }

    private void putCircleOptions(LatLng place) {
        circle = gMap.addCircle(new CircleOptions()
                .center(place)
                .radius(800)
                .fillColor(Color.argb(80, 0, 150, 136))
                .strokeColor(Color.argb(0, 0, 150, 136))
                .strokeWidth(5));

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (location != null) {
                    Intent intent = new Intent(PostEditActivity.this, MapsActivity.class);
                    intent.putExtra("Lat", location.getLatitude());
                    intent.putExtra("Lng", location.getLongitude());
                    startActivity(intent);
                }
            }
        });

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (getIntent().getExtras() != null && key.equals("post")) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                gMap.setMyLocationEnabled(true);
                gMap.getUiSettings().setMyLocationButtonEnabled(false);
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
                return;
            }
        } else if (getIntent().getExtras() != null && key.equals("maps")) {
            double lat = getIntent().getExtras().getDouble("latitude");
            double lng = getIntent().getExtras().getDouble("longitude");
            LatLng place = new LatLng(lat, lng);
            putCircleOptions(place);
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 13));
            location = new Location(LocationManager.NETWORK_PROVIDER);
            this.location.setLatitude(lat);
            this.location.setLongitude(lng);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lastKnownLocation = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        location = lastKnownLocation;
        LatLng place = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 13));
        putCircleOptions(place);

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        if (circle == null) {
            LatLng place = new LatLng(location.getLatitude(), location.getLongitude());
            putCircleOptions(place);
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 13));
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location lastKnownLocation = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            this.location = lastKnownLocation;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        gMap.clear();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(PostEditActivity.this, "Por favor activa tu Ubicacion y el internet", Toast.LENGTH_SHORT).show();
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));

    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        user = realm.where(User.class).equalTo("email", firebaseUser.getEmail()).findFirst();

    }

    @OnClick(R.id.btn_save_post)
    void onSaveButton() {
        if (TextUtils.isEmpty(edtPetName.getEditText().getText())) {
            edtPetName.setError("Llena bien este campo");
            return;
        }
        if (TextUtils.isEmpty(edtPetDescription.getEditText().getText())) {
            edtPetDescription.setError("Llena bien este campo");
            return;
        }

        if (getClassPet().equals("")) {
            Toast.makeText(PostEditActivity.this, "Por favor escoge un tipo de mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getGeneroPet() == 0) {
            Toast.makeText(PostEditActivity.this, "Por favor escoge un tipo de mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = edtPetName.getEditText().getText().toString();
        String description = edtPetDescription.getEditText().getText().toString();
        String classPet = getClassPet();
        int generePet = getGeneroPet();

        createInDBPet(name, description, classPet, generePet, mDefaultLocation, mCurrentPhotoPath);
        Toast.makeText(this, "Mascota creada con exito", Toast.LENGTH_SHORT).show();
        goToMainActivity();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(PostEditActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void createInDBPet(String name, String description, String classPet, int generePet, LatLng mDefaultLocation, String mCurrentPhotoPath) {
        realm.beginTransaction();
        Pet pet = new Pet(name, classPet, description, generePet, mDefaultLocation.latitude, mDefaultLocation.longitude, mCurrentPhotoPath);
        realm.copyToRealm(pet);
        user.getPets().add(pet);
        realm.commitTransaction();
    }

    private String getClassPet() {
        if (radioButtonDog.isChecked() | radioButtonCat.isChecked() | radioButtonOther.isChecked()) {
            if (radioButtonCat.isChecked()) {
                return "cat";
            } else if (radioButtonDog.isChecked()) {
                return "dog";
            } else {
                return "other";
            }
        } else {
            return "";
        }
    }

    private int getGeneroPet() {
        if (radioButtonMale.isChecked() | radioButtonFemale.isChecked()) {
            if (radioButtonFemale.isChecked()) {
                return 1;
            } else {
                return 2;
            }
        } else {
            return 0;
        }
    }

    @Override
    protected void onDestroy() {
        realm.removeAllChangeListeners();
        realm.close();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentPhotoPath != null){
            outState.putString("Uri", mCurrentPhotoPath);
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("Uri")) {
            mCurrentPhotoPath = savedInstanceState.getString("Uri");
        }
    }

    private void setUpToolbar() {
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Crea una Mascota");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
