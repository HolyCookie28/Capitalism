package com.ohad_d.model;

import java.io.Serializable;
import java.util.Objects;

public class TaxCard extends Card implements Serializable {
    protected int value;

    public TaxCard(){}

    public TaxCard(int value) {
        this.value = value;
    }

    public TaxCard(String name, Type type, int value) {
        super(name, type);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaxCard)) return false;
        if (!super.equals(o)) return false;
        TaxCard taxCard = (TaxCard) o;
        return value == taxCard.value;
    }
}
