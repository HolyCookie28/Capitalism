package com.ohad_d.model;

import com.ohad_d.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class Board extends BaseEntity implements Serializable {
    protected int tileNum;
    protected Tile tile[];  // ??????????

    public Board(){}

    public Board(int tileNum) {
        this.tileNum = tileNum;
    }

    public Board(int tileNum, Tile[] tile) {
        this.tileNum = tileNum;
        this.tile = tile;
    }

    public int getTileNum() {
        return tileNum;
    }

    public void setTileNum(int tileNum) {
        this.tileNum = tileNum;
    }

    public Tile[] getCell() {
        return tile;
    }

    public void setCell(Tile[] tile) {
        this.tile = tile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Board)) return false;
        if (!super.equals(o)) return false;
        Board board = (Board) o;
        return tileNum == board.tileNum && Objects.deepEquals(tile, board.tile);
    }
}
