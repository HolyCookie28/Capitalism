package com.ohad_d.tashtit.ACTIVITIES;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ohad_d.model.Board;
import com.ohad_d.model.Boards;
import com.ohad_d.model.Game;
import com.ohad_d.tashtit.R;
import com.ohad_d.viewmodel.BoardViewModel;
import com.ohad_d.viewmodel.GameViewModel;

public class MenuActivity extends AppCompatActivity {

    Button singleplayer, multiplayer, square;
    private BoardViewModel viewModel;
    private GameViewModel gameViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setViewModel();
        setListeners();

    }

    private void setViewModel() {
        viewModel = new ViewModelProvider(this).get(BoardViewModel.class);
        gameViewModel = new ViewModelProvider(this).get(GameViewModel.class);

    }

    private void setListeners() {
        square.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, square_creation.class);
                startActivity(intent);
            }
        });
        singleplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, GameboardActivity.class);
                Board board = new Board(32);
                viewModel.add(board);
                //Game game = new Game(viewModel.getEntity().getValue().getIdFs());
                //gameViewModel.add(game);
                startActivity(intent);
            }
        });
    }

    private void initializeViews() {
        multiplayer = findViewById(R.id.playWithFriendsButton);
        singleplayer = findViewById(R.id.playAloneButton);
        square = findViewById(R.id.createSquareButton);
    }


}