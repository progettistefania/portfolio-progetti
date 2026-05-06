package com.example.myappusato.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myappusato.R;
import com.example.myappusato.entity.Annuncio;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{

    ArrayList<Annuncio> annunci;
    Context context;
    //Image
    FirebaseStorage storage;
    StorageReference storageReference;

    public MyAdapter(ArrayList<Annuncio> annunci, BrowsingActivity activity){
        this.annunci = annunci;
        this.context = activity;
    }

    @NonNull
    @Override
    //creiamo la card view
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.annuncio_item_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
    //imposto i contenuti prelevati sulla card view
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Annuncio annuncio = annunci.get(position);
        String imageID = annuncio.getImage();
        //ricostruisco la stringa con #
        ArrayList<String> tags = annuncio.getTags();
        String textTags = "";
        for(String tag : tags){
            String hashtag = "#";
            //concateno con #
            tag = hashtag.concat(tag);
            textTags = textTags.concat(tag);
        }
        //chiediamo a firebase storege la storage reference dell'immagine collegata all'annuncio
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        StorageReference rf = storageReference.child("/images/"+imageID+".jpg");
        //impostiamo sulla card view tutte le text view
        holder.textViewName.setText(annuncio.getDisplayName());
        holder.textViewTitle.setText(annuncio.getTitle());
        holder.textViewTags.setText(textTags);
        holder.textViewPrice.setText("€" + annuncio.getPrice());

        try {
            //creo un file temporaneo
            String suffix = imageID;
            final File localFile = File.createTempFile(imageID, ".jpg");
            //prelevo l'immagine referenziata nel file temporaneo
            rf.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            //decodifico il file per impostarlo sulla image view
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            //usiamo la funzione setImageBitmap perchè abbiamo convertito
                            // in bitmap il file temp
                            holder.announcementImage.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Drawable id = context.getResources().getDrawable(R.mipmap.ic_launcher);
                            holder.announcementImage.setImageDrawable(id);
                            //altrimenti imposto la mipmap di icLouncer
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
        //passo dalla browsing activity a myAnnouncement activity passandogli come parametro extra
        //l'annuncio relativo alla card view cliccata
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), MyAnnouncementActivity.class);
                //passo l'annuncio all'activity MyAnnouncement
                i.putExtra("ANNUNCIO", annuncio);
                view.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return annunci.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        //istanzio e assegno tutte le viste della card
        ImageView announcementImage;
        TextView textViewName;
        TextView textViewTitle;
        TextView textViewTags;
        TextView textViewPrice;

        public ViewHolder(View itemView){
            super(itemView);
            announcementImage = itemView.findViewById(R.id.imageview);
            textViewName = itemView.findViewById(R.id.annuncio_author);
            textViewTitle = itemView.findViewById(R.id.annuncio_title);
            textViewTags = itemView.findViewById(R.id.annuncio_tags);
            textViewTags.setMovementMethod(new ScrollingMovementMethod());
            textViewPrice = itemView.findViewById(R.id.annuncio_price);
        }


    }

}
