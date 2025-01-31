package es.riberadeltajo.ceca_guillermoimdbapp.ui.slideshow;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;


import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import es.riberadeltajo.ceca_guillermoimdbapp.LocationActivity;
import es.riberadeltajo.ceca_guillermoimdbapp.MainActivity;
import es.riberadeltajo.ceca_guillermoimdbapp.R;
import es.riberadeltajo.ceca_guillermoimdbapp.database.FavoritesDatabaseHelper;
import es.riberadeltajo.ceca_guillermoimdbapp.models.KeyStoreManager;

public class EditUserFragment extends Fragment {

    private EditText editTextName, editTextEmail, editTextAddress, editTextPhone;
    private ImageView imageViewProfile;
    private String address;
    private Uri selectedImageUri;
    private Button buttonSelectAddress, buttonSelectImage, buttonSave;
    private FirebaseAuth auth;
    private FavoritesDatabaseHelper favoritesDatabaseHelper;
    private KeyStoreManager keyStoreManager;

    private ActivityResultLauncher<Intent> locationLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_user, container, false);

        // Inicializar vistas
        editTextName = view.findViewById(R.id.editTextName);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        editTextAddress = view.findViewById(R.id.editTextAddress);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        imageViewProfile = view.findViewById(R.id.imageViewProfile);
        buttonSelectAddress = view.findViewById(R.id.buttonSelectAddress);
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage);
        buttonSave = view.findViewById(R.id.buttonSave);
        keyStoreManager = new KeyStoreManager(requireContext());
        auth = FirebaseAuth.getInstance();
        favoritesDatabaseHelper = new FavoritesDatabaseHelper(requireContext());


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            editTextName.setText(user.getDisplayName());
            editTextEmail.setText(user.getEmail());
        }

        Uri savedUri = loadProfileImageUri();
        if (savedUri != null) {
            Glide.with(this).load(savedUri).into(imageViewProfile);
        }

        locationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        String selectedAddress = result.getData().getStringExtra("SELECTED_ADDRESS");
                        if (selectedAddress != null) {
                            editTextAddress.setText(selectedAddress); // Mostrar la dirección en el EditText
                        }
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        Glide.with(this).load(selectedImageUri).into(imageViewProfile);
                        saveProfileImageUri(selectedImageUri);
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap photo = (Bitmap) extras.get("data");
                            if (photo != null) {
                                Uri photoUri = saveBitmapToFile(photo);
                                Glide.with(this).load(photoUri).into(imageViewProfile);
                                saveProfileImageUri(photoUri);
                            }
                        }
                    }
                }
        );



        buttonSelectAddress.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 201);
            } else {
                Intent intent = new Intent(requireContext(), LocationActivity.class);
                locationLauncher.launch(intent);
            }
        });
        buttonSelectImage.setOnClickListener(v -> {
            // Crear opciones para el diálogo
            String[] options = {"Tomar foto", "Seleccionar de galería", "Usar URL externa"};

            // Crear el AlertDialog
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Seleccionar imagen")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0: // Tomar foto con la cámara
                                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                cameraLauncher.launch(cameraIntent);
                                break;

                            case 1: // Seleccionar de la galería
                                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(galleryIntent, 101);
                                break;

                            case 2: // Usar URL externa
                                mostrarDialogoUrl();
                                break;
                        }
                    })
                    .show();
        });



        CountryCodePicker ccp = view.findViewById(R.id.countryCodePicker);

        ccp.setOnCountryChangeListener(() -> {
            String selectedCountry = ccp.getSelectedCountryName();
            String selectedCode = ccp.getSelectedCountryCodeWithPlus();
            Toast.makeText(requireContext(), "Seleccionado: " + selectedCountry + " (" + selectedCode + ")", Toast.LENGTH_SHORT).show();

            saveSelectedCountryCode(selectedCode, selectedCountry);
            
        });

        loadSelectedCountryCode(ccp);

        buttonSave.setOnClickListener(v -> {
            String phoneNumber = editTextPhone.getText().toString().trim();
            String countryCode = ccp.getSelectedCountryCode();
            String fullPhone = "+" + countryCode + phoneNumber;
            Uri image = loadProfileImageUri();

            if (!isValidPhoneNumber(fullPhone, ccp.getSelectedCountryNameCode())) {
                Toast.makeText(requireContext(), "Número de teléfono inválido para el país seleccionado", Toast.LENGTH_SHORT).show();
                return;
            }

            String address = editTextAddress.getText().toString().trim();
            String name = editTextName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            String encryptedPhone = keyStoreManager.encrypt(phoneNumber);
            String encryptedAddress = keyStoreManager.encrypt(address);

            saveUserProfile();

            FirebaseUser user1 = auth.getCurrentUser();
            if (user != null) {
                favoritesDatabaseHelper.insertOrUpdateUser(
                        user1.getUid(),
                        name,
                        editTextEmail.getText().toString().trim(),
                        getCurrentTimestamp(),
                        null,
                        encryptedPhone,
                        encryptedAddress,
                        image != null ? image.toString() : null
                );

                // LOG PARA DEPURAR
                System.out.println("Datos guardados: ");
                System.out.println("Name: " + name);
                System.out.println("Phone: " + phoneNumber);
                System.out.println("Address: " + address);
            } else {
                Toast.makeText(requireContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);
        });



        return view;
    }

    private void abrirCamara() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(requireContext().getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(requireContext(), "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveSelectedCountryCode(String code, String country) {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("country_code", code)
                .putString("country_name", country)
                .apply();
    }

    private void loadSelectedCountryCode(CountryCodePicker ccp) {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        String savedCode = prefs.getString("country_code", null);
        if (savedCode != null) {
            ccp.setCountryForPhoneCode(Integer.parseInt(savedCode.replace("+", "")));
        }
    }

    private void mostrarDialogoUrl() {
        EditText editTextUrl = new EditText(requireContext());
        editTextUrl.setHint("https://");

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Ingresar URL de imagen")
                .setView(editTextUrl)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    String url = editTextUrl.getText().toString().trim();
                    if (!url.isEmpty()) {
                        Uri imageUri = Uri.parse(url);
                        Glide.with(this).load(imageUri).into(imageViewProfile);
                        saveProfileImageUri(imageUri); // Guardar la imagen
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK && data != null) {
            if (requestCode == 101) { // Imagen seleccionada de la galería
                Uri selectedImageUri = data.getData();
                Glide.with(this).load(selectedImageUri).into(imageViewProfile);

                // Guardar la URI de la imagen
                saveProfileImageUri(selectedImageUri);

            } else if (requestCode == 102) { // Imagen capturada con la cámara
                Bundle extras = data.getExtras();
                Bitmap photo = (Bitmap) extras.get("data");

                if (photo != null) {
                    // Convertir el Bitmap en un archivo Uri para guardar
                    Uri photoUri = saveBitmapToFile(photo);
                    Glide.with(this).load(photoUri).into(imageViewProfile);

                    // Guardar la URI de la imagen
                    saveProfileImageUri(photoUri);
                }
            }
        }
    }

    private Uri saveBitmapToFile(Bitmap bitmap) {
        File file = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profile_image.jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }




    private void saveProfileImageUri(Uri imageUri) {
        requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE)
                .edit()
                .putString("profile_image_uri", imageUri.toString())
                .apply();
    }

    private void saveUserProfile() {
        String name = editTextName.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (name.isEmpty()) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FavoritesDatabaseHelper dbHelper = new FavoritesDatabaseHelper(getContext());
            String userId = user.getUid();

            dbHelper.insertOrUpdateUser(userId, name, user.getEmail(), getCurrentTimestamp(), null, phone, address, null);

            if (isAdded()) {
                SharedPreferences prefs = requireContext().getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
                prefs.edit().putString("nombre", name).apply();

                Toast.makeText(requireContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
            }

            if (isAdded()) {
                requireActivity().onBackPressed();
            }
        } else {
            if (isAdded()) {
                Toast.makeText(requireContext(), "No se pudo guardar el perfil. Usuario no autenticado.", Toast.LENGTH_SHORT).show();
            }
        }
    }





    private Uri loadProfileImageUri() {
        SharedPreferences prefs = requireContext().getSharedPreferences("imagen", Context.MODE_PRIVATE);
        String uriString = prefs.getString("imagen", null);
        return uriString != null ? Uri.parse(uriString) : null;
    }
    private boolean isValidPhoneNumber(String fullPhone, String selectedCountryCode) {
        if (TextUtils.isEmpty(fullPhone) || TextUtils.isEmpty(selectedCountryCode)) {
            return false;
        }

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(fullPhone, selectedCountryCode);
            return phoneNumberUtil.isValidNumberForRegion(phoneNumber, selectedCountryCode);
        } catch (NumberParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }


}
