package com.ohad_d.viewmodel;

import android.app.Application;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;
import com.ohad_d.model.User;
import com.ohad_d.model.Users;
import com.ohad_d.repository.BASE.BaseRepository;
import com.ohad_d.repository.UserRepository;
import com.ohad_d.viewmodel.BASE.BaseViewModel;

public class UserViewModel extends BaseViewModel<User, Users> {
    private  UserRepository repository;

    public UserViewModel(Application application) {
        super(User.class, Users.class, application);
    }

    @Override
    protected BaseRepository<User, Users> createRepository(Application application) {
        repository = new UserRepository(application);
        return repository;
    }

    public Task<QuerySnapshot> loginUser(String name, String password){

        return repository.loginUser(name, password);
    }
}
