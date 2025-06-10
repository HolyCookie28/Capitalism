package com.ohad_d.model;

import com.ohad_d.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class Tile extends BaseEntity implements Serializable {
    protected Type type;
    private String boardId;

    public Tile() {}

    public Tile(Type type, String boardId) {
        this.type = type;
        this.boardId = boardId;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile)) return false;
        if (!super.equals(o)) return false;
        Tile tile = (Tile) o;
        return type == tile.type && Objects.equals(boardId, tile.boardId);
    }
}
