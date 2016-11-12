package io.github.divayprakash.asynctask;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.main_text);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refresh triggered", Snackbar.LENGTH_LONG)
                        .setAction("Dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {}
                        })
                        .show();
                String url = "https://iiitd.ac.in/about";
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new AsyncTaskRunner().execute(url);
                } else {
                    textView.setText("No network connection available.");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class AsyncTaskRunner extends AsyncTask<String, Void, String> {
        private static final String DEBUG_TAG = "AsyncTaskDemo";

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page!";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
        }
        private String downloadUrl(String myurl) throws IOException, UnsupportedEncodingException {
            InputStream inputStream = null;
            int length = 500;
            try {
                URL url = new URL(myurl);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(7000);
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);
                Log.d(DEBUG_TAG, "Trying to connect");
                httpURLConnection.connect();
                int response = httpURLConnection.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                inputStream = httpURLConnection.getInputStream();
                Reader reader = new InputStreamReader(inputStream, "UTF-8");
                char[] buffer = new char[length];
                reader.read(buffer);
                reader.close();
                httpURLConnection.disconnect();
                String contentAsString = new String(buffer);
                return contentAsString;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }
    }
}
