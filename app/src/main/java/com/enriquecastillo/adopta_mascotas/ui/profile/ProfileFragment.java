package com.enriquecastillo.adopta_mascotas.ui.profile;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.enriquecastillo.adopta_mascotas.R;
import com.enriquecastillo.adopta_mascotas.adapters.PetAdapter;
import com.enriquecastillo.adopta_mascotas.models.Pet;
import com.enriquecastillo.adopta_mascotas.models.User;
import com.enriquecastillo.adopta_mascotas.utils.SpaceItemDecoration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    @BindView(R.id.txt_profile_name)
    TextView txtName;
    @BindView(R.id.txt_profile_number)
    TextView txtNumnber;
    @BindView(R.id.iv_profile_image)
    ImageView imageView;


    private Realm realm;
    private User user;
    private RealmList<Pet> pets;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recyclerView ;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        realm = Realm.getDefaultInstance();
        initializeFirebase();

        recyclerView = view.findViewById(R.id.recycler_view_profile);
        layoutManager = new GridLayoutManager(getContext(), calculateNoOfColumns(getContext()), LinearLayoutManager.VERTICAL, false);

        adapter = new PetAdapter(getActivity(), R.layout.item_small_pet, user.getPets(), new PetAdapter.OnItemListener() {
            @Override
            public void onItemClickListener(Pet pet, int position) {

            }
        });
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_little);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        putDateInProfile();

        return view;
    }

    private void putDateInProfile() {
        Picasso.get().load(user.getPhoto()).into(imageView);
        txtName.setText(user.getName());
        txtNumnber.setText(user.getNumber());

    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        user  = realm.where(User.class).equalTo("email", firebaseUser.getEmail()).findFirst();

    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int numColumns = (int) (dpWidth / 170);
        return numColumns;
    }

    @OnClick(R.id.btn_edit_profile)
    void onEditProfile(){
        Intent intent = new Intent(getContext(), ProfileEditActivity.class);
        intent.putExtra("key", "profile");
        startActivity(intent);
    }


}
