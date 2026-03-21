package com.example.firstcourse;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An activity to control an ESP32 LED over a local Wi-Fi network.
 * Includes a mock mode for testing without a real ESP32 connection.
 */
public class LedControlActivity extends AppCompatActivity {

    // --- MOCK DATA FLAG ---
    // Set this to true to use mock responses for testing without a real ESP32.
    // Set this to false to send actual HTTP requests to the ESP32.
    private static final boolean USE_MOCK_DATA = true;

    // ESP32 Network Configuration
    private static final String ESP32_IP = "192.168.4.1";
    private static final String API_URL = "http://" + ESP32_IP + "/api/cmd?val=";

    // UI Elements
    private Button buttonLedOn, buttonLedOff, buttonLedStatus;
    private TextView textViewResult;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);

        buttonLedOn = findViewById(R.id.button_led_on);
        buttonLedOff = findViewById(R.id.button_led_off);
        buttonLedStatus = findViewById(R.id.button_led_status);
        textViewResult = findViewById(R.id.textview_result);

        buttonLedOn.setOnClickListener(v -> sendCommand(1));
        buttonLedOff.setOnClickListener(v -> sendCommand(2));
        buttonLedStatus.setOnClickListener(v -> sendCommand(3));
    }

    /**
     * Sends a command to the ESP32. If USE_MOCK_DATA is true, it simulates a response.
     * Otherwise, it sends a real HTTP GET request on a background thread.
     * @param commandValue The integer value for the 'val' parameter.
     */
    private void sendCommand(int commandValue) {
        textViewResult.setText("Sending command...");

        executor.execute(() -> {
            String resultMessage;

            if (USE_MOCK_DATA) {
                // Simulate a network delay and provide a mock success response
                try {
                    Thread.sleep(500); // 0.5 second delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                resultMessage = "Success! (Mock) Response: OK";
            } else {
                // --- REAL NETWORK LOGIC ---
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL(API_URL + commandValue);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        String response = in.readLine();
                        in.close();
                        resultMessage = "Success! Response: " + response;
                    } else {
                        resultMessage = "Error: Server returned code " + responseCode;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    resultMessage = "Error: " + e.getMessage();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }

            // Update UI on the main thread
            String finalResultMessage = resultMessage;
            handler.post(() -> textViewResult.setText(finalResultMessage));
        });
    }
}
