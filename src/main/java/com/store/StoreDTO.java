package com.store;

public class StoreDTO {
private String name;
private String address;
private double rating;
private String category;
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
@Override
public String toString() {
	return "StoreDTO [name=" + name + ", address=" + address + ", review=" + ", rating=" + rating + ", category="
			+ category + "]";
}



}
