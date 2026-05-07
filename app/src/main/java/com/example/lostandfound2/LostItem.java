package com.example.lostandfound2;

public class LostItem {
    private long id;
    private String postType;
    private String name;
    private String phone;
    private String description;
    private String category;
    private String location;
    private String imagePath;
    private long postedAtMillis;

    public LostItem(long id, String postType, String name, String phone, String description,
                    String category, String location, String imagePath, long postedAtMillis) {
        this.id = id;
        this.postType = postType;
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.category = category;
        this.location = location;
        this.imagePath = imagePath;
        this.postedAtMillis = postedAtMillis;
    }

    public long getId() {
        return id;
    }

    public String getPostType() {
        return postType;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getLocation() {
        return location;
    }

    public String getImagePath() {
        return imagePath;
    }

    public long getPostedAtMillis() {
        return postedAtMillis;
    }
}

