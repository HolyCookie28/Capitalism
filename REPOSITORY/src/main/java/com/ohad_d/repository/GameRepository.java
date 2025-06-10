package com.ohad_d.repository;

import android.app.Application;
import android.content.Context;

import com.google.firebase.firestore.Query;
import com.ohad_d.model.Cities;
import com.ohad_d.model.City;
import com.ohad_d.model.Game;
import com.ohad_d.model.Games;
import com.ohad_d.repository.BASE.BaseRepository;

public class GameRepository extends BaseRepository<Game, Games> {
    public GameRepository(Application application) {
        super(Game.class, Games.class, application);
    }

    @Override
    protected Query getQueryForExist(Game entity) {
        return getCollection().whereEqualTo("boardID", entity.getBoardId());
    }
}
