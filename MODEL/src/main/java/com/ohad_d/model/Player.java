package com.ohad_d.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Objects;

public class Player extends User implements Serializable {
    protected int cash;
    protected int cells;

    public Player() {}

    public Player(int cash, int cells) {
        this.cash = cash;
        this.cells = cells;
    }

    public Player(String name, String picture, String email, String password, int cash, int cells) {
        super(name, picture, email, password);
        this.cash = cash;
        this.cells = cells;
    }

    public Player(String name, String picture, int cash, int cells){
        super(name, picture);
        this.cash = cash;
        this.cells = cells;
    }

    public int getCash() {
        return cash;
    }

    public void setCash(int cash) {
        this.cash = cash;
    }

    public void addCash(int cash) {
        this.cash = this.cash + cash;
    }

    public void delCash(int cash) {
        this.cash = this.cash - cash;
    }

    public int getCells() {
        return cells;
    }

    public void setCells(int cells) {
        this.cells = cells;
    }

    public void addCells(int cells) {
        this.cells = this.cells + cells;
    }

    public void delCells(int cells) {
        this.cells = this.cells - cells;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        if (!super.equals(o)) return false;
        Player player = (Player) o;
        return cash == player.cash && cells == player.cells;
    }
}
