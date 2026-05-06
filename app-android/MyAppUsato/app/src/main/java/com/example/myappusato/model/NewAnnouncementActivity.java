package com.example.myappusato.model;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myappusato.R;
import com.example.myappusato.entity.Annuncio;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewAnnouncementActivity extends AppCompatActivity {

    private AlertDialog.Builder builder;
    private AlertDialog progressDialog;

    private Button bttUpload;
    private Button bttAddAnn;
    private Button bttAddTag;
    private EditText titleView;
    private EditText descView;
    private TextView tagsView;
    private EditText priceView;
    private TextView euro;
    private ImageView imageView;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseStorage fs;

    private String pathDecoded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_announcement);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        fs = FirebaseStorage.getInstance();

        bttAddTag = findViewById(R.id.add_tag);
        bttAddAnn = findViewById(R.id.na_addann);
        bttUpload = findViewById(R.id.na_pick_photo);
        titleView = findViewById(R.id.na_title);
        descView = findViewById(R.id.na_description);
        tagsView = findViewById(R.id.na_tags);
        priceView = findViewById(R.id.na_price);
        priceView.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,2)});
        euro = findViewById(R.id.euro_annuncio);
        imageView = findViewById(R.id.na_image);
        //entra nella galleria per selezionare l'immagine
        bttUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);

            }
        });
        //
        bttAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText edittext = new EditText(NewAnnouncementActivity.this);
                edittext.setText(tagsView.getText().toString());
                new AlertDialog.Builder(NewAnnouncementActivity.this)
                        .setTitle("Add Tag")
                        .setMessage("Separate the tags with #")
                        .setView(edittext)
                        .setNegativeButton(R.string.no, null)
                        .setPositiveButton(R.string.insert, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface di, int i) {
                                //passo la stringa presa in ingresso alla funzione tagsByEditText
                                String myText = edittext.getText().toString();
                                //se la stringa restituita è vuota attiva il toast
                                // e resetta la textView (tags view)
                                String tagsReturn = tagsByEditText(myText);
                                if(tagsReturn.isEmpty()){
                                    tagsView.setText(tagsReturn);
                                    Toast.makeText(NewAnnouncementActivity.this, "Insert one tag at least", Toast.LENGTH_SHORT).show();
                                }else {
                                    tagsView.setText(tagsReturn);
                                }
                            }
                        }).create().show();
            }
        });
        //aggiunge l'annuncio
        bttAddAnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //preleva tutto ciò che è stato inserito
                String title = titleView.getText().toString();
                String price = priceView.getText().toString();
                ArrayList<String> tags = splitTags(tagsView.getText().toString());
                tags.remove(0);
                String desc = descView.getText().toString();

                //controlla sui campi obbligatori
                if(title.isEmpty() || price.isEmpty() || tags.isEmpty() || imageView.getDrawable()==null) {
                    Toast.makeText(NewAnnouncementActivity.this, "Missing Field", Toast.LENGTH_SHORT).show();
                }else{
                    //utilizza progressbar per dare tempo a firestore per caricare
                    //documento+immagine
                    progressDialog = getDialogProgressBar().create();
                    progressDialog.show();
                    //Carico l'immagine su Storage prima
                    //assegnamo hashcode e storage lo imposta path
                    String imageNamePath = hashCodeImage(pathDecoded, user.getUid()).toString();
                    StorageReference firstRef = fs.getReference();
                    StorageReference imageRef = firstRef.child(imageNamePath+".jpg");
                    StorageReference imageFullpathReference = firstRef.child("images/"+imageNamePath+".jpg");

                    //Carico i dati come bytes (copiato da stack overflou)
                    imageView.setDrawingCacheEnabled(true);
                    imageView.buildDrawingCache();
                    Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();
                    //prende il path e carica l'immagine
                    UploadTask uploadTask = imageFullpathReference.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //insuccesso nel caricamento
                            Toast.makeText(NewAnnouncementActivity.this, "Failed upload", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(NewAnnouncementActivity.this, "Successfull upload", Toast.LENGTH_SHORT).show();

                        }
                    });


                    //costruiamo oggetto di tipo annuncio con i dati prelevati
                    //e preparo l'hashcode
                    Annuncio perId = new Annuncio(user.getUid(), desc, user.getDisplayName(), imageNamePath, price, tags, title);
                    String idAnnouncement = hashCodePrepare(perId.getTitle(), perId.getAuthor(), perId.getPrice(), perId.getDisplayName()).toString();
                    //preparo la mappa annuncio
                    Map<String, Object> announcements = new HashMap<>();
                    announcements.put("author", user.getUid());
                    announcements.put("description", desc);
                    announcements.put("displayName", user.getDisplayName());
                    announcements.put("image", imageNamePath);
                    announcements.put("price", price);
                    announcements.put("tags", tags);
                    announcements.put("title", title);
                    //carichiamo la mappa su firestore con id di referenza precedentemente calcolato
                    db.collection("annunci").document(idAnnouncement)
                            .set(announcements)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            //dieci secondi per caricare tutto
                                            //poi passiamo alla browsing activity
                                            Intent i = new Intent(NewAnnouncementActivity.this, BrowsingActivity.class);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(i);
                                            finish();
                                        }
                                    }, 10000);

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });

                }
            }
        });




    }
    //torna al chiamante
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //prendiamo l'immagine dalla galleria e la impostiamo sulla image view
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            File f = new File(selectedImage.getPath());
            pathDecoded = f.getName();
            System.out.println(f.getName());
            imageView.setImageURI(selectedImage);
        }
    }

    public Integer hashCodeImage(String imageName, String uID)
    {
        int result = 17;
        result = 31 * result + imageName.hashCode();
        result = 31 * result + uID.hashCode();
        if(result<0) result = result*(-1);
        return result;
    }

    public ArrayList<String> splitTags(String tag)
    {
        //restituisce arrayList di stringhe
        //inizializza arrayList
        ArrayList<String> tags = new ArrayList<>();
        //crea array di stringhe contenenti tutte le stringhe splittate dal #
        //riferite alla stinga tag in ingresso
        String[] split = tag.split("#");
        //cicla e aggiunge le stringhe all'arrayList
        for(String s : split) {
            tags.add(s);
        }
        //restituito l'arrayList
        return tags;
    }

    public Integer hashCodePrepare(String title, String author, String price, String displayName) {
        int result = 17;
        result = 31 * result + title.hashCode();
        result = 31 * result + author.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + displayName.hashCode();
        return result;
    }

    public String tagsByEditText(String text)
    {
        String complete;
        String finalString = "";
        String[] strings;
        // controlla se c'è il #
        String hashtah = "#";
        if(!text.contains(hashtah)) return complete = "";

        //Rimuove spazi dalla stringa text passata come parametro in ingresso
        complete = text.replaceAll("\\s+","");
        //prendo il nr della posizione in cui si trova il primo cancelletto
        Integer position = complete.indexOf("#");
        //crea una sottostringa partendo dal primo cancelletto alla fine della stringa
        String sub = complete.substring(position);
        //diamo all'array vuoto tutte le stringhe separate dai #
        //input:"#word1#word2#word3
        //output:word1 word2 word3 vanno contenute nell'array strings
        strings = sub.split("#");




        //controlla se ci sono piu di un # necessario oppure il tag è vuoto
        for(int i=0; i<strings.length;)
        {
            String temp = strings[i];
            String check = temp.replace("#", "");
            if(!check.isEmpty()){
                finalString = finalString.concat("#");
                finalString = finalString.concat(check);
                i++;

            }else
                i++;

        }

        return finalString;

    }
    //costruisco la progressbar (copiato su stak overflou)
    public AlertDialog.Builder getDialogProgressBar() {

        if (builder == null) {
            builder = new AlertDialog.Builder(this);

            builder.setTitle("Loading...");

            final ProgressBar progressBar = new ProgressBar(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            progressBar.setLayoutParams(lp);
            builder.setView(progressBar);
        }
        return builder;
    }
    //torna su browsing activity
    @Override
    public void onBackPressed() {
        Intent i = new Intent(NewAnnouncementActivity.this, BrowsingActivity.class);
        finish();
        startActivity(i);
    }

}