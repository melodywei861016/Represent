package com.example.melody.represent;

import android.view.View;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Search by Current Location
        Button search_current_location = findViewById(R.id.search_by_current_location);
        search_current_location.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocationActivity.class);
                intent.putExtra("button", "current");
                startActivity(intent);
            }
        });

        //Search by Zipcode
        Button search_by_zipcode = findViewById(R.id.search_by_zipcode);
        search_by_zipcode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("zipcode", "click detected");
                EditText zipcode_input = findViewById(R.id.zipcode_input);
                String zipcode_string = zipcode_input.getText().toString();
                Log.i("zipcode", zipcode_string);
                if (zipcode_string.equals("") || zipcode_string.length() != 5) {
                    Log.i("zipcode", "invalid zipcode");
                    Toast.makeText(getApplicationContext(), "Invalid input for Zipcode", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainActivity.this, ZipcodeActivity.class);
                    intent.putExtra("zipcode_string", zipcode_string);
                    startActivity(intent);
                }
            }
        });

        Button search_by_random_location = findViewById(R.id.search_by_random_location);
        search_by_random_location.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocationActivity.class);

                double lat = 24.7433195 + Math.random() * (49.3457868 - 24.7433195);
                double lon = -(66.9513812 + Math.random() * (124.7844079 - 66.9513812));
                intent.putExtra("lat", Double.toString(lat));
                intent.putExtra("lon", Double.toString(lon));
                intent.putExtra("button", "random");
                startActivity(intent);
            }
        });
    }
}
