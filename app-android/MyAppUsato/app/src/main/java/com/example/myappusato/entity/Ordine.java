package com.example.myappusato.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

public class Ordine implements Serializable {

    public String idSeller;
    public String idBuyer;
    public String idAnnouncement;
    public String pricePaid;
    public String idImage;

    public Ordine(){
    }

    public Ordine(String idAnnouncement, String idBuyer, String idImage, String idSeller, String pricePaid){
        this.idAnnouncement = idAnnouncement;
        this.idBuyer = idBuyer;
        this.idImage = idImage;
        this.idSeller = idSeller;
        this.pricePaid = pricePaid;

    }


    public String getIdSeller() {
        return idSeller;
    }

    public void setIdSeller(String idSeller) {
        this.idSeller = idSeller;
    }

    public String getIdPurhcaser() {
        return idBuyer;
    }

    public void setIdPurhcaser(String idPurhcaser) {
        this.idBuyer = idPurhcaser;
    }

    public String getIdAnnuncio() {
        return idAnnouncement;
    }

    public void setIdAnnuncio(String idAnnuncio) {
        this.idAnnouncement = idAnnuncio;
    }

    public String getPricePaid() {
        return pricePaid;
    }

    public void setPricePaid(String pricePaid) {
        this.pricePaid = pricePaid;
    }

    public String getIdImage() {
        return idImage;
    }

    public void setIdImage(String idImage) {
        this.idImage = idImage;
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
