package com.ohad_d.repository;

import com.google.firebase.firestore.Query;

import com.ohad_d.model.BASE.BaseEntity;
import com.ohad_d.repository.BASE.BaseRepository;

public class Xrepository extends BaseRepository {
    @Override
    protected Query getQueryForExist(BaseEntity entity) {
        return null;
    }
}
