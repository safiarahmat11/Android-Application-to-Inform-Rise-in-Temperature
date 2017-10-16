package com.safiarahmat.hydrademo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    private static final String SERVICE_URL = "http://192.168.1.202:8080/hydra/rest1/responder";
    private static final String TAG = "MainActivity";
    ImageView previewImage;
    Button acceptButton;
    VideoView vidView;
    String vidAddress;
    EditText camId;
    EditText location;
    EditText alert_type ;
    public  String flag=" ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void onStart() {
        super.onStart();

        int delay = 500; // delay for 1 sec.
        int period = 2000; // repeat every 10 sec.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                displayData();  // display the data
            }
        }, delay, period);

    }
    public void displayData(){
        String sampleURL = SERVICE_URL + "/getEventDetails";

        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "GETting data...");

        wst.execute(new String[]{sampleURL});
    }
    public void afterSomeTime(){
        // flag.replace("true","false");
        flag= "false";
        System.out.println("I AM THE REPLACED FLAG " + flag);
        alert_type = (EditText) findViewById(R.id.alert_type);
        location = (EditText) findViewById(R.id.location);
        camId = (EditText) findViewById(R.id.cam_id);
        previewImage=(ImageView)findViewById(R.id.previewImage);
        vidView = (VideoView)findViewById(R.id.myVideo);
        vidView.pause();
        previewImage.setVisibility(View.VISIBLE);
        camId.setText(" ");
        location.setText(" ");
        alert_type.setText(" ");
        WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Posting data...");
        wst.addNameValuePair("FLAG_BOOL", flag);
        wst.execute(new String[]{SERVICE_URL});
    }

    public void retrieveSampleData(View vw) {
        // flag.replace(" ","true");
        flag="true";
        String RESPONDER_ID="REScx12gif";

        int endAt = 30000;
        alert_type = (EditText) findViewById(R.id.alert_type);
        location = (EditText) findViewById(R.id.location);
        camId = (EditText) findViewById(R.id.cam_id);

        Runnable stopPlayerTask = new Runnable(){
            @Override
            public void run() {

                afterSomeTime();



            }};
        vidView = (VideoView)findViewById(R.id.myVideo);
        previewImage=(ImageView)findViewById(R.id.previewImage);
        acceptButton=(Button)findViewById(R.id.acceptButton);
        vidAddress = "https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4";
        Uri vidUri = Uri.parse(vidAddress);
        vidView.setVideoURI(vidUri);
        previewImage.setVisibility(View.GONE);
        vidView.start();
        Handler handler = new Handler();
        handler.postDelayed(stopPlayerTask, endAt);
        MediaController vidControl = new MediaController(this);
        vidControl.setAnchorView(vidView);
        vidView.setMediaController(vidControl);
        WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, this, "Posting data...");
        wst.addNameValuePair("RESPONDER_ID", RESPONDER_ID);
        wst.addNameValuePair("FLAG_BOOL", flag);
        wst.execute(new String[]{SERVICE_URL});
    }

    public void handleResponse(String response) {
        EditText camId = (EditText) findViewById(R.id.cam_id);
        EditText location = (EditText) findViewById(R.id.location);
        EditText alert_type = (EditText) findViewById(R.id.alert_type);
        try {

            JSONObject jso = new JSONObject(response);
            String camIdDetails = jso.getString("camId");
            String locationDetails = jso.getString("location");
            String alertDetails = jso.getString("alertType");
            System.out.println(camIdDetails);
            camId.setText(camIdDetails);
            location.setText(locationDetails);
            alert_type.setText(alertDetails);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    private class WebServiceTask extends AsyncTask<String, Integer, String> {

        public static final int POST_TASK = 1;
        public static final int GET_TASK = 2;

        private static final String TAG = "WebServiceTask";

        // connection timeout, in milliseconds (waiting to connect)
        private static final int CONN_TIMEOUT = 3000;

        // socket timeout, in milliseconds (waiting for data)
        private static final int SOCKET_TIMEOUT = 5000;

        private int taskType = GET_TASK;
        private Context mContext = null;
        private String processMessage = "Processing...";

        private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();


        private ProgressDialog pDlg = null;

        public WebServiceTask(int taskType, Context mContext, String processMessage) {

            this.taskType = taskType;
            this.mContext = mContext;
            this.processMessage = processMessage;
        }

        public void addNameValuePair(String name, String value) {

            params.add(new BasicNameValuePair(name, value));
        }

        protected String doInBackground(String... urls) {

            String url = urls[0];
            String result = "";

            HttpResponse response = doResponse(url);

            if (response == null) {
                return result;
            } else {

                try {

                    result = inputStreamToString(response.getEntity().getContent());

                } catch (IllegalStateException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);

                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                }

            }

            return result;
        }
        @Override
        protected void onPostExecute(String response) {

            handleResponse(response);
        }

        private HttpParams getHttpParams() {
            HttpParams htpp = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
            HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);
            return htpp;
        }

        private HttpResponse doResponse(String url) {
            // Use our connection and data timeouts as parameters for our
            // DefaultHttpClient
            HttpClient httpclient = new DefaultHttpClient(getHttpParams());
            HttpResponse response = null;
            try {
                switch (taskType) {
                    case POST_TASK:
                        HttpPost httppost = new HttpPost(url);
                        httppost.setEntity(new UrlEncodedFormEntity(params));
                        response = httpclient.execute(httppost);
                        break;

                    case GET_TASK:
                        HttpGet httpget = new HttpGet(url);
                        response = httpclient.execute(httpget);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
            return response;
        }

        private String inputStreamToString(InputStream is) {
            String line = "";
            StringBuilder total = new StringBuilder();
            // Wrap a BufferedReader around the InputStream
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            try {
                // Read response until the end
                while ((line = rd.readLine()) != null) {
                    total.append(line);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
            // Return full string
            return total.toString();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
