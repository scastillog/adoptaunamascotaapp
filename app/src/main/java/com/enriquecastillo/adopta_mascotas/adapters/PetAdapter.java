package com.enriquecastillo.adopta_mascotas.adapters;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.enriquecastillo.adopta_mascotas.R;
import com.enriquecastillo.adopta_mascotas.models.Pet;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.ViewHolder> {

    private Activity activity;
    private int layout;
    private List<Pet> pets;
    private OnItemListener listener;

    public PetAdapter(Activity activity, int layout, List<Pet> pets, OnItemListener listener) {
        this.activity = activity;
        this.layout = layout;
        this.pets = pets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(pets.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageViewPhoto;
        TextView textNamePet;
        ImageView imageViewType;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewPhoto = (ImageView) itemView.findViewById(R.id.image_view_small_pet);
            textNamePet = (TextView) itemView.findViewById(R.id.txt_name_pet);
            imageViewType = (ImageView) itemView.findViewById(R.id.iv_type_pet);
        }

        public void bind(final Pet pet, final OnItemListener listener){
            Picasso.get().load(pet.getUriPhoto()).into(imageViewPhoto);
            textNamePet.setText(pet.getName());
            setImageType(pet.getClass_pet());

        }


        private void setImageType(String class_pet) {
            if (class_pet.equals("dog")){
                Picasso.get().load(R.drawable.dog_color).into(imageViewType);
            } else if (class_pet.equals("cat")){
                Picasso.get().load(R.drawable.cat_color).into(imageViewType);
            } else {
                Picasso.get().load(R.drawable.pawprint_black).into(imageViewType);
            }
        }

    }

    public interface OnItemListener{
        void onItemClickListener(Pet pet, int position);
    }
}
