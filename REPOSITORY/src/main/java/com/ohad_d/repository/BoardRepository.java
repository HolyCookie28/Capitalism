package com.ohad_d.repository;

import android.app.Application;

import com.google.firebase.firestore.Query;
import com.ohad_d.model.Board;
import com.ohad_d.model.Boards;
import com.ohad_d.repository.BASE.BaseRepository;

public class BoardRepository extends BaseRepository<Board, Boards> {

    public BoardRepository(Application application) {
        super(Board.class, Boards.class, application);
    }

    @Override
    protected Query getQueryForExist(Board entity) {
        return getCollection().whereEqualTo("idFs", entity.getIdFs());
    }
}
