package com.example.myappusato.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myappusato.R;
import com.example.myappusato.entity.Ordine;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.ViewHolder>{

    ArrayList<Ordine> orders;
    Context context;
    //immagine
    FirebaseStorage storage;
    StorageReference storageReference;

    public MyOrderAdapter(ArrayList<Ordine> orders, MyOrdersActivity activity)
    {
        this.orders = orders;
        this.context = activity;
    }

    @NonNull
    @Override
    //crea il contenitore dell'ordine
    public MyOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.order_item_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //prende l'ordine e la id immagine
        Ordine order = orders.get(position);
        String idImage = order.getIdImage();
        //crea reference allo storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        StorageReference rf = storageReference.child("/images/"+idImage+".jpg");
        //imposta le text view
        holder.textViewSeller.setText("SellerID: "+order.getIdSeller());
        holder.textViewIdAnnouncement.setText("AnnouncementID: "+order.getIdAnnuncio());
        holder.textViewPrice.setText("Price paid: €" + order.getPricePaid());

        try {
            //imposta le image view
            String suffix = idImage;
            //creo un file temporaneo
            final File localFile = File.createTempFile(idImage, ".jpg");
            //prelevo l'immagine referenziata nel file temporaneo
            rf.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            //decodifico il file per impostarlo sulla image view
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            //usiamo la funzione setImageBitmap perchè abbiamo convertito
                            // in bitmap il file temp
                            holder.orderImage.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Pic not retrived", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        //istanzio e assegno tutte le viste della card
        ImageView orderImage;
        TextView textViewSeller;
        TextView textViewIdAnnouncement;
        TextView textViewPrice;

        public ViewHolder(View itemView){
            super(itemView);
            orderImage = itemView.findViewById(R.id.imagevieworder);
            textViewSeller = itemView.findViewById(R.id.order_seller);
            textViewIdAnnouncement = itemView.findViewById(R.id.order_id);
            textViewPrice = itemView.findViewById(R.id.order_price);
        }


    }
}
