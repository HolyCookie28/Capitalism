package com.ohad_d.model;

import java.util.Objects;

public class EffectCard extends Card{
    protected String effect;

    public EffectCard(){}

    public EffectCard(String effect) {
        this.effect = effect;
    }

    public EffectCard(String name, Type type, String effect) {
        super(name, type);
        this.effect = effect;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EffectCard)) return false;
        if (!super.equals(o)) return false;
        EffectCard that = (EffectCard) o;
        return Objects.equals(effect, that.effect);
    }
}
