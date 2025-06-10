package com.ohad_d.viewmodel;

import android.app.Application;

import com.ohad_d.model.Board;
import com.ohad_d.model.Boards;
import com.ohad_d.model.User;
import com.ohad_d.model.Users;
import com.ohad_d.repository.BASE.BaseRepository;
import com.ohad_d.repository.BoardRepository;
import com.ohad_d.viewmodel.BASE.BaseViewModel;

public class BoardViewModel extends BaseViewModel<Board, Boards> {
    private BoardRepository repository;

    public BoardViewModel(Application application) {
        super(Board.class, Boards.class, application);
    }

    @Override
    protected BaseRepository<Board, Boards> createRepository(Application application) {
        repository = new BoardRepository(application);
        return repository;
    }
}
