package com.example.ui.Model;

import java.util.ArrayList;

public class AreaModel {
    private String id;

    private String name;
    private ArrayList<String> localAreaIds;
    private ArrayList<String> description;

    public AreaModel(String id, String name, ArrayList<String> localAreaIds, ArrayList<String> description) {
        this.id = id;
        this.name = name;
        this.localAreaIds = localAreaIds;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getLocalAreaIds() {
        return localAreaIds;
    }

    public void setLocalAreaIds(ArrayList<String> localAreaIds) {
        this.localAreaIds = localAreaIds;
    }

    public ArrayList<String> getDescription() {
        return description;
    }

    public void setDescription(ArrayList<String> description) {
        this.description = description;
    }
}