package com.ohad_d.viewmodel;

import android.app.Application;

import com.ohad_d.repository.BASE.BaseRepository;
import com.ohad_d.viewmodel.BASE.BaseViewModel;

public class XviewModel extends BaseViewModel {
    @Override
    protected BaseRepository createRepository(Application application) {
        return null;
    }

    public XviewModel(Class tEntity, Class tCollection, Application application) {
        super(tEntity, tCollection, application);
    }
}
