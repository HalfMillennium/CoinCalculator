package com.coincalc.anduril.coincalculator;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

        private TextView finalValUSD, finalValEUR;
        private Button calculate;

        private EditText amount, fees;
        private boolean bit = false, eth = false, lit = false;
        String amt, fee, dollar, res;
        String coin = "BTC";
        double fin, modFin;

        private AdView mAdView;
        private InterstitialAd mInterstitialAd;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            finalValUSD = (TextView) findViewById(R.id.profit);
            finalValEUR = (TextView) findViewById(R.id.euro);
            amount = (EditText) findViewById(R.id.amount);
            fees = (EditText) findViewById(R.id.fee);
            calculate = (Button) findViewById(R.id.calc);

            calculate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    beginTask(coin);
                }
            });

            BottomNavigationView navigation = findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            navigation.setBackgroundColor(Color.parseColor("#333333"));


            MobileAds.initialize(this, "ca-app-pub-9868582549220946~5567174224");

            mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);

            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId("ca-app-pub-9868582549220946/3009008232");

        }

        private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_bit:
                        Log.d("btc", "btc");
                        setBTC();
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                        if(mInterstitialAd.isLoaded())
                            mInterstitialAd.show();
                        else
                            Log.d("ad", "not yet loaded");
                        return true;
                    case R.id.navigation_eth:
                        Log.d("eth", "eth");
                        setETH();
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                        if(mInterstitialAd.isLoaded())
                            mInterstitialAd.show();
                        else
                            Log.d("ad", "not yet loaded");
                        return true;
                    case R.id.navigation_lite:
                        Log.d("ltc", "ltc");
                        setLTC();
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());
                        if(mInterstitialAd.isLoaded())
                            mInterstitialAd.show();
                        else
                            Log.d("ad", "not yet loaded");
                        return true;
                }
                return false;
            }
        };


        private class JsonTask extends AsyncTask<String, String, String> {

            protected void onPreExecute() {
                super.onPreExecute();
            }

            protected String doInBackground(String... params) {


                HttpURLConnection connection = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(params[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();


                    InputStream stream = connection.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();
                    String line = "";

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line+"\n");
                        Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                    }

                    return buffer.toString();


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                try {
                    JSONObject obj = new JSONObject(result);
                    fee = fees.getText().toString();
                    Log.d("check", fee);

                    fin = Double.parseDouble(amount.getText().toString()) * Double.parseDouble(obj.get("USD").toString());
                    fin = Math.round(fin * 100.0) / 100.0;

                    if (fee.equals(null) || fee.length() == 0) {

                        Log.d("fin", "" + fin);

                        finalValUSD.setText("$" + fin);
                        finalValEUR.setText("€" + (Math.round((fin * 0.81) * 100.0) / 100.0));

                    } else {
                        if (Double.parseDouble(fee) < 1) {
                            double actFee = Double.parseDouble(fee);

                            double fract = 1 - actFee;

                            Log.d("fract:", "" + fract);

                            modFin = Math.round((fin * fract) * 100.0) / 100.0;

                            Log.d("mod", "" + Math.round((fin * fract) * 100.0) / 100.0);

                            finalValUSD.setText("$" + modFin);
                            finalValEUR.setText("€" + (Math.round((modFin * 0.81) * 100.0) / 100.0));
                        } else {
                            double subFee = Double.parseDouble(fee);

                            modFin = fin - subFee;
                            modFin = Math.round(modFin * 100.0) / 100.0;

                            finalValUSD.setText("$" + modFin);
                            finalValEUR.setText("€" + (Math.round((modFin * 0.81) * 100.0) / 100.0));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void beginTask(String coin)
        {
            String str = amount.getText().toString();
            BottomNavigationView nav = findViewById(R.id.navigation);

            if(!str.replaceAll(" ", "").equals("")) {
                if (nav.getSelectedItemId() == R.id.navigation_bit) {
                    coin = "BTC";
                } else if (nav.getSelectedItemId() == R.id.navigation_eth) {
                    coin = "ETH";
                } else {
                    coin = "LTC";
                }

                if (!(str.equals(null))) {
                    Log.d("url", "https://min-api.cryptocompare.com/data/price?fsym=" + coin + "&tsyms=USD");
                    new JsonTask().execute("https://min-api.cryptocompare.com/data/price?fsym=" + coin + "&tsyms=USD");
                } else {
                    Toast.makeText(this, "Please enter a valid amount!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        public void setBTC()
        {
            EditText amt = (EditText) findViewById(R.id.amount);
            amt.setBackground(getResources().getDrawable(R.drawable.bitcoin_textfield_xml));
            amt.setText("");

            TextView fin = (TextView) findViewById(R.id.profit);
            fin.setBackground(getResources().getDrawable(R.drawable.bitcoin_profit));
            fin.setText("$0");

            TextView amount = (TextView) findViewById(R.id.amt_text);
            amount.setTextColor(Color.parseColor("#F8A33D"));

            Button calc = (Button) findViewById(R.id.calc);
            calc.setBackground(getResources().getDrawable(R.drawable.bitcoin_textfield_xml));
            calc.setTextColor(Color.parseColor("#F8A33D"));

            ImageView logo = (ImageView) findViewById(R.id.logo);
            logo.setImageDrawable(getResources().getDrawable(R.drawable.bitcoin_png48));
        }

        public void setETH()
        {
            EditText amt = (EditText) findViewById(R.id.amount);
            amt.setBackground(getResources().getDrawable(R.drawable.ethereum_textfield));
            amt.setText("");

            TextView fin = (TextView) findViewById(R.id.profit);
            fin.setBackground(getResources().getDrawable(R.drawable.ethereum_profit));
            fin.setText("$0");

            TextView amount = (TextView) findViewById(R.id.amt_text);
            amount.setTextColor(Color.parseColor("#0B8311"));

            Button calc = (Button) findViewById(R.id.calc);
            calc.setBackground(getResources().getDrawable(R.drawable.ethereum_textfield));
            calc.setTextColor(Color.parseColor("#0B8311"));

            ImageView logo = (ImageView) findViewById(R.id.logo);
            logo.setImageDrawable(getResources().getDrawable(R.drawable.buy_ethereum));
        }

        public void setLTC()
        {
            EditText amt = (EditText) findViewById(R.id.amount);
            amt.setBackground(getResources().getDrawable(R.drawable.litecoin_textfield));
            amt.setText("");

            TextView fin = (TextView) findViewById(R.id.profit);
            fin.setBackground(getResources().getDrawable(R.drawable.litecoin_profit));
            fin.setText("$0");

            TextView amount = (TextView) findViewById(R.id.amt_text);
            amount.setTextColor(Color.parseColor("#BEBEBE"));

            Button calc = (Button) findViewById(R.id.calc);
            calc.setBackground(getResources().getDrawable(R.drawable.litecoin_textfield));
            calc.setTextColor(Color.parseColor("#BEBEBE"));

            ImageView logo = (ImageView) findViewById(R.id.logo);
            logo.setImageDrawable(getResources().getDrawable(R.drawable.litecoin));
        }
    }
