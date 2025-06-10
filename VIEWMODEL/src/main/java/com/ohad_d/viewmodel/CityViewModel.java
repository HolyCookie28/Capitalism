package com.ohad_d.viewmodel;

import android.app.Application;

import com.ohad_d.model.Cities;
import com.ohad_d.model.City;
import com.ohad_d.repository.BASE.BaseRepository;
import com.ohad_d.repository.CityRepository;
import com.ohad_d.viewmodel.BASE.BaseViewModel;

public class CityViewModel extends BaseViewModel<City, Cities> {
    private CityRepository cityRepository;

    public CityViewModel(Application application) {
        super(City.class, Cities.class, application);
    }

    @Override
    protected BaseRepository<City, Cities> createRepository(Application application) {
        cityRepository = new CityRepository(application);
        return cityRepository;
    }
}
