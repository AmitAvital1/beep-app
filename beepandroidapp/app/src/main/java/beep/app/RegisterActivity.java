package beep.app;

import static beep.app.util.Constants.CODE;
import static beep.app.util.Constants.REGISTER;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import beep.app.util.http.HttpClientUtil;
import login.UserDTO;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class  RegisterActivity extends AppCompatActivity {

    private UserDTO userDTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        userDTO = (UserDTO) getIntent().getSerializableExtra("userDTO");

        Button completeAction = findViewById(R.id.completeButton);
        completeAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeAction();
            }
        });
    }
    public void completeAction() {
        EditText firstNameEditText = findViewById(R.id.firstNameEditText);
        EditText lastNameEditText = findViewById(R.id.lastNameEditText);

        firstNameEditText.setBackgroundResource(R.drawable.edit_text_valid);
        lastNameEditText.setBackgroundResource(R.drawable.edit_text_valid);
        firstNameEditText.setError(null);
        lastNameEditText.setError(null);

        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        userDTO.setFirstName(firstName);
        userDTO.setLastName(lastName);

        String finalUrl = HttpUrl
                .parse(REGISTER)
                .newBuilder()
                .build()
                .toString();

        Gson gson = new Gson();
        String json = gson.toJson(userDTO);
        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(finalUrl)
                .put(requestBody)
                .build();
        HttpClientUtil.runAsync(request, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> {
                    firstNameEditText.setBackgroundResource(R.drawable.edit_text_error);
                    lastNameEditText.setBackgroundResource(R.drawable.edit_text_error);
                    firstNameEditText.setError("Error");
                    lastNameEditText.setError("Error");
                    System.out.println(e.toString());
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.code() == 400) {
                    runOnUiThread(() -> {
                        firstNameEditText.setBackgroundResource(R.drawable.edit_text_error);
                        firstNameEditText.setError("Invalid First Name");
                    });
                }else if(response.code() == 406) { //Register
                    runOnUiThread(() -> {
                        lastNameEditText.setBackgroundResource(R.drawable.edit_text_error);
                        lastNameEditText.setError("Invalid Last Name");;
                    });
                }else{
                    runOnUiThread(() -> {
                        UserDTO userDTO = gson.fromJson(responseBody, UserDTO.class);
                        Intent intent = new Intent(RegisterActivity.this, MainScreenActivity.class);
                        intent.putExtra("userDTO", userDTO);
                        startActivity(intent);
                    });
                }

            }
        });
    }
}