package com.freel.ledcolor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class ColorPicker extends ActionBarActivity implements ColorRing.MyCallback {

    private TextView textView;

    private ColorRing ring;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        ring = (ColorRing) findViewById(R.id.ring);
        ring.registerCallBack(this);

        textView = (TextView)findViewById(R.id.urlView);
        textView.setText("url");

        sp = PreferenceManager.getDefaultSharedPreferences(this);

    }

    @Override
    public void callBackReturn() {
        String url = "http://" + sp.getString("ledServer", "192.168.1.1") + "/" + Color.red(ring.mCenterNewColor) + "," + Color.green(ring.mCenterNewColor) + "," + Color.blue(ring.mCenterNewColor);
        textView.setText(url);
        HttpAsyncTask httpAsyncTask = new HttpAsyncTask();
        httpAsyncTask.execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_color_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsActivity = new Intent(getBaseContext(),
                    PrefActivity.class);
            startActivity(settingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        public String doInBackground(String... params) {
            String inputLine = null;
            try {
                URL url = new URL(params[0]);
                URLConnection conn = url.openConnection();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                while ((inputLine = br.readLine()) != null) {
                    return inputLine;
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return inputLine;
        }

//        public String doInBackground(String... urls) {
//            String url = urls[0];
//            // Creating HTTP client
//            HttpClient httpClient = new DefaultHttpClient();
//
//            // Creating HTTP Post
//            HttpGet httpGet = new HttpGet(url);
//            // Making HTTP Request
//            try {
//                HttpResponse response = httpClient.execute(httpGet);
//
//            } catch (ClientProtocolException e) {
//                // writing exception to log
//                e.printStackTrace();
//
//            } catch (IOException e) {
//                // writing exception to log
//                e.printStackTrace();
//            }
//            return "OK";
//        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
        }
    }

}
