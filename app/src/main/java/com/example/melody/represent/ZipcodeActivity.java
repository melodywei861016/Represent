package com.example.melody.represent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ZipcodeActivity extends AppCompatActivity {

    static final String GEOCODIO_API_KEY = "9602550464bcd46a42b50bb5cf2bb5dc8daba5f";
    static final String GEOCODIO_API_URL = "https://api.geocod.io/v1.3/";
    private String zipcode = "";
    private int senator_num = 0;
    private ArrayList<JSONObject> senators = new ArrayList<JSONObject>();
    private ArrayList<JSONObject> reps = new ArrayList<JSONObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legislators_listview);
        new zipcodeToDistrict().execute();

    }

    class zipcodeToDistrict extends AsyncTask<Void, Void, String> {

        private Exception exception;
        private Context mContext = getApplicationContext();

        protected String doInBackground(Void... urls) {
            try {
                URL url;
                Bundle bundle = getIntent().getExtras();
                if (bundle.getString("zipcode_string") != null) {
                    zipcode = bundle.getString("zipcode_string");
                    url = new URL(GEOCODIO_API_URL + "geocode?q=" + zipcode + "&fields=cd&api_key=" + GEOCODIO_API_KEY);
                } else {
                    Log.e("ERROR", "Didn't input valid zipcode");
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
            Log.i("PostExecute", "Success");

            try {
                JSONObject obj = new JSONObject(response);
                JSONArray results = obj.getJSONArray("results");
                JSONObject fields = results.getJSONObject(0).getJSONObject("fields");
                JSONArray congressional_district = fields.getJSONArray("congressional_districts");
                for (int i = 0; i < congressional_district.length(); i++) {
                    JSONObject district_array = congressional_district.getJSONObject(i);
                    Log.i("district", district_array.getString("name"));
                    JSONArray legislators = district_array.getJSONArray("current_legislators");
                    Log.i("legislators", legislators.toString());
                    for (int j = 0; j < legislators.length(); j++) {
                        JSONObject legislator = legislators.getJSONObject(j);
                        Log.i("type string", legislator.getString("type"));
                        if (legislator.getString("type").equals("senator")) {
                            if (senator_num < 2 && !senators.contains(legislator)) {
                                senator_num++;
                                senators.add(legislator);
                            }
                        } else if (legislator.getString("type").equals("representative") && !reps.contains(legislator)) {
                            reps.add(legislator);
                        }
                    }
                }
                TextView search_results_text = findViewById(R.id.search_results_text);
                search_results_text.setText("Search Results by Zipcode " + zipcode + ":");
                LinearLayout linearlayout_senator = findViewById(R.id.linearlayout_senators);

                //SENATORS

                for (JSONObject senator: senators) {
                    //CardView
                    CardView legislator_view = new CardView(mContext);
                    linearlayout_senator.addView(legislator_view);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1400, 500);
                    layoutParams.setMargins(0, 30, 0, 30);
                    legislator_view.setLayoutParams(layoutParams);
                    legislator_view.setRadius(50);
                    legislator_view.setCardBackgroundColor(Color.WHITE);

                    //Senator Name
                    LinearLayout.LayoutParams text_name_layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
                    LinearLayout.LayoutParams text_info_layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
                    LinearLayout.LayoutParams image_layout = new LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.MATCH_PARENT);
                    ImageView image = new ImageView(mContext);
                    image.setLayoutParams(image_layout);
                    String bioguide_id = senator.getJSONObject("references").getString("bioguide_id");
                    final String imageUrl = "http://bioguide.congress.gov/bioguide/photo/"+ bioguide_id.substring(0, 1) +"/" + bioguide_id + ".jpg";

                    //Senator Party
                    TextView party = new TextView(mContext);
                    LinearLayout.LayoutParams senator_party_layout = new LinearLayout.LayoutParams(320, 80);
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
                            Intent intent = new Intent(ZipcodeActivity.this, DetailActivity.class);
                            intent.putExtra("type", "Senator");
                            intent.putExtra("party", partyString);
                            intent.putExtra("name", nameString);
                            intent.putExtra("imageURL", imageUrl);
                            startActivity(intent);
                        }
                    });
                }

                //REPRESENTATIVE
                LinearLayout linearlayout_rep = findViewById(R.id.linearlayout_rep);

                for (JSONObject rep: reps) {
                    //CardView
                    CardView legislator_view = new CardView(mContext);
                    linearlayout_rep.addView(legislator_view);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1400, 500);
                    layoutParams.setMargins(0, 30, 0, 30);
                    legislator_view.setLayoutParams(layoutParams);
                    legislator_view.setRadius(50);
                    legislator_view.setCardBackgroundColor(Color.WHITE);

                    //Rep Name
                    LinearLayout.LayoutParams text_name_layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
                    LinearLayout.LayoutParams text_info_layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
                    LinearLayout.LayoutParams image_layout = new LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.MATCH_PARENT);
                    ImageView image = new ImageView(mContext);
                    image.setLayoutParams(image_layout);
                    String bioguide_id = rep.getJSONObject("references").getString("bioguide_id");
                    final String imageUrl = "http://bioguide.congress.gov/bioguide/photo/" + bioguide_id.substring(0, 1) + "/" + bioguide_id + ".jpg";

                    //Rep Party
                    TextView party = new TextView(mContext);
                    LinearLayout.LayoutParams rep_party_layout = new LinearLayout.LayoutParams(320, 80);
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
                            Intent intent = new Intent(ZipcodeActivity.this, DetailActivity.class);
                            intent.putExtra("type", "Representative");
                            intent.putExtra("party", partyString);
                            intent.putExtra("name", nameString);
                            intent.putExtra("imageURL", imageUrl);
                            startActivity(intent);
                        }
                    });
                }
            } catch (JSONException e) {
                Log.d("TEST", "exception");
                e.printStackTrace();
            }
        }
    }
}
