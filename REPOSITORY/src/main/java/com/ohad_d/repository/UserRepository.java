package com.ohad_d.repository;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.ohad_d.model.User;
import com.ohad_d.model.Users;
import com.ohad_d.repository.BASE.BaseRepository;

public class UserRepository extends BaseRepository<User, Users> {

    public UserRepository(Application application) {
        super(User.class, Users.class, application);
    }

    @Override
    protected Query getQueryForExist(User entity) {
        return getCollection().whereEqualTo("name", entity.getName());
    }

    public Task<QuerySnapshot> loginUser(String name, String password) {
        return getCollection().whereEqualTo("name", name).whereEqualTo("password", password).get();
    }
}
