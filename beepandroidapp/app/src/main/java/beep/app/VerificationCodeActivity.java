package beep.app;

import static beep.app.util.Constants.CODE;
import static beep.app.util.Constants.LOGIN;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import beep.app.R;
import beep.app.util.http.HttpClientUtil;
import login.UserDTO;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VerificationCodeActivity extends AppCompatActivity {

    private EditText editTextDigit1;
    private EditText editTextDigit2;
    private EditText editTextDigit3;
    private EditText editTextDigit4;
    private TextView errorTextView;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);

        UserDTO userDTO = (UserDTO) getIntent().getSerializableExtra("userDTO");

        editTextDigit1 = findViewById(R.id.editTextDigit1);
        editTextDigit2 = findViewById(R.id.editTextDigit2);
        editTextDigit3 = findViewById(R.id.editTextDigit3);
        editTextDigit4 = findViewById(R.id.editTextDigit4);
        btnSubmit = findViewById(R.id.btnSubmit);
        errorTextView = findViewById(R.id.errorTextView);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String digit1 = editTextDigit1.getText().toString();
                String digit2 = editTextDigit2.getText().toString();
                String digit3 = editTextDigit3.getText().toString();
                String digit4 = editTextDigit4.getText().toString();

                String fourDigitNumber = digit1 + digit2 + digit3 + digit4;

                String finalUrl = HttpUrl
                        .parse(CODE + fourDigitNumber)
                        .newBuilder()
                        .build()
                        .toString();

                Gson gson = new Gson();
                String json = gson.toJson(userDTO);
                RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

                Request request = new Request.Builder()
                        .url(finalUrl)
                        .post(requestBody)
                        .build();
                HttpClientUtil.runAsync(request, new Callback() {

                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        runOnUiThread(() -> {
                            errorTextView.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String responseBody = response.body().string();
                        if (response.code() == 400) { //Invalid code
                            runOnUiThread(() -> {
                                errorTextView.setVisibility(View.VISIBLE);
                            });
                        }else if(response.code() == 201) { //Register
                            UserDTO userDTO = gson.fromJson(responseBody, UserDTO.class);
                            runOnUiThread(() -> {
                                Intent intent = new Intent(VerificationCodeActivity.this, RegisterActivity.class);
                                intent.putExtra("userDTO", userDTO);
                                startActivity(intent);
                            });
                        }else if(response.code() == 200) { //User exist
                            UserDTO userDTO = gson.fromJson(responseBody, UserDTO.class);
                            runOnUiThread(() -> {
                                Intent intent = new Intent(VerificationCodeActivity.this, MainScreenActivity.class);
                                intent.putExtra("userDTO", userDTO);
                                startActivity(intent);
                            });
                        }


                    }
                });

            }
        });

        // Add TextWatchers to each EditText
        addTextWatcher(editTextDigit1);
        addTextWatcher(editTextDigit2);
        addTextWatcher(editTextDigit3);
        addTextWatcher(editTextDigit4);

    }
    private void addTextWatcher(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Not needed for this example
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Not needed for this example
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Enable the button if all four EditTexts have non-empty text
                boolean enableButton = editTextDigit1.length() > 0 &&
                        editTextDigit2.length() > 0 &&
                        editTextDigit3.length() > 0 &&
                        editTextDigit4.length() > 0;

                btnSubmit.setEnabled(enableButton);
                errorTextView.setVisibility(View.INVISIBLE);
            }
        });
    }
}