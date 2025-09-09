package com.store;

/**
 * Data Transfer Object for store information.
 * Includes latitude and longitude for map markers.
 */
public class StoreDTO {
    private String name;
    private String address;
    private double rating;
    private String category;
    private double latitude;
    private double longitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "StoreDTO [name=" + name + ", address=" + address + ", rating=" + rating
                + ", category=" + category + ", latitude=" + latitude + ", longitude=" + longitude + "]";
    }
}
