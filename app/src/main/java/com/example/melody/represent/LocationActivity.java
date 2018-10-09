package com.example.melody.represent;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.location.Location;

import com.google.android.gms.tasks.OnSuccessListener;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import java.util.ArrayList;

import android.widget.LinearLayout.LayoutParams;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.text.Html;

public class LocationActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    private String latitude;
    private String longitude;
    private int senator_num = 0;
    private boolean found_rep = false;
    private ArrayList<JSONObject> senators = new ArrayList<JSONObject>();
    private JSONObject rep = null;

    static final String GEOCODIO_API_KEY = "9602550464bcd46a42b50bb5cf2bb5dc8daba5f";
    static final String GEOCODIO_API_URL = "https://api.geocod.io/v1.3/";

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legislators_listview);
        Bundle bundle = getIntent().getExtras();
        String buttonType = bundle.getString("button");
        if (buttonType.equals("current")) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            Log.i("LocationActivity", "Inside LocationClient");
                            Log.i("LocationActivity", String.valueOf(location == null));
                            if (location != null) {
                                // Logic to handle location object
                                latitude = Double.toString(location.getLatitude());
                                longitude = Double.toString(location.getLongitude());
                                Log.i("LOCATION", location.getLatitude() + " and " + location.getLongitude());
                                new locationToDistrict().execute();
                            }
                        }
                    });
        } else if (buttonType.equals("random")) {
            latitude = bundle.getString("lat");
            longitude = bundle.getString("lon");
            Log.i("LOCATION", latitude + " and " + longitude);
            new locationToDistrict().execute();
        }
    }

    class locationToDistrict extends AsyncTask<Void, Void, String> {

        private Exception exception;
        private Context mContext = getApplicationContext();

        protected String doInBackground(Void... urls) {
            try {
                URL url;
                if (latitude != null && longitude != null) {
                    url = new URL(GEOCODIO_API_URL + "reverse?q=" + latitude + ", " + longitude + "&fields=cd&api_key=" + GEOCODIO_API_KEY);
                } else {
                    Log.e("ERROR", "Didn't input valid location in coordinates");
                    return null;
                }
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "ERROR IN POST EXECUTE";

            }
            try {
                JSONObject obj = new JSONObject(response);
                JSONArray results = obj.getJSONArray("results");
                if (results.length() == 0) {
                    Intent intent = new Intent(LocationActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                JSONObject fields = results.getJSONObject(0).getJSONObject("fields");
                JSONArray congressional_district = fields.getJSONArray("congressional_districts");
                JSONObject district_array = congressional_district.getJSONObject(0);
                JSONArray legislators = district_array.getJSONArray("current_legislators");
                Log.i("legislators", legislators.toString());
                for (int i = 0; i < legislators.length(); i++) {
                    JSONObject legislator = legislators.getJSONObject(i);
                    Log.i("type string", legislator.getString("type"));
                    if (legislator.getString("type").equals("senator")) {
                        if (senator_num < 2) {
                            senator_num++;
                            senators.add(legislator);
                        }
                    } else if (legislator.getString("type").equals("representative")) {
                        if (!found_rep) {
                            found_rep = true;
                            rep = legislator;
                        }
                    }
                }
                JSONObject addr = results.getJSONObject(0).getJSONObject("address_components");
                String city = addr.getString("city");
                String state = addr.getString("state");
                TextView search_results_text = findViewById(R.id.search_results_text);
                Bundle bundle = getIntent().getExtras();
                if (bundle.getString("button").equals("current")) {
                    search_results_text.setText("Search Results by Current Location: " + city + ", " + state);
                } else if (bundle.getString("button").equals("random")) {
                    search_results_text.setText("Search Results by Random Location: " + city + ", " + state);
                }
                LinearLayout linearlayout_senator = findViewById(R.id.linearlayout_senators);

                //SENATORS

                for (JSONObject senator: senators) {
                    //CardView
                    CardView legislator_view = new CardView(mContext);
                    linearlayout_senator.addView(legislator_view);
                    LayoutParams layoutParams = new LayoutParams(1400, 500);
                    layoutParams.setMargins(0, 30, 0, 30);
                    legislator_view.setLayoutParams(layoutParams);
                    legislator_view.setRadius(50);
                    legislator_view.setCardBackgroundColor(Color.WHITE);

                    //Senator Name
                    LayoutParams text_name_layout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    text_name_layout.setMargins(450, 30, 0, 0);
                    TextView senator_name = new TextView(mContext);
                    senator_name.setLayoutParams(text_name_layout);
                    senator_name.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
                    senator_name.setTextColor(Color.BLACK);
                    senator_name.setTextSize(25);
                    JSONObject bio = senator.getJSONObject("bio");
                    final String nameString = bio.getString("first_name") + " " + bio.getString("last_name");
                    senator_name.setText(nameString);

                    //Senator Info
                    TextView senator_info = new TextView(mContext);
                    LayoutParams text_info_layout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    text_info_layout.setMargins(450, 150, 0, 0);
                    senator_info.setLayoutParams(text_info_layout);
                    JSONObject contact = senator.getJSONObject("contact");
                    String email_website = "";
                    String contact_form = contact.getString("contact_form");
                    String website = contact.getString("url");
                    if (!contact_form.equals("null")) {
                        email_website += ("<b>Contact Form: </b>" + contact_form + "<br/>");
                    }
                    if (!website.equals("null")) {
                        email_website += ("<b>Website: </b>" + website);
                    }
                    senator_info.setText(Html.fromHtml(email_website));

                    //Senator Image
                    LayoutParams image_layout = new LayoutParams(400, LayoutParams.MATCH_PARENT);
                    ImageView image = new ImageView(mContext);
                    image.setLayoutParams(image_layout);
                    String bioguide_id = senator.getJSONObject("references").getString("bioguide_id");
                    final String imageUrl = "http://bioguide.congress.gov/bioguide/photo/"+ bioguide_id.substring(0, 1) +"/" + bioguide_id + ".jpg";

                    //Senator Party
                    TextView party = new TextView(mContext);
                    LayoutParams senator_party_layout = new LayoutParams(320, 80);
                    senator_party_layout.setMargins(1050, 400, 0, 0);
                    party.setLayoutParams(senator_party_layout);
                    party.setTextColor(Color.WHITE);
                    party.setGravity(Gravity.CENTER);
                    party.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                    final String partyString = bio.getString("party");
                    if (partyString.equals("Democrat")) {
                        party.setBackgroundDrawable(getResources().getDrawable(R.drawable.democrat));
                        party.setText("Democrat");
                    } else if (partyString.equals("Republican")) {
                        party.setBackgroundDrawable(getResources().getDrawable(R.drawable.republican));
                        party.setText("Republican");
                    } else if (partyString.equals("Independent")) {
                        party.setBackgroundDrawable(getResources().getDrawable(R.drawable.independent));
                        party.setText("Independent");
                    }
                    //Adding Views to CardView
                    new ImageDownloaderTask(image).execute(imageUrl);
                    legislator_view.addView(senator_name);
                    legislator_view.addView(senator_info);
                    legislator_view.addView(image);
                    legislator_view.addView(party);

                    legislator_view.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent(LocationActivity.this, DetailActivity.class);
                            intent.putExtra("type", "Senator");
                            intent.putExtra("party", partyString);
                            intent.putExtra("name", nameString);
                            intent.putExtra("imageURL", imageUrl);
                            startActivity(intent);
                        }
                    });
                }

                Log.i("representative", rep.getJSONObject("bio").getString("first_name") + " " + rep.getJSONObject("bio").getString("last_name"));

                //REPRESENTATIVE
                LinearLayout linearlayout_rep = findViewById(R.id.linearlayout_rep);

                //CardView
                CardView legislator_view = new CardView(mContext);
                linearlayout_rep.addView(legislator_view);
                LayoutParams layoutParams = new LayoutParams(1400, 500);
                layoutParams.setMargins(0, 30, 0, 30);
                legislator_view.setLayoutParams(layoutParams);
                legislator_view.setRadius(50);
                legislator_view.setCardBackgroundColor(Color.WHITE);

                //Rep Name
                LayoutParams text_name_layout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                text_name_layout.setMargins(450, 30, 0, 0);
                TextView rep_name = new TextView(mContext);
                rep_name.setLayoutParams(text_name_layout);
                rep_name.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
                rep_name.setTextColor(Color.BLACK);
                rep_name.setTextSize(25);
                JSONObject bio = rep.getJSONObject("bio");
                final String nameString = bio.getString("first_name") + " " + bio.getString("last_name");
                rep_name.setText(nameString);

                //Rep Info
                TextView rep_info = new TextView(mContext);
                LayoutParams text_info_layout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                text_info_layout.setMargins(450, 150, 0, 0);
                rep_info.setLayoutParams(text_info_layout);
                JSONObject contact = rep.getJSONObject("contact");
                String email_website = "";
                String contact_form = contact.getString("contact_form");
                String website = contact.getString("url");
                if (!contact_form.equals("null")) {
                    email_website += ("<b>Contact Form: </b>" + contact_form + "<br/>");
                }
                if (!website.equals("null")) {
                    email_website += ("<b>Website: </b>" + website);
                }
                rep_info.setText(Html.fromHtml(email_website));

                //Rep Image
                LayoutParams image_layout = new LayoutParams(400, LayoutParams.MATCH_PARENT);
                ImageView image = new ImageView(mContext);
                image.setLayoutParams(image_layout);
                String bioguide_id = rep.getJSONObject("references").getString("bioguide_id");
                final String imageUrl = "http://bioguide.congress.gov/bioguide/photo/"+ bioguide_id.substring(0, 1) +"/" + bioguide_id + ".jpg";

                //Rep Party
                TextView party = new TextView(mContext);
                LayoutParams rep_party_layout = new LayoutParams(320, 80);
                rep_party_layout.setMargins(1050, 400, 0, 0);
                party.setLayoutParams(rep_party_layout);
                party.setTextColor(Color.WHITE);
                party.setGravity(Gravity.CENTER);
                party.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                final String partyString = bio.getString("party");
                if (partyString.equals("Democrat")) {
                    party.setBackgroundDrawable(getResources().getDrawable(R.drawable.democrat));
                    party.setText("Democrat");
                } else if (partyString.equals("Republican")) {
                    party.setBackgroundDrawable(getResources().getDrawable(R.drawable.republican));
                    party.setText("Republican");
                } else if (partyString.equals("Independent")) {
                    party.setBackgroundDrawable(getResources().getDrawable(R.drawable.independent));
                    party.setText("Independent");
                }
                //Adding Views to CardView
                new ImageDownloaderTask(image).execute(imageUrl);
                legislator_view.addView(rep_name);
                legislator_view.addView(rep_info);
                legislator_view.addView(image);
                legislator_view.addView(party);

                legislator_view.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(LocationActivity.this, DetailActivity.class);
                        intent.putExtra("type", "Representative");
                        intent.putExtra("party", partyString);
                        intent.putExtra("name", nameString);
                        intent.putExtra("imageURL", imageUrl);
                        startActivity(intent);
                    }
                });

            } catch (JSONException e) {
                Log.d("TEST", "exception");
                e.printStackTrace();
            }
        }
    }

}
