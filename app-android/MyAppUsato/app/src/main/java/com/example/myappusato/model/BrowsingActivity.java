package com.example.myappusato.model;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myappusato.R;
import com.example.myappusato.entity.Annuncio;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class BrowsingActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user;
    FirebaseAuth mAuth;
    DocumentReference docRef;
    ArrayList<Annuncio> annuncioArrayList;
    RecyclerView recyclerView;
    MyAdapter myAdapter;

    Button btt_addTags;
    TextView tagsAdded;

    String flagPrice;
    ListenerRegistration lr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browsing);

        flagPrice = "all_prices";
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        annuncioArrayList = new ArrayList<Annuncio>();

        btt_addTags = findViewById(R.id.button_tag_filter);
        tagsAdded = findViewById(R.id.tags_filter_text);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        myAdapter = new MyAdapter(annuncioArrayList, BrowsingActivity.this);
        recyclerView.setAdapter(myAdapter);
        //fa retriveing dei dati da firebase e aggiunge ad annuncioArrayList

        lr = eventChangeListener();
        //v
        //bottone per inserire i tag
        btt_addTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText edittext = new EditText(BrowsingActivity.this);
                edittext.setText(tagsAdded.getText().toString());
                new AlertDialog.Builder(BrowsingActivity.this)
                        .setTitle("Filter by tags")
                        .setMessage("Separate the tags with #")
                        .setView(edittext)
                        .setNegativeButton(R.string.no, null)
                        .setPositiveButton(R.string.insert, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface di, int i) {
                                String myText = edittext.getText().toString();
                                //passo la stringa presa in ingresso alla funzione tagsByEditText
                                String tagsReturn = tagsByEditText(myText);
                                //se la stringa restituita è vuota attiva il toast
                                // e resetta la textView (tagsAdded)
                                if(tagsReturn.isEmpty()){
                                    tagsAdded.setHint("No tags");
                                    Toast.makeText(BrowsingActivity.this, "Insert one tag at least", Toast.LENGTH_SHORT).show();
                                }else {
                                    //altrimenti imposta la stringa nella textView come ci è stata restituita
                                    tagsAdded.setText(tagsReturn);
                                    //ora prende il testo di tagsAdded e riempie array tagsSplitted con la funzione splitTags
                                    ArrayList<String> tagsSplitted = splitTags(tagsAdded.getText().toString());
                                    tagsSplitted.remove(0);
                                    //filtra recyclerView con tags e fascia prezzo
                                    filterDatabasebyTagsAndPrice(tagsSplitted, flagPrice);

                                }
                            }
                        }).create().show();
            }
        });
    }
    //v
    //se l'utente torna all'activity precedente c'è l'alertDialog se si desidera logOut
    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(this)
                .setTitle("Exit?")
                .setMessage("This will cause logout")
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    //se vuole logOut chiama istanza di firebase con funzione di signOut
                    //l'activity corrente viene distrutta con la funzione finish che chiama la onDestroy
                    public void onClick(DialogInterface di, int i) {
                        lr.remove();
                        FirebaseAuth.getInstance().signOut();
                        finish();
                    }
                }).create().show();
    }
    //v
    private ListenerRegistration eventChangeListener()
    {
        //preleva annunci dal db firestore
        Query query = db.collection("annunci");
                ListenerRegistration listnerRegistration = query
                .addSnapshotListener(new EventListener<QuerySnapshot>(){
                    @Override
                    public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                        //se errore con firestore
                        if(error != null)
                        {
                            Log.e("Firestore error", error.getMessage());
                        }
                        //se non ci sono errori cicla tutti i documenti firestore
                        //trsforma documenti in oggetti annuncio
                        //aggiunge il documento diventato oggetto tipo annuncio all'arrayList
                        //notifica al myAdapter il cambio di dataset (è stato aggiunto un annuncio o rimosso)
                        for(DocumentChange dc : value.getDocumentChanges())
                        {
                            switch (dc.getType())
                            {
                                case ADDED:
                                    Annuncio annuncioAdd = dc.getDocument().toObject(Annuncio.class);
                                    annuncioArrayList.add(annuncioAdd);
                                    myAdapter.notifyDataSetChanged();
                                case REMOVED:
                                    Annuncio annuncioRem = dc.getDocument().toObject(Annuncio.class);
                                    annuncioArrayList.remove(annuncioRem);
                                    myAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
                return listnerRegistration;
    }
    //v
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //inserisce il menù nell'activity corrente
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    //v
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        //per ogni caso del menù facciamo:
        switch (item.getItemId()) {
            //passiamo da browsing a newAnnuncement activity per creare nuovo annuncio
            case R.id.add_annuncio:
                Intent i = new Intent(BrowsingActivity.this, NewAnnouncementActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return true;
                //passiamo da browsing a myOrders activity per visualizzare gli acquisti fatti
            case R.id.orders:
                Intent i1 = new Intent(BrowsingActivity.this, MyOrdersActivity.class);
                i1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i1);
                finish();
                return true;
                //filtra il database e recyclerView per visualizzare gli annunci inseriti dall'utente in sessione
            case R.id.my_announcement:
                filterDatabasebyMyAnn(user.getUid());
                return true;
                //da menù innestato filter by price
            case R.id.filter:
                return true;
                //filtro da databae a recyclerView per visualizzare annunci
                // con tag correnti e fascia di prezzo corrente
            case R.id.price_all:
                //imposto la scelta nella fascia di prezzo (all prices)
                flagPrice = "all_prices";
                //diamo alla stringa c5 i tag prelevati dalla textview
                String c5 = tagsAdded.getText().toString();
                //se non ci sono tag creo arrayList conntenente una sola stringa (empty)
                if(c5.isEmpty()) {
                    c5 = "empty";
                    ArrayList<String> a5 = new ArrayList<>();
                    a5.add(c5);
                    //filtro database e recyclerView per tag e price
                    filterDatabasebyTagsAndPrice(a5, flagPrice);
                    //altrimenti splitto i tags e filtro database per tag e price
                }else{
                    ArrayList<String> tagsSplitted = splitTags(tagsAdded.getText().toString());
                    tagsSplitted.remove(0);
                    filterDatabasebyTagsAndPrice(tagsSplitted, flagPrice);
                }
                return true;
            case R.id.price_20:
                // come per all price ma flag price=20
                flagPrice = "20";
                String c1 = tagsAdded.getText().toString();
                if(c1.isEmpty()) {
                    c1 = "empty";
                    ArrayList<String> a1 = new ArrayList<>();
                    a1.add(c1);
                    filterDatabasebyTagsAndPrice(a1, flagPrice);
                }else{
                    ArrayList<String> tagsSplitted = splitTags(tagsAdded.getText().toString());
                    tagsSplitted.remove(0);
                    filterDatabasebyTagsAndPrice(tagsSplitted, flagPrice);
                }
                return true;
            // come per all price ma flag price=50
            case R.id.price_50:
                flagPrice = "50";
                String c2 = tagsAdded.getText().toString();
                if(c2.isEmpty()) {
                    c2 = "empty";
                    ArrayList<String> a2 = new ArrayList<>();
                    a2.add(c2);
                    filterDatabasebyTagsAndPrice(a2, flagPrice);
                }else{
                    ArrayList<String> tagsSplitted = splitTags(tagsAdded.getText().toString());
                    tagsSplitted.remove(0);
                    filterDatabasebyTagsAndPrice(tagsSplitted, flagPrice);
                }
                return true;
            // come per all price ma flag price=100
            case R.id.price_100:
                flagPrice = "100";
                String c3 = tagsAdded.getText().toString();
                if(c3.isEmpty()) {
                    c3 = "empty";
                    ArrayList<String> a3 = new ArrayList<>();
                    a3.add(c3);
                    filterDatabasebyTagsAndPrice(a3, flagPrice);
                }else{
                    ArrayList<String> tagsSplitted = splitTags(tagsAdded.getText().toString());
                    tagsSplitted.remove(0);
                    filterDatabasebyTagsAndPrice(tagsSplitted, flagPrice);
                }
                return true;
            // come per all price ma flag price=200
            case R.id.price_200:
                flagPrice = "200";
                String c4 = tagsAdded.getText().toString();
                if(c4.isEmpty()) {
                    c4 = "empty";
                    ArrayList<String> a4 = new ArrayList<>();
                    a4.add(c4);
                    filterDatabasebyTagsAndPrice(a4, flagPrice);
                }else{
                    ArrayList<String> tagsSplitted = splitTags(tagsAdded.getText().toString());
                    tagsSplitted.remove(0);
                    filterDatabasebyTagsAndPrice(tagsSplitted, flagPrice);
                }
                return true;
                //reset tags e fascia prezzo (no tags, all prices)
            case R.id.filter_restore:
                flagPrice = "all_prices";
                restoreFilter();
                return true;
                //se faccio logOut torno alla login activity
            case R.id.log_out:
                lr.remove();
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;

            default:
                return false;

        }
    }
    //v
    public void filterDatabasebyTagsAndPrice(ArrayList<String> tags, String price)
    {
        //passo in ingresso i tags e la fascia di prezzo che desidero filtrare
        annuncioArrayList.clear();
        //pulisco l'arrayList
        //Filtro prima per tags
        //se la prima posizione dell'arrayList non è empty
        if(!tags.get(0).equals("empty"))
        {
            //chiedo al database tutti i documenti che contengono qualsiasi di questi tags
            db.collection("annunci").whereArrayContainsAny("tags", tags)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            //se non ci sono annunci attiva il toast
                            if(task.getResult().isEmpty())
                            {
                                Toast.makeText(BrowsingActivity.this, "There isn't announcement with these tags", Toast.LENGTH_SHORT).show();
                                myAdapter.notifyDataSetChanged();
                                return;
                            }
                            if (task.isSuccessful()) {
                                // se ha successo trasformiamo i documenti in oggetti annuncio
                                Integer counter = 0;
                                //uso un contatore per contare gli oggetti annuncio che hanno sia i tags
                                // che la fascia di prezzo concorde a quella che si vuole filtrare
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    Annuncio annuncioFiltered = document.toObject(Annuncio.class);
                                    //dopo aver trasformato l'oggetto in annuncio con i tags corrispondenti
                                    //filtro quest'ultimo per la fascia di prezzo desiderata
                                    String check = filterPrice(price, annuncioFiltered);
                                    if(check.equals(price)){
                                        //se c'è riscontro incremento il counter
                                        //aggiungo l'annuncio all'arrayList
                                        //notifico il cambiamento a myAdapter
                                        counter = counter + 1;
                                        annuncioArrayList.add(annuncioFiltered);
                                        myAdapter.notifyDataSetChanged();
                                    }

                                }
                                if(counter==0)
                                {
                                    Toast.makeText(BrowsingActivity.this, "No announcement with these tags", Toast.LENGTH_SHORT).show();
                                    annuncioArrayList.clear();
                                    myAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                                return;
                            }
                        }
                    });
        }else {
            //altrimenti filtra gli annunci tutti per la fascia di prezzo desiderata
            db.collection("annunci")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Integer i = 0;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    Annuncio annuncioNotFiltered = document.toObject(Annuncio.class);
                                    //Filtro per prezzo
                                    String check = filterPrice(price, annuncioNotFiltered);
                                    if(check.equals(price)){
                                        i = i + 1;
                                        annuncioArrayList.add(annuncioNotFiltered);
                                        myAdapter.notifyDataSetChanged();
                                    }

                                }
                                if(i==0){
                                    Toast.makeText(BrowsingActivity.this, "There aren't announcement in this price range", Toast.LENGTH_SHORT).show();
                                    annuncioArrayList.clear();
                                    myAdapter.notifyDataSetChanged();
                                }

                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }
    //v
    public void restoreFilter()
    {
        //imposto textView tagsAdded con setHint svuotandola dal testo corrente
        tagsAdded.setText("");
        tagsAdded.setHint("No tags");
        //pulisco annuncioArrayList
        annuncioArrayList.clear();
        //richiamo eventChangeListner per riavere tutti i documenti non filtrati
        lr = eventChangeListener();

    }
    //v
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
    //v
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
    //v
    public String filterPrice(@NonNull String price, Annuncio annuncio)
    {
        //se il prezzo in ingresso=fascia di prezzo da filtrare
        if(price.equals("20"))
        {
            //preparo una stringa da convertire a double
            //assegno alla stringa il prezzo dell'annuncio formato stringa
            String price_to_parse = annuncio.getPrice();
            //preparo un nr double e gli assegno la stringa convertita in double
            Double price_parsed = Double.parseDouble(price_to_parse);
            //se minore restituisco 20 come stringa
            if(price_parsed<20)
                return "20";
            //altrimenti verifico sulle altre fasce di prezzo
        }else if(price.equals("50"))
        {
            String price_to_parse = annuncio.getPrice();
            Double price_parsed = Double.parseDouble(price_to_parse);
            //se compreso tra 20 e 50 restituisco 50 come stringa
            if(price_parsed>=20 && price_parsed<50)
                return "50";
        }else if(price.equals("100"))
        {
            String price_to_parse = annuncio.getPrice();
            Double price_parsed = Double.parseDouble(price_to_parse);
            //se compreso tra 50 e 100 restituisco 100 come stringa
            if(price_parsed>=50 && price_parsed<100)
                return "100";
        }else if(price.equals("200"))
        {
            String price_to_parse = annuncio.getPrice();
            Double price_parsed = Double.parseDouble(price_to_parse);
            //se maggiore di 100 restituisco 200 come stringa
            if(price_parsed>=100)
                return "200";
        }else
            //altrimenti restituisco all price
            return "all_prices";
        //altrimenti striga vuota
        return "";

    }
    //v
    public void filterDatabasebyMyAnn(String myId)
    {
        //pulisco annuncio arrayList
        annuncioArrayList.clear();
        //filtro il database firestore nella collezione annunci tramite id autore
        //sto chiedendo da firestore tutti gli annunci effettuati dall'utente in sessione
        db.collection("annunci")
                .whereEqualTo("author", myId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //se non produce alcun documento l'utente non ha caricato annunci o ha venduto tutto
                        if(task.getResult().isEmpty())
                        {
                            Toast.makeText(BrowsingActivity.this, "You haven't added an announcement yet", Toast.LENGTH_SHORT).show();
                            myAdapter.notifyDataSetChanged();
                            return;
                        }
                        //se ha successo la query trasformiamo gli oggetti ricevuti in annunci
                        //li aggiungiamo all'annuncioArrayList
                        //notifichiamo il cambiamento all'adapter
                        if(task.isSuccessful())
                        {
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult())
                            {
                                Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());
                                Annuncio myAnnouncement = documentSnapshot.toObject(Annuncio.class);
                                annuncioArrayList.add(myAnnouncement);
                                myAdapter.notifyDataSetChanged();
                            }
                        }else{
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}
