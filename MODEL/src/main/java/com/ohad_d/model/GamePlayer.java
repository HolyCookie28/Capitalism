package com.ohad_d.model;

import com.ohad_d.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class GamePlayer extends BaseEntity implements Serializable {
    protected String playerID;
    protected String gameID;

    public GamePlayer() {
    }

    public GamePlayer(String playerID, String gameID) {
        this.playerID = playerID;
        this.gameID = gameID;
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GamePlayer)) return false;
        if (!super.equals(o)) return false;
        GamePlayer that = (GamePlayer) o;
        return Objects.equals(playerID, that.playerID) && Objects.equals(gameID, that.gameID);
    }
}
