package com.example.myappusato.model;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.myappusato.R;
import com.example.myappusato.entity.Ordine;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyOrdersActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private DocumentReference docRef;
    private ArrayList<Ordine> ordineArrayList;
    private RecyclerView recyclerView;
    private MyOrderAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        ordineArrayList = new ArrayList<Ordine>();

        recyclerView = findViewById(R.id.recycler_view_orders);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        myAdapter = new MyOrderAdapter(ordineArrayList, MyOrdersActivity.this);
        recyclerView.setAdapter(myAdapter);

        eventChangeListener();
    }

    //filtra gli ordini per Uid su firestore nella collezione orders
    private void eventChangeListener()
    {
        db.collection("orders")
                .whereEqualTo("idBuyer", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().isEmpty())
                        {
                            Toast.makeText(MyOrdersActivity.this, "There isn't orders", Toast.LENGTH_SHORT).show();
                            myAdapter.notifyDataSetChanged();
                            return;
                        }
                        if (task.isSuccessful()) {
                            //se ha successo cicla tutti i documenti firestore filtrati per Uid
                            //trsforma ogni document in oggetti ordine
                            //aggiunge il documento diventato oggetto tipo ordine all'arrayList
                            //notifica al myAdapter il cambio di dataset
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Ordine ordineAdd = document.toObject(Ordine.class);
                                ordineArrayList.add(ordineAdd);
                                myAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }

                    }
                });
    }

    @Override
    public void onBackPressed() {
        //ritorna a browsing activity
        Intent i = new Intent(MyOrdersActivity.this, BrowsingActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

}