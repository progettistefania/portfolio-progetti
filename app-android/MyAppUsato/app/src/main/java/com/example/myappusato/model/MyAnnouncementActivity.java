package com.example.myappusato.model;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myappusato.R;
import com.example.myappusato.entity.Annuncio;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyAnnouncementActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference rf;

    private ImageView imageView;
    private TextView titleView;
    private TextView authorView;
    private TextView descView;
    private TextView priceView;
    private Button buyBtt;

    private String uID;
    private Annuncio annuncio;
    private String idAnnuncio;

    private DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_announcement);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uID = user.getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        imageView = findViewById(R.id.iw_photo);
        titleView = findViewById(R.id.ann_title);
        authorView = findViewById(R.id.ann_auth);
        descView = findViewById(R.id.ann_description);
        priceView = findViewById(R.id.ann_price);
        buyBtt = findViewById(R.id.buy_bttn);
        //prelevo l'annuncio passatomi dalla card view della browsing activity
        //inizializzo l'oggetto annuncio
        annuncio = (Annuncio) getIntent().getSerializableExtra("ANNUNCIO");


        titleView.setText(annuncio.getTitle());
        authorView.setText(annuncio.getDisplayName());
        descView.setText(annuncio.getDescription());
        priceView.setText("€" + annuncio.getPrice());
        //faccio lhashcode dell'id dell'annucio
        idAnnuncio = hashCodePrepare(annuncio.getTitle(), annuncio.getAuthor(), annuncio.getPrice(), annuncio.getDisplayName()).toString();


        //creo la referenza allo store di firebase e prelevo l'immagine
        StorageReference rf = firebaseStorage.getReference().child("/images/"+annuncio.getImage()+".jpg");
        try {
            String suffix = annuncio.getImage();
            final File localFile = File.createTempFile(suffix, ".jpg");
            rf.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            //Toast.makeText(MyAnnouncementActivity.this, "Pic retrived", Toast.LENGTH_SHORT).show();
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            imageView.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MyAnnouncementActivity.this, "Pic not retrived", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
        //se l'utente corrente è lo stesso dell'id autore che ha creato l'annucio
        //imposto il testo del bottone a remove
        if(uID.equals(annuncio.getAuthor())){
            buyBtt.setText("Remove");
        }

        buyBtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //(venditore)rimuove l'annuncio ma prima chiede conferma
                if(buyBtt.getText().equals("Remove")){
                    new AlertDialog.Builder(MyAnnouncementActivity.this)
                            .setTitle("Delete File")
                            .setMessage("Are you sure, you want to delete this file?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //se viene cliccato delete la funzione deleteDocumente elimina documento e immagine
                                    deleteDocument(idAnnuncio, annuncio.getImage());
                                }
                            }).setNegativeButton("Cancel", null)
                            .create().show();


                }
                //(compratore)compra l'annuncio
                else{
                    new android.app.AlertDialog.Builder(MyAnnouncementActivity.this)
                            .setTitle("Do you want to buy this item?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Buy", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //se l'utente acquista prepara l'id dell'ordine tramite hashcode
                                    //aggiungo tramite la funzione addOrder l'ordine effettuato alla collezione
                                    //su firestore orders passando l'id codificato
                                    //cancello l'annuncio da firestore
                                    String idOrder = hashCodeOrder(annuncio.getAuthor(), idAnnuncio, annuncio.getPrice(), user.getUid()).toString();
                                    addOrder(idOrder);
                                    deleteOnlyAnnouncement(idAnnuncio);
                                }
                            }).create().show();

                }

            }
        });


    }
    //(venditore)
    public void deleteDocument(String idAnnuncioPrepared, String idImagePrepared)
    {

        // crea una reference all'mmagine relativa all'annuncio
        StorageReference storageRef = firebaseStorage.getReference();
        StorageReference imagesRef = storageRef.child("images");
        StorageReference spaceRef = storageRef.child("images/"+ idImagePrepared+".jpg");

        // cancella l'immagine tramite reference passata
        spaceRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Element deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
        //diamo alla document reference la referenza dell'annuncio con id preparato
        documentReference = firebaseFirestore.collection("annunci").document(idAnnuncioPrepared);
        //tramite reference cancella il documento
        documentReference.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MyAnnouncementActivity.this, "Done it", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(MyAnnouncementActivity.this, BrowsingActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }else
                            Toast.makeText(MyAnnouncementActivity.this, "Failed", Toast.LENGTH_SHORT).show();

                    }
                });


    }
    //preparo l'hashcode dell'annuncio
    public Integer hashCodePrepare(String title, String author, String price, String displayName) {
        int result = 17;
        result = 31 * result + title.hashCode();
        result = 31 * result + author.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + displayName.hashCode();
        return result;
    }
    //(compratore)cancella solo il documento annuncio dalla collezione senza però
    //cancellare l'immagine
    public void deleteOnlyAnnouncement(String idAnnuncioPrepared)
    {
        documentReference = firebaseFirestore.collection("annunci").document(idAnnuncioPrepared);
        documentReference.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(MyAnnouncementActivity.this, "Done it", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(MyAnnouncementActivity.this, BrowsingActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }else
                            Toast.makeText(MyAnnouncementActivity.this, "Failed", Toast.LENGTH_SHORT).show();

                    }
                });
    }
    //(compratore)crea documento ordine su firestore
    public void addOrder(String idOrder)
    {
        //preparo la mappa ordine
        Map<String, Object> orderToInsert = new HashMap<>();
        //string deve essere uguale al campo su firestore
        orderToInsert.put("idAnnouncement", idAnnuncio);
        orderToInsert.put("idBuyer", user.getUid());
        orderToInsert.put("idImage", annuncio.getImage());
        orderToInsert.put("idSeller", annuncio.getAuthor());
        orderToInsert.put("pricePaid", annuncio.getPrice());
        //carichiamo la mappa su firestore con id di referenza precedentemente calcolato
        firebaseFirestore.collection("orders").document(idOrder)
                .set(orderToInsert)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }
    //prepara l'hashcode per l'ordine
    public Integer hashCodeOrder(String seller, String idAnnuncio, String price, String idBuyer) {
        int result = 17;
        result = 31 * result + seller.hashCode();
        result = 31 * result + idAnnuncio.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + idBuyer.hashCode();
        return result;
    }

}