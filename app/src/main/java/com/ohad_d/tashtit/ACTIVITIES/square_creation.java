package com.ohad_d.tashtit.ACTIVITIES;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.ohad_d.helper.AlertUtil;
import com.ohad_d.helper.BitMapHelper;
import com.ohad_d.helper.Global;
import com.ohad_d.model.City;
import com.ohad_d.tashtit.R;
import com.ohad_d.viewmodel.CityViewModel;

import java.util.HashMap;
import java.util.Map;

public class square_creation extends AppCompatActivity {

    // UI Elements
    private EditText editCityName, editCityPrice, editCityTax;
    private EditText editHouseCost, editHouseTax, editHotelCost, editHotelTax;
    private Button btnSelectImage, btnCreateCity;
    private ImageView imgCityPreview, imgPreviewCity;
    private View viewColorPreview;
    private TextView txtPreviewCityName, txtPreviewPrice, txtPreviewTax, txtPreviewHouse, txtPreviewHotel;

    // Data variables
    private Bitmap selectedImageBitmap;
    private String selectedColor = "#4CAF50"; // Default green
    private Map<String, String> colorMap;
    private CityViewModel viewModel;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Void>   cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_square_creation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize UI elements
        initViews();
        setLaunchers();

        // Initialize color map
        initColorMap();

        setViewModel();

        // Set button click listeners
        setClickListeners();

