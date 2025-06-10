package com.ohad_d.repository;

import android.app.Application;

import com.google.firebase.firestore.Query;
import com.ohad_d.model.Cities;
import com.ohad_d.model.City;
import com.ohad_d.repository.BASE.BaseRepository;

public class CityRepository extends BaseRepository<City, Cities> {
    public CityRepository(Application application) {
        super(City.class, Cities.class, application);
    }


    @Override
    protected Query getQueryForExist(City entity) {
        return getCollection().whereEqualTo("name", entity.getName());
    }
}
