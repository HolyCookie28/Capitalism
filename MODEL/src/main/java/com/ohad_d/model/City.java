package com.ohad_d.model;

import android.graphics.Bitmap;

import com.ohad_d.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class City extends BaseEntity implements Serializable {
    protected String name;
    protected int cost;
    protected int tax;
    protected boolean house;
    protected int house_cost;
    protected int house_tax;
    protected boolean hotel;
    protected int hotel_cost;
    protected int hotel_tax;
    protected String picture;
    protected String color;

    public City() {
    }

    public City(String name, int cost, int tax, boolean house, int house_cost, int house_tax, boolean hotel, int hotel_tax, int hotel_cost) {
        this.name = name;
        this.cost = cost;
        this.tax = tax;
        this.house = house;
        this.house_cost = house_cost;
        this.house_tax = house_tax;
        this.hotel = hotel;
        this.hotel_tax = hotel_tax;
        this.hotel_cost = hotel_cost;
    }

    public City(String name, int cost, int tax, boolean house, int house_cost, int house_tax, boolean hotel, int hotel_cost, int hotel_tax, String picture, String color) {
        this.name = name;
        this.cost = cost;
        this.tax = tax;
        this.house = house;
        this.house_cost = house_cost;
        this.house_tax = house_tax;
        this.hotel = hotel;
        this.hotel_cost = hotel_cost;
        this.hotel_tax = hotel_tax;
        this.picture = picture;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getTax() {
        return tax;
    }

    public void setTax(int tax) {
        this.tax = tax;
    }

    public boolean isHouse() {
        return house;
    }

    public void setHouse(boolean house) {
        this.house = house;
    }

    public int getHouse_cost() {
        return house_cost;
    }

    public void setHouse_cost(int house_cost) {
        this.house_cost = house_cost;
    }

    public int getHouse_tax() {
        return house_tax;
    }

    public void setHouse_tax(int house_tax) {
        this.house_tax = house_tax;
    }

    public boolean isHotel() {
        return hotel;
    }

    public void setHotel(boolean hotel) {
        this.hotel = hotel;
    }

    public int getHotel_cost() {
        return hotel_cost;
    }

    public void setHotel_cost(int hotel_cost) {
        this.hotel_cost = hotel_cost;
    }

    public int getHotel_tax() {
        return hotel_tax;
    }

    public void setHotel_tax(int hotel_tax) {
        this.hotel_tax = hotel_tax;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof City)) return false;
        if (!super.equals(o)) return false;
        City city = (City) o;
        return cost == city.cost && tax == city.tax && house == city.house && house_cost == city.house_cost && house_tax == city.house_tax && hotel == city.hotel && hotel_cost == city.hotel_cost && hotel_tax == city.hotel_tax && Objects.equals(name, city.name) && Objects.equals(picture, city.picture) && Objects.equals(color, city.color);
    }
}