        // Initialize with default values
        initPreview();
    }

    private void setLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitMap -> {
                    if (bitMap != null) {
                        selectedImageBitmap = bitMap;
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        final Uri imageUri = result.getData().getData();
                        try {
                            selectedImageBitmap = BitMapHelper.uriToBitmap(imageUri, square_creation.this);
                        } catch (Exception e) {
                        }
                    }
                });

        // This launcher is needed if you need to check the permission
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
                        AlertUtil.alertOk(square_creation.this, "Permission required", "Permission required to access camera/gallery", true, 0);
                    }
                });
    }

    private void initViews() {
        // EditText fields
        editCityName = findViewById(R.id.edit_city_name);
        editCityPrice = findViewById(R.id.edit_city_price);
        editCityTax = findViewById(R.id.edit_city_tax);
        editHouseCost = findViewById(R.id.edit_house_cost);
        editHouseTax = findViewById(R.id.edit_house_tax);
        editHotelCost = findViewById(R.id.edit_hotel_cost);
        editHotelTax = findViewById(R.id.edit_hotel_tax);

        // Buttons
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnCreateCity = findViewById(R.id.btn_create_city);

        // Images
        imgCityPreview = findViewById(R.id.img_city_preview);
        imgPreviewCity = findViewById(R.id.img_preview_city);

        // Preview elements
        viewColorPreview = findViewById(R.id.view_color_preview);
        txtPreviewCityName = findViewById(R.id.txt_preview_city_name);
        txtPreviewPrice = findViewById(R.id.txt_preview_price);
        txtPreviewTax = findViewById(R.id.txt_preview_tax);
        txtPreviewHouse = findViewById(R.id.txt_preview_house);
        txtPreviewHotel = findViewById(R.id.txt_preview_hotel);
    }

    private void initColorMap() {
        colorMap = new HashMap<>();
        colorMap.put("btn_color_brown", "#795548");
        colorMap.put("btn_color_purple", "#7B1FA2");
        colorMap.put("btn_color_pink", "#E91E63");
        colorMap.put("btn_color_orange", "#FF9800");
        colorMap.put("btn_color_red", "#F44336");
        colorMap.put("btn_color_yellow", "#FFEB3B");
        colorMap.put("btn_color_green", "#4CAF50");
        colorMap.put("btn_color_blue", "#2196F3");
    }

    private void setClickListeners() {
        // Select image button
        btnSelectImage.setOnClickListener(v -> Global.takePicture(square_creation.this,cameraLauncher,galleryLauncher,requestPermissionLauncher));

        // Create city button
        btnCreateCity.setOnClickListener(v -> {
            if (validateInputs()) {
                createCity();
            }
        });

        // Text change listeners for real-time preview updates
        EditText[] editTexts = {
                editCityName, editCityPrice, editCityTax,
                editHouseCost, editHouseTax, editHotelCost, editHotelTax
        };

        for (EditText editText : editTexts) {
            editText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    updatePreview();
                }
            });
        }
    }

    private void initPreview() {
        // Set default preview values
        viewColorPreview.setBackgroundColor(Color.parseColor(selectedColor));
        txtPreviewCityName.setText("City Name");
        txtPreviewPrice.setText("$200");
        txtPreviewTax.setText("$20");
        txtPreviewHouse.setText("$100 / $50");
        txtPreviewHotel.setText("$500 / $150");
    }

    // Called when a color button is clicked
    public void onColorSelected(View view) {
        String colorId = view.getResources().getResourceEntryName(view.getId());
        if (colorMap.containsKey(colorId)) {
            selectedColor = colorMap.get(colorId);
            viewColorPreview.setBackgroundColor(Color.parseColor(selectedColor));
        }
    }

    private void updatePreview() {
        // Update city name
        String cityName = getTextOrDefault(editCityName, "City Name");
        txtPreviewCityName.setText(cityName);

        // Update price
        String price = "$" + getTextOrDefault(editCityPrice, "200");
        txtPreviewPrice.setText(price);

        // Update tax
        String tax = "$" + getTextOrDefault(editCityTax, "20");
        txtPreviewTax.setText(tax);

        // Update house details
        String houseCost = getTextOrDefault(editHouseCost, "100");
        String houseTax = getTextOrDefault(editHouseTax, "50");
        txtPreviewHouse.setText("$" + houseCost + " / $" + houseTax);

        // Update hotel details
        String hotelCost = getTextOrDefault(editHotelCost, "500");
        String hotelTax = getTextOrDefault(editHotelTax, "150");
        txtPreviewHotel.setText("$" + hotelCost + " / $" + hotelTax);
    }

    private String getTextOrDefault(EditText editText, String defaultValue) {
        String text = editText.getText() != null ? editText.getText().toString().trim() : "";
        return text.isEmpty() ? defaultValue : text;
    }

    private boolean validateInputs() {
        // Check if city name is provided
        if (editCityName.getText() == null || editCityName.getText().toString().trim().isEmpty()) {
            showToast("Please enter a city name");
            return false;
        }

        // Check if city price is provided
        if (editCityPrice.getText() == null || editCityPrice.getText().toString().trim().isEmpty()) {
            showToast("Please enter a city price");
            return false;
        }

        // Check if city tax is provided
        if (editCityTax.getText() == null || editCityTax.getText().toString().trim().isEmpty()) {
            showToast("Please enter a city tax");
            return false;
        }

        // Check if house cost is provided
        if (editHouseCost.getText() == null || editHouseCost.getText().toString().trim().isEmpty()) {
            showToast("Please enter a house cost");
            return false;
        }

        // Check if house tax is provided
        if (editHouseTax.getText() == null || editHouseTax.getText().toString().trim().isEmpty()) {
            showToast("Please enter a house tax");
            return false;
        }

        // Check if hotel cost is provided
        if (editHotelCost.getText() == null || editHotelCost.getText().toString().trim().isEmpty()) {
            showToast("Please enter a hotel cost");
            return false;
        }

        // Check if hotel tax is provided
        if (editHotelTax.getText() == null || editHotelTax.getText().toString().trim().isEmpty()) {
            showToast("Please enter a hotel tax");
            return false;
        }

        // Check if image is selected
        if (selectedImageBitmap == null) {
            selectedImageBitmap = vectorToBitmap(square_creation.this,R.drawable.ic_city,100,100);
        }

        return true;
    }

    private void createCity() {
        try {
            // Parse input values
            String cityName = editCityName.getText().toString().trim();
            int cityPrice = Integer.parseInt(editCityPrice.getText().toString().trim());
            int cityTax = Integer.parseInt(editCityTax.getText().toString().trim());
            int houseCost = Integer.parseInt(editHouseCost.getText().toString().trim());
            int houseTax = Integer.parseInt(editHouseTax.getText().toString().trim());
            int hotelCost = Integer.parseInt(editHotelCost.getText().toString().trim());
            int hotelTax = Integer.parseInt(editHotelTax.getText().toString().trim());

            // Create city object
            City city = new City();
            city.setName(cityName);
            city.setCost(cityPrice);
            city.setTax(cityTax);
            city.setHouse_cost(houseCost);
            city.setHouse_tax(houseTax);
            city.setHotel_cost(hotelCost);
            city.setHotel_tax(hotelTax);
            city.setColor(selectedColor);
            city.setPicture(BitMapHelper.encodeTobase64(selectedImageBitmap));

            // Here you would typically:
            // 1. Save the city to a database
            // 2. Or pass it to another activity

            // For example, to save to a database:
            //viewModel.add(city);
            saveToDatabase(city);

            // Or to pass to another activity:
            // Intent intent = new Intent(this, CityDetailsActivity.class);
            // intent.putExtra("CITY", city);
            // startActivity(intent);

            showToast("City square created successfully!");

            // Optionally clear the form
            clearForm();
            finish();

        } catch (NumberFormatException e) {
            showToast("Please enter valid numeric values");
        }
    }

    private void saveToDatabase(City city) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Cities")
                .whereEqualTo("name", city.getName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {

                        db.collection("Cities").add(city)
                                .addOnSuccessListener(documentReference -> {
                                    String docId = documentReference.getId();
                                    city.setIdFs(docId);
                                    documentReference.update("idFs", docId);
                                    Toast.makeText(square_creation.this, "City creation successful", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(square_creation.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Username already exists
                        Toast.makeText(square_creation.this, "name already taken", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(square_creation.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearForm() {
        // Clear all input fields
        editCityName.setText("");
        editCityPrice.setText("");
        editCityTax.setText("");
        editHouseCost.setText("");
        editHouseTax.setText("");
        editHotelCost.setText("");
        editHotelTax.setText("");

        // Reset image
        selectedImageBitmap = null;
        imgCityPreview.setImageResource(R.drawable.ic_city);
        imgPreviewCity.setImageResource(R.drawable.ic_city);

        // Reset preview
        initPreview();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setViewModel() {
        viewModel = new ViewModelProvider(this).get(CityViewModel.class);

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

    public static Bitmap vectorToBitmap(Context context, int vectorResId, int width, int height) {
        VectorDrawableCompat vectorDrawable = VectorDrawableCompat.create(
                context.getResources(), vectorResId, null);

        if (vectorDrawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
            return bitmap;
        }
        return null;
    }
}
