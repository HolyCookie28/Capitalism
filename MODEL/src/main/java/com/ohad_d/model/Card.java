package com.ohad_d.model;

import com.ohad_d.model.BASE.BaseEntity;

import java.io.Serializable;
import java.util.Objects;

public class Card extends BaseEntity implements Serializable {
    protected String name;
    protected Type type;

    public Card() {}

    public Card(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        if (!(o instanceof Card)) return false;
        if (!super.equals(o)) return false;
        Card card = (Card) o;
        return Objects.equals(name, card.name) && type == card.type;
    }
}
