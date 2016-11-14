package de.kjaduesseldorf.sportsunite.app.model;

import java.util.Arrays;
import java.util.List;

public class District {

    private long id;
    private String name;
    private String subDistricts;

    public District() {

    }

    public District(long id, String name, String subDistricts) {
        this.id = id;
        this.name = name;
        this.subDistricts = subDistricts;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSubDistricts() {
        return subDistricts;
    }

    public List<String> getSubDistrictsList() {
        return Arrays.asList(subDistricts.split(","));
    }
}
