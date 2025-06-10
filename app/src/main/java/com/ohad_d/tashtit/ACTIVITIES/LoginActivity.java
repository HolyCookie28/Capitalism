package com.ohad_d.tashtit.ACTIVITIES;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.ohad_d.helper.AlertUtil;
import com.ohad_d.helper.BitMapHelper;
import com.ohad_d.helper.Global;
import com.ohad_d.model.User;
import com.ohad_d.model.Users;
import com.ohad_d.tashtit.R;
import com.ohad_d.viewmodel.UserViewModel;

public class LoginActivity extends AppCompatActivity {
    private Button btnReg;
    private Button btnLog;
    private TextView loginTxt;
    private EditText etEmail;
    private EditText etUsername;
    private EditText etPassword;
    private ImageButton pfp;
    private ConstraintLayout constraintLayout;
    private UserViewModel viewModel;
    private ArrayAdapter<User> adapter;
    private FirebaseFirestore db;
    private User user;
    private ActivityResultLauncher<Void>   cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Bitmap bitmapPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        FirebaseApp.initializeApp(LoginActivity.this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setViewModel();
        setListeners();
        setLaunchers();
    }

    private void setListeners() {
        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (constraintLayout.getVisibility() == View.VISIBLE)
                {
                    constraintLayout.setVisibility(View.GONE);
                    loginTxt.setText("Login");
                }
                else {
                    db = FirebaseFirestore.getInstance();
                    viewModel.loginUser(etUsername.getText().toString(),etPassword.getText().toString())
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()){
                                    User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                                    saveUserToPreferences(user);
                                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                                    startActivity(intent);
                                }else {
                                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                                }
                            });
                    //loginUser(etUsername.getText().toString(),etPassword.getText().toString());
                }
            }
        });
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (constraintLayout.getVisibility() == View.GONE)
                {
                    constraintLayout.setVisibility(View.VISIBLE);
                    loginTxt.setText("Register");
                }
                else {
                    if (!isValidEmail(etEmail.getText().toString())) {
                        Toast.makeText(LoginActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();}
                    else if (etUsername.getText()!=null && etPassword.getText()!=null) {
                        db = FirebaseFirestore.getInstance();
                        if (bitmapPhoto==null)
                            bitmapPhoto = BitmapFactory.decodeResource(getResources(),R.drawable.defaultuser4);
                        Log.d("EncodedPicture", BitMapHelper.encodeTobase64(bitmapPhoto));

                        registerUser(etUsername.getText().toString(),etEmail.getText().toString(),etPassword.getText().toString(),BitMapHelper.encodeTobase64(bitmapPhoto));
                    }
                }
            }
        });
        pfp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.takePicture(LoginActivity.this,cameraLauncher,galleryLauncher,requestPermissionLauncher);
            }
        });
    }

    private void initializeViews() {
        btnLog = findViewById(R.id.btnLog);
        btnReg = findViewById(R.id.btnReg);
        loginTxt = findViewById(R.id.logintxt);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        pfp = findViewById(R.id.pfp);
        constraintLayout = findViewById(R.id.constraintLayout);

    }

    private void setViewModel() {
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

       /* viewModel.getAll();
        viewModel.getLiveDataCollection().observe(this, new Observer<Users>() {
            @Override
            public void onChanged(Users users) {
                adapter.clear();
                adapter.addAll(users);
                adapter.notifyDataSetChanged();
            }
        });*/
    }

    public void registerUser(String inputName, String email, String password, String picture) {
        db.collection("Users")
                .whereEqualTo("name", inputName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        user = new User(inputName,picture,email,password);

                        db.collection("Users").add(user)
                                .addOnSuccessListener(documentReference -> {
                                    String docId = documentReference.getId();
                                    user.setIdFs(docId);
                                    documentReference.update("idFs", docId);
                                    Toast.makeText(LoginActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Username already exists
                        Toast.makeText(LoginActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
    public boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();}

    private void saveUserToPreferences(User user) {
        SharedPreferences sharedPref = getApplicationContext()
                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("userIdFs", user.getIdFs());
        editor.putString("username", user.getName());
        editor.putString("picture", user.getPicture());
        editor.apply();
    }

    private void setLaunchers(){
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitMap -> {
                    if (bitMap != null){
                        bitmapPhoto = bitMap;
                        pfp.setImageBitmap(bitmapPhoto);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null){
                        final Uri imageUri = result.getData().getData();
                        try {
                            bitmapPhoto= BitMapHelper.uriToBitmap(imageUri, LoginActivity.this);
                            pfp.setImageBitmap(bitmapPhoto);

                        }
                        catch (Exception e) {}
                    }
                });
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Launch appropriate action based on currentRequestType
                        if (Global.getCurrentRequestType() == 0) {
                            cameraLauncher.launch(null);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            galleryLauncher.launch(intent);
                        }
                    } else {
                        AlertUtil.alertOk(LoginActivity.this, "Permission required", "Permission required to access camera/gallery", true, 0);
                    }
                });
     }
}