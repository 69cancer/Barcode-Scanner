package com.dxd.barcodescanner;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class ProductSearch extends ActionBarActivity {
    String barcode = null;
    String jsonData = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_search);
        barcode = getIntent().getStringExtra("barcode");

    }



    public class searchDatabaseAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;

            try {
                response = httpclient.execute(new HttpGet(params[0]));
                StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();

                    JSONObject jsono = new JSONObject(responseString);
                    jsonData = jsono.toString();





                    return true;

                } else {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return false;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean == false) {
                Toast.makeText(getApplicationContext(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), UploadProduct.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("IP",((EditText) findViewById(R.id.editTextIP)).getText().toString());
                intent.putExtra("Port",((EditText) findViewById(R.id.editTextPort)).getText().toString());
                intent.putExtra("barcode",barcode);
                getApplicationContext().startActivity(intent);
 
            }
            else{
                TextView textView = (TextView) findViewById(R.id.jsonData);
                textView.setText(jsonData);
            }
        }

    }


    public void searchDatabase(View v){
        EditText editTextIP = (EditText) findViewById(R.id.editTextIP);

        EditText editTextPort = (EditText) findViewById(R.id.editTextPort);
        String ip = editTextIP.getText().toString();
        String port = editTextPort.getText().toString();
        String url = "http://"+ ip+ ":"+ port+ "/product/?q="+ barcode;

        new searchDatabaseAsyncTask().execute(url);
    }

}
