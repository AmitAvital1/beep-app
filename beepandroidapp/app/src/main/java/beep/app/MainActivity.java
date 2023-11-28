package beep.app;

import static beep.app.util.Constants.LOGIN;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.style.BackgroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import beep.app.login.RegionAdapter;
import beep.app.login.RegionItem;
import beep.app.login.Regions;
import beep.app.util.http.HttpClientUtil;
import login.UserDTO;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerRegion;
    private EditText phoneNumber;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerRegion = findViewById(R.id.RegionSpinner);
        phoneNumber = findViewById(R.id.EditTextPhoneNumber);
        loginButton = findViewById(R.id.loginButton);

        List<RegionItem> regionItems = Regions.ALL_REGIONS;
        RegionAdapter adapter = new RegionAdapter(this, regionItems);
        spinnerRegion.setAdapter(adapter);

        loginButton.setOnClickListener(button -> {
            loginClickListener(phoneNumber,(RegionItem)spinnerRegion.getSelectedItem());
        });


    }

    private void loginClickListener(EditText phoneNumber,RegionItem phoneRegion) {
        if(isValidPhoneNumber(phoneNumber.getText().toString())){
            callLoginApi(phoneNumber, phoneRegion);

        }else{
            phoneNumber.setBackgroundResource(R.drawable.edit_text_error);
            phoneNumber.setError("Invalid phone number");
        }
    }

    private void callLoginApi(EditText phoneNumber, RegionItem phoneRegion) {
        String finalUrl = HttpUrl
                .parse(LOGIN)
                .newBuilder()
                .build()
                .toString();

        Gson gson = new Gson();
        Integer regionNumber = new Integer(phoneRegion.getRegionNumber());
        String json = gson.toJson(new UserDTO(phoneRegion.getPrefix(), phoneRegion.getPrefix() + phoneNumber.getText().toString()));
        RequestBody requestBody = RequestBody.create(json,MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(finalUrl)
                .put(requestBody)
                .build();
        HttpClientUtil.runAsync(request, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(() -> {
                    System.out.println("got failed " + e.toString());
                    phoneNumber.setBackgroundResource(R.drawable.edit_text_error);
                    phoneNumber.setError("Invalid phone number");
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.code() == 200) { //Phone exist
                    runOnUiThread(() -> {
                        phoneNumber.setBackgroundResource(R.drawable.edit_text_valid);
                        UserDTO userDTO = gson.fromJson(responseBody, UserDTO.class);
                        Intent intent = new Intent(MainActivity.this, VerificationCodeActivity.class);
                        intent.putExtra("userDTO", userDTO);
                        startActivity(intent);
                    });
                }
            }
        });
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Use a regular expression to check if the phone number contains only digits
        // and has a length of 9 digits
        String regex = "^[0-9]{9}$";
        return Pattern.matches(regex, phoneNumber);
    }
}