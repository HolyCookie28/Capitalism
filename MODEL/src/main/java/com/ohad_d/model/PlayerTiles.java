package com.ohad_d.model;

import com.ohad_d.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class PlayerTiles extends BaseEntity implements Serializable {
    protected String playerID;
    protected String CellID;

    public PlayerTiles() {}

    public PlayerTiles(String playerID, String cellID) {
        this.playerID = playerID;
        CellID = cellID;
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public String getCellID() {
        return CellID;
    }

    public void setCellID(String cellID) {
        CellID = cellID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerTiles)) return false;
        if (!super.equals(o)) return false;
        PlayerTiles that = (PlayerTiles) o;
        return Objects.equals(playerID, that.playerID) && Objects.equals(CellID, that.CellID);
    }
}
