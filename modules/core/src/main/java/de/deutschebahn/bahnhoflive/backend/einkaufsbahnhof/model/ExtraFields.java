package de.deutschebahn.bahnhoflive.backend.einkaufsbahnhof.model;

import java.util.List;

public class ExtraFields {
    public final List<String> paymentTypes;

    public final String location;

    public final String phone;

    public final String email;

    public final String web;

    public ExtraFields(List<String> paymentTypes, String location, String phone, String email, String web) {
        this.paymentTypes = paymentTypes;
        this.location = location;
        this.phone = phone;
        this.email = email;
        this.web = web;
    }
}
