package io.github.divayprakash.asynctask;

import android.Manifest;
import android.app.ProgressDialog;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private final static int PERMISSIONS_REQUEST_INTERNET = 1;
    private final static int PERMISSIONS_REQUEST_NETWORK_STATE = 2;
    private static final String DEBUG_PERMISSIONS_TAG = "Permissions";
    private static final String DEBUG_RUNNER_TAG = "AsyncTaskRunner";
    private static final String ERROR_TAG = "ERROR";
    private String mainText;
    private boolean isRunning;
    private AsyncTaskRunner asyncTaskRunner;
    private static final String url = "https://iiitd.ac.in/about";
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.main_text);
        mainText = (String) textView.getText();
        if (savedInstanceState != null) {
            setTextView(savedInstanceState.getString("MainText"));
            if (savedInstanceState.getBoolean("AsyncTaskStatus")) {
                isRunning = true;
                progressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("Retrieving data");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
                asyncTaskRunner = new AsyncTaskRunner();
                asyncTaskRunner.execute(url);
            }
        }
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
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    isRunning = true;
                    progressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMessage("Retrieving data");
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    asyncTaskRunner = new AsyncTaskRunner();
                    asyncTaskRunner.execute(url);
                } else {
                    //Log.e(ERROR_TAG, "ERROR : No network connection available!");
                    setTextView("ERROR : No network connection available!");
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("MainText", mainText);
        savedInstanceState.putBoolean("AsyncTaskStatus", isRunning);
        super.onSaveInstanceState(savedInstanceState);
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
            System.exit(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTextView(String text) {
        mainText = new String(text);
        textView.setText(text);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRunning) asyncTaskRunner.cancel(true);
        if (progressDialog != null) progressDialog.dismiss();
    }

    private class AsyncTaskRunner extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... urls) {
            InputStream inputStream = null;
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
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line, buffer = new String();
                while ((line = bufferedReader.readLine()) != null) {
                    buffer += line;
                    buffer += "\n";
                    if (isCancelled()) {
                        httpURLConnection.disconnect();
                        bufferedReader.close();
                        break;
                    }
                }
                httpURLConnection.disconnect();
                bufferedReader.close();
                String upToNCharacters = buffer.substring(0, Math.min(buffer.length(), 4000));
                if (buffer.length() > 4000) {
                    Log.d(DEBUG_RUNNER_TAG, "Response buffer length = " + buffer.length());
                    int chunkCount = buffer.length() / 4000;
                    for (int i = 1; i <= chunkCount; i++) {
                        int max = 4000 * (i + 1);
                        String chunkIdentifier = "chunk " + (i + 1) + " of " + (chunkCount + 1) + ":" ;
                        String loggingData = buffer.substring(4000 * i, Math.min(buffer.length(), max));
                        Log.d(DEBUG_RUNNER_TAG, chunkIdentifier + loggingData);
                        if (isCancelled()) break;
                    }
                }
                return upToNCharacters;
            } catch (SocketTimeoutException e) {
                //Log.e(ERROR_TAG, "SocketTimeoutException encountered");
                isRunning = false;
                return "ERROR : Connection timed out!";
            } catch (IOException e) {
                //Log.e(ERROR_TAG, "IOException encountered");
                isRunning = false;
                return "ERROR : Unable to retrieve webpage!";
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    }
                    catch (IOException e) {
                        //Log.e(ERROR_TAG, "IOException encountered in closing inputStream");
                        isRunning = false;
                        return "ERROR : Unable to retrieve webpage!";
                    }
                }
            }
        }
        @Override
        protected void onPostExecute(String result) {
            isRunning = false;
            setTextView(result);
            if (progressDialog != null) progressDialog.dismiss();
        }
    }
}
