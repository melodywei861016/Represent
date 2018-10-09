package com.example.melody.represent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ImageView legislator_image = findViewById(R.id.legislator_image);
        Bundle bundle = getIntent().getExtras();
        new ImageDownloaderTask(legislator_image).execute(bundle.getString("imageURL"));
        TextView legislator_type = findViewById(R.id.legislator_type);
        String typeString = bundle.getString("type");
        if (typeString.equals("Representative")) {
            legislator_type.setText("Representative");
        } else if (typeString.equals("Senator")) {
            legislator_type.setText("Senator");
        }
        TextView legislator_party = findViewById(R.id.legislator_party);
        String partyString = bundle.getString("party");
        if (partyString.equals("Republican")) {
            legislator_party.setBackgroundDrawable(getResources().getDrawable(R.drawable.republican));
            legislator_party.setText("Republican");
        } else if (partyString.equals("Democrat")) {
            legislator_party.setBackgroundDrawable(getResources().getDrawable(R.drawable.democrat));
            legislator_party.setText("Democrat");
        } else if (partyString.equals("Independent")) {
            legislator_party.setBackgroundDrawable(getResources().getDrawable(R.drawable.independent));
            legislator_party.setText("Independent");
        }

        TextView legislator_name = findViewById(R.id.legislator_name);
        String nameString = bundle.getString("name");
        legislator_name.setText(nameString);
    }
}
