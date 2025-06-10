package com.ohad_d.viewmodel;

import android.app.Application;

import com.ohad_d.model.Game;
import com.ohad_d.model.Games;
import com.ohad_d.model.User;
import com.ohad_d.model.Users;
import com.ohad_d.repository.BASE.BaseRepository;
import com.ohad_d.repository.GameRepository;
import com.ohad_d.viewmodel.BASE.BaseViewModel;

public class GameViewModel extends BaseViewModel<Game, Games> {
    private GameRepository repository;

    public GameViewModel(Application application) {
        super(Game.class, Games.class, application);
    }

    @Override
    protected BaseRepository<Game, Games> createRepository(Application application) {
        repository = new GameRepository(application);
        return repository;
    }
}
