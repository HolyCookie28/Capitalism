package com.ohad_d.model;

import com.ohad_d.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Game extends BaseEntity implements Serializable {
    protected String boardId;
    protected TaxCard taxCards;
    protected EffectCard effectCards;
    protected Player currentPlayer;

    public Game() {}

    public Game(Player currentPlayer, String boardId) {
        this.currentPlayer = currentPlayer;
        this.boardId = boardId;
    }

    public Game(String boardId) {
        this.boardId = boardId;
    }

    public Game(String board, TaxCard taxCards, EffectCard effectCards, Player currentPlayer) {
        this.boardId = board;
        this.taxCards = taxCards;
        this.effectCards = effectCards;
        this.currentPlayer = currentPlayer;
    }

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    public TaxCard getTaxCards() {
        return taxCards;
    }

    public void setTaxCards(TaxCard taxCards) {
        this.taxCards = taxCards;
    }

    public EffectCard getEffectCards() {
        return effectCards;
    }

    public void setEffectCards(EffectCard effectCards) {
        this.effectCards = effectCards;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Game)) return false;
        if (!super.equals(o)) return false;
        Game game = (Game) o;
        return Objects.equals(boardId, game.boardId) && Objects.deepEquals(taxCards, game.taxCards) && Objects.deepEquals(effectCards, game.effectCards) && Objects.equals(currentPlayer, game.currentPlayer);
    }
}
