package com.example.myappusato.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public class Annuncio implements Serializable {
    //autore (user id o e-mail)
    //il titolo dell'oggetto, una foto (obbligatoria) e una descrizione (facoltativa)
    //uno o più tag (almeno uno è obbligatorio)
    //il prezzo (obbligatorio)

    public String author;
    public String displayName;
    public String title;
    public String image;
    public String description;
    public ArrayList<String> tags;
    public String price;

    public Annuncio(){
    }

    public Annuncio(String author, String description, String displayName, String image, String price, ArrayList<String> tags, String title){
        this.author = author;
        this.title = title;
        this.image = image;
        this.description = description;
        this.tags = tags;
        this.price = price;
        this.displayName = displayName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }


}


