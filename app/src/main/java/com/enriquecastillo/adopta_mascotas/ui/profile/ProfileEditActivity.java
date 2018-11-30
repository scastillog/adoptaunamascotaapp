package com.enriquecastillo.adopta_mascotas.ui.profile;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.enriquecastillo.adopta_mascotas.MainActivity;
import com.enriquecastillo.adopta_mascotas.R;
import com.enriquecastillo.adopta_mascotas.models.User;
import com.enriquecastillo.adopta_mascotas.ui.login.loginemail.EmailLoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ProfileEditActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    //Creando feature camara
    private String mCurrentPhotoPath;
    private final int RC_SELECT_PICTURE = 10;
    private final int RC_CAMERA = 20;
    private final int RC_PERMISSION = 90;

    @BindView(R.id.et_profile_name)
    TextInputLayout edtName;
    @BindView(R.id.et_profile_email)
    TextInputLayout edtEmail;
    @BindView(R.id.et_profile_contacto)
    TextInputLayout edtNumber;
    @BindView(R.id.image_view_ProfileEdit)
    ImageView imageViewProfile;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.mLinearLayout_Profile)
    LinearLayout linearLayout;

    private Realm realm;
    private RealmResults<User> users;
    private User user;
    private String imageUrl;

    private String key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        ButterKnife.bind(this);
        setUpToolbar();
        initializeFirebase();

        if (getIntent().getExtras() != null) {
            key = getIntent().getStringExtra("key");
        }
        realm = Realm.getDefaultInstance();
        users = realm.where(User.class).findAll();

        if (myRequestStoragePermission()) { imageViewProfile.setEnabled(true);}
        else { imageViewProfile.setEnabled(false);}
        
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    edtEmail.getEditText().setText(firebaseUser.getEmail());
                    edtName.getEditText().setText(firebaseUser.getDisplayName());
                    edtNumber.getEditText().setText(firebaseUser.getPhoneNumber());
                    imageUrl = firebaseUser.getPhotoUrl().toString();
                    user = realm.where(User.class).equalTo("email", firebaseUser.getEmail()).findFirst();
                }
            }
        };
    }

    @OnClick(R.id.btn_save_profile)
    void onSaveProfile() {
        if (TextUtils.isEmpty(edtName.getEditText().getText())) {
            edtEmail.setError("Llena bien este campo");
            return;
        }
        if (TextUtils.isEmpty(edtEmail.getEditText().getText())) {
            edtEmail.setError("Llena bien este campo");
            return;
        }

        if (!EmailLoginActivity.isValidEmail(edtEmail.getEditText().getText().toString())) {
            edtEmail.setError("Digita un correo valido");
            return;
        }

        if (TextUtils.isEmpty(edtNumber.getEditText().getText())) {
            edtNumber.setError("Llena bien este campo");
            return;
        }

        if (6 > edtNumber.getEditText().getText().toString().length()) {
            edtNumber.setError("El numero debe tener mas de 6 caracteres");
            return;
        }

        String name = edtName.getEditText().getText().toString();
        String email = edtEmail.getEditText().getText().toString().trim();
        String number = edtNumber.getEditText().getText().toString();

        if (key.equals("create")) {
            createAccountInDB(name, email, number);
        } else if (key.equals("profile")) {
            //Actualizar el usuario
            editAccountInDB(name, email, number);
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.image_view_ProfileEdit)
    void onCLickImageProfile(){
        showOptions();
    }

    private void showOptions() {
        final CharSequence[] option = {"Tomar foto", "Elegir galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(ProfileEditActivity.this);
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
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intentCamera, RC_CAMERA);
            }
        }
    }

    private File createImage() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

    private void showExplanation() {
        new AlertDialog.Builder(ProfileEditActivity.this)
                .setTitle("Permisos denegados")
                .setMessage("Para usar las funciones de la app debes aceptar los permisos")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(),null );
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case RC_CAMERA:
                    MediaScannerConnection.scanFile(ProfileEditActivity.this,
                            new String[]{mCurrentPhotoPath}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned"+path);
                                    Log.i("ExternalStorage", "-> Uri = "+uri);
                                }
                            });
                    File f = new File(mCurrentPhotoPath);
                    Picasso.get().load(Uri.fromFile(f)).fit().into(imageViewProfile);
                    break;
                case RC_SELECT_PICTURE:
                    Uri path = data.getData();
                    Picasso.get().load(path).into(imageViewProfile);
                    break;
            }
        }

    }

    private boolean myRequestStoragePermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }
        if ((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(CAMERA)) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))){
            Snackbar.make(linearLayout, "Los permisos son necesarios para poder usar la aplicacion", Snackbar.LENGTH_INDEFINITE )
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(View view) {
                            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, RC_PERMISSION);
                        }
                    }).show();
            return true;
        } else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, RC_PERMISSION);
        }
        return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSION){
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permisos Aceptados", Toast.LENGTH_SHORT).show();
                imageViewProfile.setEnabled(true);
            }
        } else {
            showExplanation();
        }
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edita tu Perfil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mCurrentPhotoPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentPhotoPath = savedInstanceState.getString("file_path");
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);

    }

    @Override
    protected void onDestroy() {
        realm.removeAllChangeListeners();
        realm.close();
        super.onDestroy();
    }

    //CRUD Actions

    private void createAccountInDB(String name, String email, String number) {
        realm.beginTransaction();
        User user = new User(name, email, number, imageUrl);
        realm.copyToRealm(user);
        realm.commitTransaction();
    }

    private void editAccountInDB(String name, String email, String number) {
        realm.beginTransaction();
        user.setName(name);
        user.setEmail(email);
        user.setNumber(number);
        realm.copyToRealmOrUpdate(user);
        realm.commitTransaction();

    }

}
