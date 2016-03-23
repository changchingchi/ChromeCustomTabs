// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.chromium.customtabsdemos;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * The simplest way to use Chrome Custom Tabs. Without any customization or speeding process.
 */
public class SimpleCustomTabActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mUrlEditText;
    private String mApiUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_custom_tab);

        findViewById(R.id.start_custom_tab).setOnClickListener(this);

        new POSTRequest().execute();
        mUrlEditText = (EditText)findViewById(R.id.url);


    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch (viewId) {
            case R.id.start_custom_tab:
                String url = mUrlEditText.getText().toString();
                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
                CustomTabActivityHelper.openCustomTab(
                        this, customTabsIntent, Uri.parse(url), new WebviewFallback());
                break;
            default:
                //Unknown View Clicked
        }
    }



    private class POSTRequest extends AsyncTask<Void,Void,Void>{

        private HashMap<String,String> mPostDataMap;
        private HashMap<String,String> mRequestHeadersMap;
        private HashMap<String,String> mRequestHeadersMap2;
        private String paymentURL;
        private JSONObject mJsonObject;
        String jsonString = "{\"intent\": \"sale\",\"redirect_urls\": {\"return_url\": \"myscheme://myhost.paypal.com\",\"cancel_url\": \"myscheme://myhost.paypal.com\"},\"payer\":{\"payment_method\": \"paypal\"},\"transactions\": [{\"amount\": {\"total\": \"7.47\",\"currency\": \"USD\"}}]}";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPostDataMap = new HashMap<>();
            mRequestHeadersMap = new HashMap<>();
            mRequestHeadersMap2 = new HashMap<>();

            mPostDataMap.put("grant_type","client_credentials");
            jsonString.replace("\\","");


        }

        @Override
        protected Void doInBackground(Void... params) {
            String responseString = performPostCall("https://api.sandbox.paypal.com/v1/oauth2/token",mPostDataMap);

            try{
                JSONObject jsonObject = new JSONObject(responseString);
                String access_token = jsonObject.getString("access_token");
                Log.d("============",access_token);
                String responseString2 = performPostCallJSON("https://api.sandbox.paypal.com/v1/payments/payment",jsonString,access_token);

                JSONArray links = new JSONObject(responseString2).getJSONArray("links");
                paymentURL = links.getJSONObject(1).getString("href");
            }catch (JSONException e){
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            String token =paymentURL.substring(paymentURL.lastIndexOf("=")+1);
            Toast.makeText(getApplicationContext(),"token is "+token,Toast.LENGTH_LONG).show();
            mUrlEditText.setText(paymentURL);

        }

        public String  performPostCall(String requestURL,
                                       HashMap<String, String> postDataParams) {

            URL url;
            String response = "";
            try {
                url = new URL(requestURL);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                addHeaders();
                Iterator mItr = mRequestHeadersMap.entrySet().iterator();
                while (mItr.hasNext()) {
                    Map.Entry pair = (Map.Entry)mItr.next();

                    conn.setRequestProperty(pair.getKey().toString(),pair.getValue().toString());
                    mItr.remove(); // avoids a ConcurrentModificationException
                }

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";
                }
            } catch (Exception e) {
                Log.d("aaa",e.toString());
            }

            return response;
        }


        public String  performPostCallJSON(String requestURL,
                                      String postDataParams,String accesstoken) {

            URL url;
            String response = "";
            try {
                url = new URL(requestURL);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                mRequestHeadersMap2.put("Authorization","Bearer "+accesstoken);
                mRequestHeadersMap2.put("Content-Type","application/json");
                Iterator mItr = mRequestHeadersMap2.entrySet().iterator();
                while (mItr.hasNext()) {
                    Map.Entry pair = (Map.Entry)mItr.next();

                    conn.setRequestProperty(pair.getKey().toString(),pair.getValue().toString());
                    mItr.remove(); // avoids a ConcurrentModificationException
                }

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postDataParams);

                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK || responseCode == HttpsURLConnection.HTTP_CREATED) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";
                }
            } catch (Exception e) {
                Log.d("aaa",e.toString());
            }

            return response;
        }

        private void addHeaders() {
            mRequestHeadersMap.put("Accept", "application/json");
            mRequestHeadersMap.put("Accept-Language","en_US");
            mRequestHeadersMap.put("Authorization", "Basic RU9KMlMtWjZPb05fbGVfS1MxZDc1d3NaNnkwU0ZkVnNZOTE4M0l2eEZ5WnA6RUNsdXNNRVVrOGU5aWhJN1pkVkxGNWNaNnkwU0ZkVnNZOTE4M0l2eEZ5WnA=");
            mRequestHeadersMap.put("content-type", "application/x-www-form-urlencoded");
        }
        private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : params.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            return result.toString();
        }

    }
}


