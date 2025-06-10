package com.ohad_d.tashtit.ACTIVITIES;

import static com.ohad_d.helper.BitMapHelper.decodeBase64;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.ohad_d.helper.BitMapHelper;
import com.ohad_d.model.City;
import com.ohad_d.model.Game;
import com.ohad_d.model.Player;
import com.ohad_d.model.PlayerTiles;
import com.ohad_d.tashtit.R;
import com.ohad_d.viewmodel.GameViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameboardActivity extends ComponentActivity {

    private ImageButton dice1, dice2;
    private boolean cdice1 = false, cdice2 = false;
    private int GRID_SIZE = 9; // Total number of cells (9 on each side)
    private int CELL_SIZE_DP = 35;
    private Player player1, player2, player3, player4;
    private int startingCash = 50;
    private Button button1, button2, button3, button4;
    private TextView textView1, textView2, textView3, textView4;
    private GridLayout gridLayout;
    private GameViewModel viewModel;
    private Player currentPlayer;
    private Player[] ownership = new Player[32];
    private City[] cityTiles = new City[32];
    private Player[] players = new Player[4];
    private Player[] jail = new Player[4];
    private int[] jailCount = new int[4];
    private List<DocumentSnapshot> allCities = new ArrayList<>();
    private boolean citiesLoaded = false;
    private int diceValue1 = 0, diceValue2 = 0;
    private Player loggedIn;
    private boolean gameInProgress = false; // Track if a turn is in progress
    private int currentPlayerIndex = 0; // Track whose turn it is (0 = player1, 1-3 = AI players)
    private boolean[] playerBankrupt = new boolean[4]; // Track which players are bankrupt
    private int activePlayers = 4; // Count of active players
    private boolean isAnimating = false; // Prevent interactions during animation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gameboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setViewModel();
        gridLayout = findViewById(R.id.monopolyBoard);

        // Initialize bankruptcy tracking
        for (int i = 0; i < 4; i++) {
            playerBankrupt[i] = false;
        }
        activePlayers = 4;

        loadCitiesFromDatabase();
        initializeViews();
        setlisteners();
        setPlayers();
        updateMoney();
    }

    private void setViewModel() {
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
    }

    // NEW METHOD: Load all cities once from database
    private void loadCitiesFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Cities")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            allCities = task.getResult().getDocuments();
                            citiesLoaded = true;
                            Log.d("Cities", "Loaded " + allCities.size() + " cities from database");

                            // Create grid only after cities are loaded
                            createGrid();

                            // Initialize game after grid is ready
                            updatePlayerPosition();
                            startGame();
                        } else {
                            Log.w("FirestoreError", "Error getting cities.", task.getException());
                            Toast.makeText(GameboardActivity.this, "Failed to load game data", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void startGame() {
        currentPlayerIndex = 0;
        currentPlayer = player1;
        gameInProgress = false;
        enableDiceForPlayer1();
        Log.d("Game", "Game started - Player 1's turn");
    }

    private void handlePlayer1JailTurn() {
        if (jailCount[0] < 3) {
            jailCount[0]++;
            Toast.makeText(this, "You are in jail for " + (3 - jailCount[0]) + " more turns", Toast.LENGTH_SHORT).show();
            Log.d("Jail", "Player1 in jail, count: " + jailCount[0]);
            endPlayer1Turn();
        } else {
            // Release from jail after 3 turns
            jail[0] = null;
            jailCount[0] = 0;
            Toast.makeText(this, "You are released from jail!", Toast.LENGTH_SHORT).show();
            Log.d("Jail", "Player1 released from jail");
            // Now enable dice for normal turn
            dice1.setClickable(true);
            dice2.setClickable(true);
            cdice1 = false;
            cdice2 = false;
        }
    }

    private void enableDiceForPlayer1() {
        if (!gameInProgress && currentPlayerIndex == 0 && !isAnimating) {
            // Check if player1 is in jail - if so, don't enable dice
            if (jail[0] == player1) {
                Log.d("Turn", "Player1 is in jail, dice remain disabled");
                // Process jail turn directly
                handlePlayer1JailTurn();
                return;
            }

            dice1.setClickable(true);
            dice2.setClickable(true);
            cdice1 = false;
            cdice2 = false;
            Log.d("Turn", "Dice enabled for player1");
        }
    }

    private void disableDice() {
        dice1.setClickable(false);
        dice2.setClickable(false);
    }

    private void player1Turn() {
        if (gameInProgress) return; // Prevent multiple turn processing

        Log.d("Turn", "player1Turn called - cdice1: " + cdice1 + ", cdice2: " + cdice2);

        // Both dice must be clicked to process turn
        if (cdice1 && cdice2) {
            gameInProgress = true;
            disableDice();

            Log.d("Turn", "Both dice clicked, processing turn");

            // Show player's dice roll
            int totalRoll = diceValue1 + diceValue2;
            String rollMessage = "You rolled: " + diceValue1 + " + " + diceValue2 + " = " + totalRoll;
            Toast.makeText(this, rollMessage, Toast.LENGTH_LONG).show();

            // Animate player1 movement
            animatePlayer1Movement(totalRoll);
        }
    }

    // Method to animate player1 movement step by step
    private void animatePlayer1Movement(int steps) {
        isAnimating = true;

        // Store original position
        int startPosition = player1.getCells();

        // Animate each step for player1
        animatePlayer1Step(0, steps, startPosition);
    }

    // Recursive method to animate player1 steps
    private void animatePlayer1Step(int currentStep, int totalSteps, int startPosition) {
        if (currentStep >= totalSteps) {
            // Animation complete
            isAnimating = false;

            // Set final position
            player1.setCells((startPosition + totalSteps) % 32);

            // Handle passing GO
            if ((startPosition + totalSteps) > 31) {
                player1.addCash(10);
                updateMoney();
                Toast.makeText(this, "Passed GO! Collected $10", Toast.LENGTH_SHORT).show();
            }

            Log.d("position", "player1Turn final position: " + player1.getCells());
            updatePlayerPosition();
            return;
        }

        // Calculate current position for this step
        int currentPosition = (startPosition + currentStep + 1) % 32;
        player1.setCells(currentPosition);
        updatePlayerPosition();

        // Continue to next step after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                animatePlayer1Step(currentStep + 1, totalSteps, startPosition), 500);
    }

    private void endPlayer1Turn() {
        gameInProgress = false;
        currentPlayerIndex = 1; // Move to next player
        // Start AI players turn cycle
        processAITurn(1);
    }

    private void processAITurn(int playerIndex) {
        if (playerIndex > 3) {
            // All AI players have played, back to player 1
            currentPlayerIndex = 0;
            currentPlayer = player1;

            // Check if player1 is bankrupt
            if (playerBankrupt[0]) {
                // Skip player1 and continue with AI
                processAITurn(1);
                return;
            }

            enableDiceForPlayer1();
            Log.d("Turn", "All AI turns complete, back to player 1");
            return;
        }

        // Skip bankrupt AI players
        if (playerBankrupt[playerIndex]) {
            processAITurn(playerIndex + 1);
            return;
        }

        currentPlayer = players[playerIndex];
        Log.d("Turn", "Processing AI turn for " + currentPlayer.getName());

        // Check if AI player is in jail
        if (jail[playerIndex] == currentPlayer) {
            if (jailCount[playerIndex] < 3) {
                jailCount[playerIndex]++;
                Log.d("Jail", currentPlayer.getName() + " is in jail for " + (3 - jailCount[playerIndex]) + " more turns");
                Toast.makeText(this, currentPlayer.getName() + " is in jail for " + (3 - jailCount[playerIndex]) + " more turns", Toast.LENGTH_SHORT).show();
                // Move to next AI player after a short delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> processAITurn(playerIndex + 1), 2000);
                return;
            } else {
                // Release from jail after 3 turns
                jail[playerIndex] = null;
                jailCount[playerIndex] = 0;
                Log.d("Jail", currentPlayer.getName() + " is released from jail!");
                Toast.makeText(this, currentPlayer.getName() + " is released from jail!", Toast.LENGTH_SHORT).show();
            }
        }

        // AI rolls dice and shows the result
        int dice1Value = (int) (Math.random() * 6) + 1;
        int dice2Value = (int) (Math.random() * 6) + 1;
        int totalRoll = dice1Value + dice2Value;

        // Show dice roll to user
        showAIDiceRoll(currentPlayer.getName(), dice1Value, dice2Value, totalRoll);

        // Animate player movement step by step
        animatePlayerMovement(currentPlayer, playerIndex, totalRoll);
    }

    private void showAIDiceRoll(String playerName, int ddice1, int ddice2, int total) {
        // Update dice images to show what AI rolled
        dice1.setImageResource(getResources().getIdentifier("dice"+ddice1,"drawable", getPackageName()));
        dice1.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        dice2.setImageResource(getResources().getIdentifier("dice"+ddice2,"drawable", getPackageName()));
        dice2.setScaleType(ImageButton.ScaleType.FIT_CENTER);

        // Show toast with dice result
        String rollMessage = playerName + " rolled: " + dice1 + " + " + dice2 + " = " + total;
        Toast.makeText(this, rollMessage, Toast.LENGTH_LONG).show();
        Log.d("DiceRoll", rollMessage);
    }

    // Method to animate player movement step by step
    private void animatePlayerMovement(Player player, int playerIndex, int steps) {
        isAnimating = true;
        disableDice(); // Disable dice during animation

        // Store original position
        int startPosition = player.getCells();

        // Animate each step
        animateStep(player, playerIndex, 0, steps, startPosition);
    }

    // Recursive method to animate each step
    private void animateStep(Player player, int playerIndex, int currentStep, int totalSteps, int startPosition) {
        if (currentStep >= totalSteps) {
            // Animation complete
            isAnimating = false;

            // Handle passing GO
            if (player.getCells() > 31) {
                player.delCells(32);
                player.addCash(10);
                updateMoney();
                Toast.makeText(this, player.getName() + " passed GO and collected $10", Toast.LENGTH_SHORT).show();
                Log.d("AI", player.getName() + " passed GO and collected $10");
            }

            // Update final position
            updatePlayerPosition();

            // Process AI game logic after movement is complete
            handleAIGameLogic(player, playerIndex);

            // Move to next AI player after a longer delay to show the result
            new Handler(Looper.getMainLooper()).postDelayed(() -> processAITurn(playerIndex + 1), 3000);
            return;
        }

        // Move player one step
        player.addCells(1);
        updatePlayerPosition();

        // Continue to next step after a short delay
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                animateStep(player, playerIndex, currentStep + 1, totalSteps, startPosition), 500);
    }

    private void handleAIGameLogic(Player aiPlayer, int playerIndex) {
        int currentPosition = aiPlayer.getCells();
        Log.d("AI", aiPlayer.getName() + " landed on position " + currentPosition);

        // Handle jail logic
        if (currentPosition == 8 || currentPosition == 28) {
            jail[playerIndex] = aiPlayer;
            jailCount[playerIndex] = 0;
            if (currentPosition == 28) {
                aiPlayer.setCells(8);
            }
            Toast.makeText(this, aiPlayer.getName() + " is sent to jail!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Handle casino logic
        if (currentPosition == 16) {
            handleAICasino(aiPlayer);
            return;
        }

        // Skip special tiles
        if (currentPosition == 0 || currentPosition == 8 || currentPosition == 16 || currentPosition == 24) {
            return;
        }

        // Handle property interactions
        if (cityTiles[currentPosition] != null) {
            // Handle tax payment
            if (ownership[currentPosition] != null && ownership[currentPosition] != aiPlayer) {
                int totalTax = cityTiles[currentPosition].getTax();
                if (cityTiles[currentPosition].isHouse()) {
                    totalTax += cityTiles[currentPosition].getHouse_tax();
                }
                if (cityTiles[currentPosition].isHotel()) {
                    totalTax += cityTiles[currentPosition].getHotel_tax();
                }

                // Use the payTax method to handle bankruptcy
                if (payTax(aiPlayer, playerIndex, totalTax)) {
                    ownership[currentPosition].addCash(totalTax);
                    updateMoney();
                    Toast.makeText(this, aiPlayer.getName() + " paid $" + totalTax + " to " +
                            ownership[currentPosition].getName(), Toast.LENGTH_SHORT).show();
                }
                // If player is bankrupt, payTax handles it
            }
            // Handle property purchase
            else if (ownership[currentPosition] == null) {
                // AI decides whether to buy (70% chance if they can afford it)
                if (aiPlayer.getCash() >= cityTiles[currentPosition].getCost() && Math.random() < 0.7) {
                    aiPlayer.delCash(cityTiles[currentPosition].getCost());
                    ownership[currentPosition] = aiPlayer;
                    updateMoney();
                    Toast.makeText(this, aiPlayer.getName() + " bought " +
                            cityTiles[currentPosition].getName() + "!", Toast.LENGTH_SHORT).show();
                    Log.d("AI", aiPlayer.getName() + " bought " + cityTiles[currentPosition].getName());
                }
            }
            // Handle property upgrades (AI owns the property)
            else if (ownership[currentPosition] == aiPlayer) {
                City city = cityTiles[currentPosition];
                // 40% chance to upgrade if they can afford it
                if (Math.random() < 0.4) {
                    if (!city.isHouse() && aiPlayer.getCash() >= city.getHouse_cost()) {
                        city.setHouse(true);
                        aiPlayer.delCash(city.getHouse_cost());
                        updateMoney();
                        Toast.makeText(this, aiPlayer.getName() + " built a house on " +
                                city.getName() + "!", Toast.LENGTH_SHORT).show();
                    } else if (!city.isHotel() && aiPlayer.getCash() >= city.getHotel_cost()) {
                        city.setHotel(true);
                        aiPlayer.delCash(city.getHotel_cost());
                        updateMoney();
                        Toast.makeText(this, aiPlayer.getName() + " built a hotel on " +
                                city.getName() + "!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void updateMoney(){
        button1.setText(Integer.toString(player1.getCash()));
        button2.setText(Integer.toString(player2.getCash()));
        button3.setText(Integer.toString(player3.getCash()));
        button4.setText(Integer.toString(player4.getCash()));
    }

    private void updatePlayerPosition() {
        // First, remove existing player images from all tiles
        for (int i = 0; i < 32; i++) {
            FrameLayout tile = gridLayout.findViewWithTag(i);
            if (tile != null) {
                for (int j = 1; j <= 4; j++) {
                    ImageView playerPic = tile.findViewWithTag("player" + j);
                    if (playerPic != null) {
                        tile.removeView(playerPic);
                    }
                }
            }
        }

        // Now add updated player positions (only for non-bankrupt players)
        if (!playerBankrupt[0]) placePlayerOnTile(player1, 1);
        if (!playerBankrupt[1]) placePlayerOnTile(player2, 2);
        if (!playerBankrupt[2]) placePlayerOnTile(player3, 3);
        if (!playerBankrupt[3]) placePlayerOnTile(player4, 4);
    }

    private void placePlayerOnTile(Player player, int playerNumber) {
        FrameLayout tile = gridLayout.findViewWithTag(player.getCells());

        if (tile == null) {
            Log.w("PlacePlayer", "Tile not found for position: " + player.getCells() + " for player: " + player.getName());
            return;
        }

        Log.d("cells", "placePlayerOnTile: "+player.getName()+" cells "+player.getCells()+"  tile: "+tile.getTag());

        // VISUAL POSITIONING (always executed for all players)
        int[] gravities = {
                Gravity.TOP | Gravity.LEFT,      // Top Left
                Gravity.TOP | Gravity.RIGHT,     // Top Right
                Gravity.BOTTOM | Gravity.LEFT,   // Bottom Left
                Gravity.BOTTOM | Gravity.RIGHT   // Bottom Right
        };

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(40, 40);
        params.gravity = gravities[playerNumber-1];
        ImageView playerPic = new ImageView(this);
        playerPic.setImageBitmap(decodeBase64(player.getPicture()));
        playerPic.setTag("player" + playerNumber);
        playerPic.setLayoutParams(params);
        tile.addView(playerPic);

        // GAME LOGIC - Execute for player1 when both dice are clicked and it's their turn
        if (currentPlayer == player1 && cdice1 && cdice2 && playerNumber == 1 && gameInProgress) {
            Log.d("GameLogic", "Executing game logic for player1 at position: " + player.getCells());
            handleCurrentPlayerGameLogic(player, playerNumber, tile);
        }
    }

    private void handleCurrentPlayerGameLogic(Player player, int playerNumber, FrameLayout tile) {
        int currentPosition = player.getCells();
        Log.d("GameLogic", "handleCurrentPlayerGameLogic called for " + player.getName() + " at position " + currentPosition);

        // Handle jail logic - send to jail (position 8)
        if (currentPosition == 8 || currentPosition == 28) {
            Log.d("GameLogic", "Player landed on jail tile");
            jail[playerNumber-1] = player;
            jailCount[playerNumber-1] = 0;
            if (currentPosition == 28) { // Go to jail tile
                currentPlayer.setCells(8);
                updatePlayerPosition(); // Update position to show player in jail
            }
            Toast.makeText(this, player.getName() + " is sent to jail!", Toast.LENGTH_SHORT).show();
            endPlayer1Turn();
            return;
        }

        // Handle casino logic
        if (currentPosition == 16) {
            Log.d("GameLogic", "Player landed on casino");
            showCasinoDialog();
            return;
        }

        // Skip corner tiles that don't have cities
        if (currentPosition == 0 || currentPosition == 24) {
            endPlayer1Turn();
            return;
        }

        // Check if cityTiles array has valid data for this position
        if (currentPosition >= cityTiles.length || cityTiles[currentPosition] == null) {
            Log.w("GameLogic", "No city data for position " + currentPosition);
            endPlayer1Turn();
            return;
        }

        Log.d("GameLogic", "City found at position " + currentPosition + ": " + cityTiles[currentPosition].getName());

        if (cityTiles[currentPosition] != null) {
            // Handle property tax payment
            if (ownership[currentPosition] != player && ownership[currentPosition] != null) {
                int totalTax = cityTiles[currentPosition].getTax();
                if (cityTiles[currentPosition].isHouse()) {
                    totalTax += cityTiles[currentPosition].getHouse_tax();
                }
                if (cityTiles[currentPosition].isHotel()) {
                    totalTax += cityTiles[currentPosition].getHotel_tax();
                }

                // Check if player can pay tax
                if (payTax(player, playerNumber - 1, totalTax)) {
                    ownership[currentPosition].addCash(totalTax);
                    updateMoney();
                    Toast.makeText(this, player.getName() + " paid $" + totalTax + " to " +
                            ownership[currentPosition].getName(), Toast.LENGTH_SHORT).show();
                    endPlayer1Turn();
                }
                // If bankrupt, the game will handle it automatically
                return;
            }

            // Handle property purchase or management
            if (ownership[currentPosition] == null) { // Unowned property
                Log.d("GameLogic", "Player landed on unowned property, showing purchase dialog");
                showPreviewDialog(cityTiles[currentPosition], tile);
                return;
            } else if (ownership[currentPosition] == player) {
                // Player owns this property - show upgrade dialog
                Log.d("GameLogic", "Player landed on own property, showing upgrade dialog");
                showPreviewDialog(cityTiles[currentPosition], tile);
                return;
            } else {
                // This case should be handled above, but just in case
                Log.d("GameLogic", "Unexpected case - ending turn");
                endPlayer1Turn();
            }
        }
    }

    private void setPlayers() {
        SharedPreferences sharedPref = getApplicationContext()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        player1 = new Player(sharedPref.getString("username",null),sharedPref.getString("picture",null),startingCash,0);
        textView1.setText(player1.getName());
        player2 = new Player("player2", BitMapHelper.encodeTobase64(BitmapFactory.decodeResource(getResources(),R.drawable.defaultuser1)),startingCash,0);
        player3 = new Player("player3", BitMapHelper.encodeTobase64(BitmapFactory.decodeResource(getResources(),R.drawable.defaultuser2)),startingCash,0);
        player4 = new Player("player4", BitMapHelper.encodeTobase64(BitmapFactory.decodeResource(getResources(),R.drawable.defaultuser3)),startingCash,0);
        textView2.setText(player2.getName());
        textView3.setText(player3.getName());
        textView4.setText(player4.getName());
        players[0]=player1;
        players[1]=player2;
        players[2]=player3;
        players[3]=player4;
    }

    private void setlisteners() {
        dice1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cdice1 && !gameInProgress && currentPlayerIndex == 0 && !isAnimating) {
                    diceValue1 = (int) (Math.random() * 6) + 1;
                    Log.d("diceValue1", "Value: "+diceValue1);
                    dice1.setImageResource(getResources().getIdentifier("dice"+diceValue1,"drawable", getPackageName()));
                    dice1.setScaleType(ImageButton.ScaleType.FIT_CENTER);
                    cdice1 = true;

                    // Don't add to cells here - let animation handle it
                    // player1.addCells(diceValue1);

                    player1Turn();
                }
            }
        });

        dice2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cdice2 && !gameInProgress && currentPlayerIndex == 0 && !isAnimating) {
                    diceValue2 = (int) (Math.random() * 6) + 1;
                    Log.d("diceValue2", "Value: "+diceValue2);
                    dice2.setImageResource(getResources().getIdentifier("dice"+diceValue2,"drawable", getPackageName()));
                    dice2.setScaleType(ImageButton.ScaleType.FIT_CENTER);
                    cdice2 = true;

                    // Don't add to cells here - let animation handle it
                    // player1.addCells(diceValue2);

                    player1Turn();
                }
            }
        });

        // Rest of your button listeners remain the same...
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playerBankrupt[0] && !isAnimating) {
                    showPlayerPropertiesDialog(player1, 0);
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playerBankrupt[1] && !isAnimating) {
                    showPlayerPropertiesDialog(player2, 1);
                }
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playerBankrupt[2] && !isAnimating) {
                    showPlayerPropertiesDialog(player3, 2);
                }
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!playerBankrupt[3] && !isAnimating) {
                    showPlayerPropertiesDialog(player4, 3);
                }
            }
        });
    }

    // Method to show player properties dialog
    private void showPlayerPropertiesDialog(Player player, int playerIndex) {
        // Get all properties owned by this player
        List<City> ownedProperties = new ArrayList<>();
        List<Integer> propertyPositions = new ArrayList<>();

        for (int i = 0; i < ownership.length; i++) {
            if (ownership[i] == player && cityTiles[i] != null) {
                ownedProperties.add(cityTiles[i]);
                propertyPositions.add(i);
            }
        }

        if (ownedProperties.isEmpty()) {
            Toast.makeText(this, player.getName() + " doesn't own any properties", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(player.getName() + "'s Properties");

        // Create a list of property names
        String[] propertyNames = new String[ownedProperties.size()];
        for (int i = 0; i < ownedProperties.size(); i++) {
            City city = ownedProperties.get(i);
            String upgrades = "";
            if (city.isHouse()) upgrades += " [House]";
            if (city.isHotel()) upgrades += " [Hotel]";
            propertyNames[i] = city.getName() + upgrades;
        }

        builder.setItems(propertyNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Show preview of selected property
                City selectedCity = ownedProperties.get(which);
                int position = propertyPositions.get(which);
                FrameLayout tile = gridLayout.findViewWithTag(position);
                if (tile != null) {
                    showPreviewDialog(selectedCity, tile);
                }
            }
        });

        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void initializeViews() {
        dice1 = findViewById(R.id.dice1);
        dice2 = findViewById(R.id.dice2);
        button1 = findViewById(R.id.wealth1);
        button2 = findViewById(R.id.wealth2);
        button3 = findViewById(R.id.wealth3);
        button4 = findViewById(R.id.wealth4);
        textView1 = findViewById(R.id.tv1);
        textView2 = findViewById(R.id.tv2);
        textView3 = findViewById(R.id.tv3);
        textView4 = findViewById(R.id.tv4);
    }

    private void createGrid(){
        gridLayout.setColumnCount(GRID_SIZE);
        gridLayout.setRowCount(GRID_SIZE);
        int countID = 0;

        // Create cells for the grid layout
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                // Only create cells for the border of the grid
                if (row == 0 || row == GRID_SIZE - 1 || col == 0 || col == GRID_SIZE - 1) {
                    FrameLayout tile = createTile(row, col, countID);
                    countID++;

                    // FIXED: Consistent tile tagging logic
                    int tileIndex;
                    if (row == 0) {
                        // Top row: left to right (0-8)
                        tileIndex = col;
                    } else if (col == GRID_SIZE - 1) {
                        // Right side: top to bottom (9-16)
                        tileIndex = 8 + row;
                    } else if (row == GRID_SIZE - 1) {
                        // Bottom row: right to left (17-24)
                        tileIndex = 16 + (GRID_SIZE - 1 - col);
                    } else {
                        // Left side: bottom to top (25-31)
                        tileIndex = 24 + (GRID_SIZE - 1 - row);
                    }

                    tile.setTag(tileIndex);
                    Log.d("tile tag", "createGrid: row=" + row + " col=" + col + " tag=" + tileIndex);

                    GridLayout.Spec rowSpec = GridLayout.spec(row);
                    GridLayout.Spec colSpec = GridLayout.spec(col);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);

                    // Convert dp to pixels
                    int cellSizePx = (int) (CELL_SIZE_DP * getResources().getDisplayMetrics().density);
                    params.width = cellSizePx;
                    params.height = cellSizePx;
                    params.setMargins(1, 1, 1, 1);

                    gridLayout.addView(tile, params);
                }
                // Cells inside grid
                else {
                    FrameLayout cell = new FrameLayout(this);
                    cell.setBackgroundColor(Color.parseColor("#FFB793"));
                    GridLayout.Spec rowSpec = GridLayout.spec(row);
                    GridLayout.Spec colSpec = GridLayout.spec(col);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);

                    // Convert dp to pixels
                    int cellSizePx = (int) (CELL_SIZE_DP * getResources().getDisplayMetrics().density);
                    params.width = cellSizePx;
                    params.height = cellSizePx;
                    params.setMargins(0,0,0,0);

                    gridLayout.addView(cell, params);
                }
            }
        }
    }

    private FrameLayout createTile(int row, int col, int id) {
        FrameLayout tile = new FrameLayout(this);

        if(row==0 && col==0){
            tile.setBackgroundColor(Color.WHITE);
            ImageButton corner = new ImageButton(GameboardActivity.this);
            corner.setScaleType(ImageView.ScaleType.CENTER_CROP);
            corner.setBackgroundColor(0);
            corner.setImageResource(R.drawable.go);
            corner.setTag("start");
            tile.addView(corner);
        } else if ((row==0 && col==8) || row==8 && (col==0 || col==8)) {
            tile.setBackgroundColor(Color.WHITE);
            ImageButton corner = new ImageButton(GameboardActivity.this);
            corner.setScaleType(ImageView.ScaleType.CENTER_CROP);
            corner.setBackgroundColor(0);
            if (row==col){
                corner.setImageResource(R.drawable.jackpot);
                corner.setTag("casino");
            } else if (row>col) {
                corner.setImageResource(R.drawable.arrested);
                corner.setTag("arrest");
            }
            else {
                corner.setImageResource(R.drawable.prison);
                corner.setTag("jail");
            }
            tile.addView(corner);
        } else {
            // CHANGED: Use pre-loaded cities instead of querying database
            if (citiesLoaded && !allCities.isEmpty()) {
                createCityTile(tile, id);
            } else {
                Log.w("Cities", "Cities not loaded yet for tile " + id);
            }
        }

        return tile;
    }

    // Create city tile using pre-loaded data
    private void createCityTile(FrameLayout tile, int id) {
        // Get random city from pre-loaded list
        Random random = new Random();
        DocumentSnapshot randomDoc = allCities.get(random.nextInt(allCities.size()));

        String cityName = randomDoc.getString("name");
        String cityPic = randomDoc.getString("picture");
        String cityColor = randomDoc.getString("color");
        int cityCost = Integer.parseInt(randomDoc.get("cost").toString());
        int cityTax = Integer.parseInt(randomDoc.get("tax").toString());
        int cityHotelCost = Integer.parseInt(randomDoc.get("hotel_cost").toString());
        int cityHouseCost = Integer.parseInt(randomDoc.get("house_cost").toString());
        int cityHotelTax = Integer.parseInt(randomDoc.get("hotel_tax").toString());
        int cityHouseTax = Integer.parseInt(randomDoc.get("house_tax").toString());
        Boolean cityHotel = randomDoc.getBoolean("hotel");
        Boolean cityHouse = randomDoc.getBoolean("house");

        City city = new City(cityName, cityCost, cityTax,
                Boolean.TRUE.equals(cityHouse), cityHouseCost, cityHouseTax,
                Boolean.TRUE.equals(cityHotel), cityHotelCost, cityHotelTax,
                cityPic, cityColor);

        cityTiles[id] = city;

        ImageButton ibcity = new ImageButton(GameboardActivity.this);
        ibcity.setBackgroundColor(Color.parseColor(cityColor));
        ibcity.setImageBitmap(BitMapHelper.decodeBase64(cityPic));
        ibcity.setTag(cityName);
        ibcity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreviewDialog(city, tile);
            }
        });
        tile.addView(ibcity);

        Log.d("RandomCity", "Assigned city: " + cityName + " to tile " + id);
    }

    public void showPreviewDialog(City city, FrameLayout tile) {
        Log.d("PreviewDialog", "showPreviewDialog called for city: " + city.getName());
        Log.d("PreviewDialog", "Tile tag: " + tile.getTag());
        Log.d("PreviewDialog", "cdice1: " + cdice1 + ", cdice2: " + cdice2);

        // Create the main CardView
        CardView cardView = new CardView(GameboardActivity.this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(dpToPx(GameboardActivity.this, 16), dpToPx(GameboardActivity.this, 16),
                dpToPx(GameboardActivity.this, 16), dpToPx(GameboardActivity.this, 16));
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(GameboardActivity.this, 8));
        cardView.setCardElevation(dpToPx(GameboardActivity.this, 4));

        // Create main LinearLayout
        LinearLayout mainLayout = new LinearLayout(GameboardActivity.this);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // Create Preview header TextView
        TextView previewHeader = new TextView(GameboardActivity.this);
        previewHeader.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        previewHeader.setText("Preview");
        previewHeader.setTextSize(16);
        previewHeader.setTypeface(null, Typeface.BOLD);
        previewHeader.setGravity(Gravity.CENTER);
        previewHeader.setPadding(dpToPx(GameboardActivity.this, 8), dpToPx(GameboardActivity.this, 8),
                dpToPx(GameboardActivity.this, 8), dpToPx(GameboardActivity.this, 8));
        previewHeader.setBackgroundColor(Color.parseColor("#EEEEEE"));

        // Create color preview View
        View colorPreview = new View(GameboardActivity.this);
        colorPreview.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(GameboardActivity.this, 24)
        ));
        colorPreview.setBackgroundColor(Color.parseColor(city.getColor()));

        // Create city name TextView
        TextView cityName = new TextView(GameboardActivity.this);
        cityName.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        cityName.setText(city.getName());
        cityName.setTextSize(18);
        cityName.setTypeface(null, Typeface.BOLD);
        cityName.setGravity(Gravity.CENTER);
        cityName.setPadding(dpToPx(GameboardActivity.this, 8), dpToPx(GameboardActivity.this, 8),
                dpToPx(GameboardActivity.this, 8), dpToPx(GameboardActivity.this, 8));

        // Create city ImageView
        ImageView cityImage = new ImageView(GameboardActivity.this);
        cityImage.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(GameboardActivity.this, 200)
        ));
        cityImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        cityImage.setImageBitmap(decodeBase64(city.getPicture()));
        cityImage.setBackgroundColor(Color.parseColor("#DDDDDD"));

        // Create TableLayout for property details
        TableLayout tableLayout = new TableLayout(GameboardActivity.this);
        LinearLayout.LayoutParams tableParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tableLayout.setLayoutParams(tableParams);
        tableLayout.setPadding(dpToPx(GameboardActivity.this, 8), dpToPx(GameboardActivity.this, 8),
                dpToPx(GameboardActivity.this, 8), dpToPx(GameboardActivity.this, 8));

        // Create table rows
        addTableRow(GameboardActivity.this, tableLayout, "Price:", String.valueOf(city.getCost()));
        addTableRow(GameboardActivity.this, tableLayout, "Tax:", String.valueOf(city.getTax()));
        addTableRow(GameboardActivity.this, tableLayout, "House:", city.getHouse_cost() + " / " + city.getHouse_tax());
        addTableRow(GameboardActivity.this, tableLayout, "Hotel:", city.getHotel_cost() + " / " + city.getHotel_tax());

        // Add all views to main layout
        mainLayout.addView(previewHeader);
        mainLayout.addView(colorPreview);
        mainLayout.addView(cityName);
        mainLayout.addView(cityImage);
        mainLayout.addView(tableLayout);

        // Add main layout to CardView
        cardView.addView(mainLayout);

        // Create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(GameboardActivity.this);
        builder.setView(cardView);
        int tilePosition = Integer.parseInt(tile.getTag().toString());

        Log.d("PreviewDialog", "Tile position: " + tilePosition);
        Log.d("PreviewDialog", "Ownership at position: " + (ownership[tilePosition] != null ? ownership[tilePosition].getName() : "null"));
        Log.d("PreviewDialog", "Current player: " + (currentPlayer != null ? currentPlayer.getName() : "null"));

        // UNOWNED PROPERTY - Show buy options
        if (ownership[tilePosition] == null && cdice1 && cdice2) {
            Log.d("PreviewDialog", "Setting up buy dialog for unowned property");
            builder.setCancelable(false);
            builder.setPositiveButton("Buy", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("PreviewDialog", "Buy button clicked");
                    if (currentPlayer.getCash() >= city.getCost()) {
                        currentPlayer.delCash(city.getCost());
                        ownership[tilePosition] = currentPlayer;
                        updateMoney();
                        Log.d("Ownership", city.getName() + " owned by " + currentPlayer.getName());
                        Toast.makeText(GameboardActivity.this, "You bought " + city.getName() + "!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GameboardActivity.this, "Insufficient funds", Toast.LENGTH_SHORT).show();
                        Log.d("Ownership", currentPlayer.getName() + " failed to buy " + city.getName());
                    }
                    dialog.dismiss();
                    endPlayer1Turn();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("PreviewDialog", "Cancel button clicked");
                    dialog.dismiss();
                    endPlayer1Turn();
                }
            });
        }
        // OWNED BY PLAYER1 - Show upgrade options
        else if (ownership[tilePosition] == player1 && cdice1 && cdice2) {
            Log.d("PreviewDialog", "Setting up upgrade dialog for owned property");
            builder.setCancelable(false);
            builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("PreviewDialog", "Close button clicked");
                    dialog.dismiss();
                    endPlayer1Turn();
                }
            });

            if (!city.isHotel()) {
                builder.setPositiveButton("Buy Hotel ($" + city.getHotel_cost() + ")", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("PreviewDialog", "Buy Hotel button clicked");
                        if (currentPlayer.getCash() >= city.getHotel_cost()) {
                            city.setHotel(true);
                            currentPlayer.delCash(city.getHotel_cost());
                            updateMoney();
                            Toast.makeText(GameboardActivity.this, "Hotel built on " + city.getName() + "!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GameboardActivity.this, "Insufficient funds for hotel", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                        endPlayer1Turn();
                    }
                });
            }

            if (!city.isHouse()) {
                builder.setNegativeButton("Buy House ($" + city.getHouse_cost() + ")", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("PreviewDialog", "Buy House button clicked");
                        if (currentPlayer.getCash() >= city.getHouse_cost()) {
                            city.setHouse(true);
                            currentPlayer.delCash(city.getHouse_cost());
                            updateMoney();
                            Toast.makeText(GameboardActivity.this, "House built on " + city.getName() + "!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(GameboardActivity.this, "Insufficient funds for house", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                        endPlayer1Turn();
                    }
                });
            }
        }
        // JUST VIEWING (clicked on tile, not landed on it during turn)
        else {
            Log.d("PreviewDialog", "Setting up view-only dialog");
            builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        try {
            AlertDialog dialog = builder.create();
            Log.d("PreviewDialog", "About to show dialog");
            dialog.show();
            Log.d("PreviewDialog", "Dialog.show() called successfully");
        } catch (Exception e) {
            Log.e("PreviewDialog", "Error showing dialog: " + e.getMessage(), e);
            // If dialog fails, continue the game
            if (cdice1 && cdice2) {
                endPlayer1Turn();
            }
        }
    }

    private static void addTableRow(Context context, TableLayout tableLayout, String label, String value) {
        TableRow tableRow = new TableRow(context);
        tableRow.setLayoutParams(new TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Label TextView
        TextView labelTextView = new TextView(context);
        labelTextView.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        labelTextView.setText(label);
        labelTextView.setTypeface(null, android.graphics.Typeface.BOLD);
        labelTextView.setPadding(dpToPx(context, 4), dpToPx(context, 4),
                dpToPx(context, 4), dpToPx(context, 4));

        // Value TextView
        TextView valueTextView = new TextView(context);
        valueTextView.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        valueTextView.setText(value);
        valueTextView.setPadding(dpToPx(context, 4), dpToPx(context, 4),
                dpToPx(context, 4), dpToPx(context, 4));

        tableRow.addView(labelTextView);
        tableRow.addView(valueTextView);
        tableLayout.addView(tableRow);
    }

    private static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void showCasinoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎰 Welcome to the Casino!");
        builder.setMessage("You landed on the casino! Want to try your luck?\n" +
                "Current cash: $" + player1.getCash());
        builder.setCancelable(false);

        builder.setPositiveButton("Gamble", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showGamblingAmountDialog();
            }
        });

        builder.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                endPlayer1Turn(); // Changed from finish()
            }
        });

        builder.create().show();
    }

    private void showGamblingDialog(int gamblingAmount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎲 Casino Gambling");
        builder.setMessage("You're gambling $" + gamblingAmount + "!\n\n" +
                "Roll the dice! If both dice show the same number, you win $" + (gamblingAmount * 2) + "!\n" +
                "Otherwise, you lose your $" + gamblingAmount + ".");
        builder.setCancelable(false);

        builder.setPositiveButton("Roll Dice", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int casinoDice1 = (int) (Math.random() * 6) + 1;
                int casinoDice2 = (int) (Math.random() * 6) + 1;

                if (casinoDice1 == casinoDice2) {
                    player1.addCash(gamblingAmount);
                    updateMoney();
                    Toast.makeText(GameboardActivity.this,
                            "🎉 JACKPOT! You rolled " + casinoDice1 + "-" + casinoDice2 +
                                    "! You won $" + gamblingAmount + "!", Toast.LENGTH_LONG).show();
                } else {
                    // Check for bankruptcy after gambling loss
                    if (!payTax(player1, 0, gamblingAmount)) {
                        // Player is bankrupt, game handles it
                        return;
                    }
                    Toast.makeText(GameboardActivity.this,
                            "😔 Sorry! You rolled " + casinoDice1 + "-" + casinoDice2 +
                                    ". You lost $" + gamblingAmount, Toast.LENGTH_LONG).show();
                }
                endPlayer1Turn();
            }
        });

        builder.create().show();
    }

    private void showGamblingAmountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("💰 Choose Gambling Amount");
        builder.setMessage("How much do you want to gamble?\n" +
                "Current cash: $" + player1.getCash());
        builder.setCancelable(false);

        // Create input field for custom amount
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter amount (e.g., 50)");
        builder.setView(input);

        // Add some preset buttons for common amounts
        builder.setPositiveButton("Gamble This Amount", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String amountStr = input.getText().toString().trim();
                if (!amountStr.isEmpty()) {
                    try {
                        int amount = Integer.parseInt(amountStr);
                        if (amount > 0 && amount <= player1.getCash()) {
                            showGamblingDialog(amount);
                        } else if (amount > player1.getCash()) {
                            Toast.makeText(GameboardActivity.this, "Not enough money!", Toast.LENGTH_SHORT).show();
                            endPlayer1Turn();
                        } else {
                            Toast.makeText(GameboardActivity.this, "Invalid amount!", Toast.LENGTH_SHORT).show();
                            endPlayer1Turn();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(GameboardActivity.this, "Please enter a valid number!", Toast.LENGTH_SHORT).show();
                        endPlayer1Turn();
                    }
                } else {
                    Toast.makeText(GameboardActivity.this, "No amount entered!", Toast.LENGTH_SHORT).show();
                    endPlayer1Turn();
                }
            }
        });

        builder.setNeutralButton("Quick $50", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (player1.getCash() >= 50) {
                    showGamblingDialog(50);
                } else {
                    Toast.makeText(GameboardActivity.this, "Not enough money!", Toast.LENGTH_SHORT).show();
                    endPlayer1Turn();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                endPlayer1Turn();
            }
        });

        builder.create().show();
    }

    private void handleAICasino(Player aiPlayer) {
        if (aiPlayer.getCash() >= 20) {
            boolean willGamble = Math.random() < 0.5;

            if (willGamble) {
                Log.d("Casino", aiPlayer.getName() + " decided to gamble $20");
                Toast.makeText(this, aiPlayer.getName() + " is gambling at the casino!", Toast.LENGTH_SHORT).show();

                int casinoDice1 = (int) (Math.random() * 6) + 1;
                int casinoDice2 = (int) (Math.random() * 6) + 1;

                if (casinoDice1 == casinoDice2) {
                    aiPlayer.addCash(20);
                    updateMoney();
                    Toast.makeText(this, aiPlayer.getName() + " won at the casino! Rolled " +
                            casinoDice1 + "-" + casinoDice2 + " and won $20!", Toast.LENGTH_LONG).show();
                } else {
                    aiPlayer.delCash(20);
                    updateMoney();
                    Toast.makeText(this, aiPlayer.getName() + " lost at the casino. Rolled " +
                            casinoDice1 + "-" + casinoDice2 + " and lost $20.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, aiPlayer.getName() + " visited the casino but decided not to gamble.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, aiPlayer.getName() + " visited the casino but doesn't have enough money to gamble.", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to check if a player is bankrupt and handle bankruptcy
    private void checkPlayerBankruptcy(Player player, int playerIndex) {
        if (player.getCash() < 0 && !playerBankrupt[playerIndex]) {
            handlePlayerBankruptcy(player, playerIndex);
        }
    }

    // Method to handle player bankruptcy
    private void handlePlayerBankruptcy(Player player, int playerIndex) {
        Log.d("Bankruptcy", player.getName() + " is bankrupt with $" + player.getCash());

        playerBankrupt[playerIndex] = true;
        activePlayers--;

        // Transfer all properties back to unowned status
        for (int i = 0; i < ownership.length; i++) {
            if (ownership[i] == player) {
                ownership[i] = null;
                Log.d("Bankruptcy", "Property at position " + i + " returned to bank");
            }
        }

        // Set player cash to 0
        player.setCash(0);
        updateMoney();

        // Show specific bankruptcy dialog for human player (player1)
        if (playerIndex == 0) {
            showPlayerBankruptcyDialog();
        } else {
            // Show regular toast for AI players
            Toast.makeText(this, player.getName() + " is bankrupt and eliminated from the game!",
                    Toast.LENGTH_LONG).show();
        }

        // Hide the bankrupt player's UI elements
        hidePlayerUI(playerIndex);

        // Check if game should end
        checkGameEnd();
    }

    // Method to show bankruptcy dialog for the player
    private void showPlayerBankruptcyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("💸 You're Bankrupt!");
        builder.setMessage("Unfortunately, you have run out of money and are eliminated from the game.\n\n" +
                "All your properties have been returned to the bank.\n\n" +
                "Better luck next time!");
        builder.setCancelable(false);

        builder.setPositiveButton("Continue Watching", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(GameboardActivity.this, "You can watch the remaining players continue the game.",
                        Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Exit Game", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Close the activity
            }
        });

        builder.create().show();
    }

    // Method to hide UI elements for bankrupt players
    private void hidePlayerUI(int playerIndex) {
        switch (playerIndex) {
            case 0:
                textView1.setVisibility(View.GONE);
                button1.setVisibility(View.GONE);
                break;
            case 1:
                textView2.setVisibility(View.GONE);
                button2.setVisibility(View.GONE);
                break;
            case 2:
                textView3.setVisibility(View.GONE);
                button3.setVisibility(View.GONE);
                break;
            case 3:
                textView4.setVisibility(View.GONE);
                button4.setVisibility(View.GONE);
                break;
        }
    }

    // Method to check if the game should end
    private void checkGameEnd() {
        if (activePlayers <= 1) {
            finishGame();
        }
    }

    // Method to finish the game and declare winner
    private void finishGame() {
        Player winner = null;

        // Find the remaining active player
        for (int i = 0; i < 4; i++) {
            if (!playerBankrupt[i]) {
                winner = players[i];
                break;
            }
        }

        if (winner != null) {
            showGameEndDialog(winner);
        } else {
            // This shouldn't happen, but just in case
            Toast.makeText(this, "Game ended - No winner determined", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Method to show game end dialog with winner
    private void showGameEndDialog(Player winner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🎉 Game Over!");
        builder.setMessage("Congratulations!\n\n" +
                winner.getName() + " wins the game!\n\n" +
                "Final cash: $" + winner.getCash() + "\n\n" +
                "Thanks for playing Monopoly!");
        builder.setCancelable(false);

        builder.setPositiveButton("New Game", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Restart the game
                restartGame();
            }
        });

        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Close the activity
            }
        });

        builder.create().show();
    }

    // Method to restart the game
    private void restartGame() {
        // Reset all player stats
        for (int i = 0; i < 4; i++) {
            players[i].setCash(startingCash);
            players[i].setCells(0);
            playerBankrupt[i] = false;
            jail[i] = null;
            jailCount[i] = 0;
        }

        // Reset ownership
        for (int i = 0; i < ownership.length; i++) {
            ownership[i] = null;
        }

        // Reset game state
        activePlayers = 4;
        currentPlayerIndex = 0;
        currentPlayer = player1;
        gameInProgress = false;
        cdice1 = false;
        cdice2 = false;

        // Show all player UI elements
        for (int i = 0; i < 4; i++) {
            showPlayerUI(i);
        }

        // Update UI
        updateMoney();
        updatePlayerPosition();
        enableDiceForPlayer1();

        Toast.makeText(this, "New game started!", Toast.LENGTH_SHORT).show();
    }

    // Method to show player UI elements
    private void showPlayerUI(int playerIndex) {
        switch (playerIndex) {
            case 0:
                textView1.setVisibility(View.VISIBLE);
                button1.setVisibility(View.VISIBLE);
                break;
            case 1:
                textView2.setVisibility(View.VISIBLE);
                button2.setVisibility(View.VISIBLE);
                break;
            case 2:
                textView3.setVisibility(View.VISIBLE);
                button3.setVisibility(View.VISIBLE);
                break;
            case 3:
                textView4.setVisibility(View.VISIBLE);
                button4.setVisibility(View.VISIBLE);
                break;
        }
    }

    // Method to get next active player index
    private int getNextActivePlayerIndex(int currentIndex) {
        int nextIndex = (currentIndex + 1) % 4;

        // Skip bankrupt players
        while (playerBankrupt[nextIndex] && nextIndex != currentIndex) {
            nextIndex = (nextIndex + 1) % 4;
        }

        return nextIndex;
    }

    // Modified method to check bankruptcy during tax payment
    private boolean payTax(Player player, int playerIndex, int taxAmount) {
        player.delCash(taxAmount);

        // Check for bankruptcy after paying tax
        if (player.getCash() < 0) {
            checkPlayerBankruptcy(player, playerIndex);
            return false; // Player is bankrupt
        }

        return true; // Player can continue
    }
}