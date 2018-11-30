package com.enriquecastillo.adopta_mascotas.ui.post;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.enriquecastillo.adopta_mascotas.R;
import com.enriquecastillo.adopta_mascotas.adapters.PostAdapter;
import com.enriquecastillo.adopta_mascotas.models.Pet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment  implements RealmChangeListener<RealmResults<Pet>>{

    @BindView(R.id.fabCreatePos)
    FloatingActionButton fabCreate;

    private Realm realm;
    private RealmResults<Pet> pets;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private LinearLayoutManager layoutManager;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        ButterKnife.bind(this, view);
        realm = Realm.getDefaultInstance();

        pets = realm.where(Pet.class).findAll();

        recyclerView = view.findViewById(R.id.recycler_view_post);
        layoutManager = new LinearLayoutManager(getActivity());

        adapter = new PostAdapter(getActivity(), R.layout.item_big_pet, pets, new PostAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Pet pet, int position) {
                Toast.makeText(getActivity(), pet.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        pets.addChangeListener(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fabCreate.getVisibility() == View.VISIBLE) {
                    fabCreate.hide();
                } else if (dy < 0 && fabCreate.getVisibility() != View.VISIBLE) {
                    fabCreate.show();
                }
            }
        });

        return view;
    }

    @OnClick(R.id.fabCreatePos)
    void onClickPOst(){
        Intent intent = new Intent(getActivity(), PostEditActivity.class);
        intent.putExtra("key", "post");
        startActivity(intent);
    }

    @Override
    public void onChange(RealmResults<Pet> pets) {
        adapter.notifyDataSetChanged();
    }

}
