package io.github.divayprakash.asynctask;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private final static int PERMISSIONS_REQUEST_INTERNET = 1;
    private final static int PERMISSIONS_REQUEST_NETWORK_STATE = 2;
    private static final String DEBUG_PERMISSIONS_TAG = "Permissions";
    private static final String DEBUG_RUNNER_TAG = "AsyncTaskRunner";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.main_text);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        checkPermissions();
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

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Log.d(DEBUG_PERMISSIONS_TAG, "User has not granted INTERNET permission, request for it");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},
                    PERMISSIONS_REQUEST_INTERNET);
        }
        else {
            Log.d(DEBUG_PERMISSIONS_TAG, "User has already granted INTERNET permission, rejoice!");
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(DEBUG_PERMISSIONS_TAG, "User has not granted ACCESS_NETWORK_STATE permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    PERMISSIONS_REQUEST_NETWORK_STATE);
        }
        else {
            Log.d(DEBUG_PERMISSIONS_TAG, "User has already granted ACCESS_NETWORK_STATE permission, rejoice!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_INTERNET: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(DEBUG_PERMISSIONS_TAG, "User has now granted INTERNET permission");
                } else {
                    Log.d(DEBUG_PERMISSIONS_TAG, "User did not grant INTERNET permission on request");
                    checkPermissions();
                }
                return;
            }
            case PERMISSIONS_REQUEST_NETWORK_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(DEBUG_PERMISSIONS_TAG, "User has now granted ACCESS_NETWORK_STATE permission");
                } else {
                    Log.d(DEBUG_PERMISSIONS_TAG, "User did not grant ACCESS_NETWORK_STATE permission on request");
                    checkPermissions();
                }
                return;
            }
        }
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
        @Override
        protected String doInBackground(String... urls) {
            InputStream inputStream = null;
            int length = 500;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(7000);
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);
                Log.d(DEBUG_RUNNER_TAG, "Trying to connect");
                httpURLConnection.connect();
                int response = httpURLConnection.getResponseCode();
                Log.d(DEBUG_RUNNER_TAG, "The response is: " + response);
                inputStream = httpURLConnection.getInputStream();
                Reader reader = new InputStreamReader(inputStream, "UTF-8");
                char[] buffer = new char[length];
                reader.read(buffer);
                reader.close();
                httpURLConnection.disconnect();
                String contentAsString = new String(buffer);
                return contentAsString;
            } catch (SocketTimeoutException e) {
                Log.d(DEBUG_RUNNER_TAG, "SocketTimeoutException encountered");
                return "ERROR : Connection timed out!";
            } catch (IOException e) {
                Log.d(DEBUG_RUNNER_TAG, "IOException encountered");
                return "ERROR : Unable to retrieve webpage!";
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    }
                    catch (IOException e) {
                        Log.d(DEBUG_RUNNER_TAG, "IOException encountered in closing inputStream");
                        return "ERROR : Unable to retrieve webpage!";
                    }
                }
            }
        }
        @Override
        protected void onPostExecute(String result) {
            textView.setText(result);
        }
    }
}
