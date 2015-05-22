package com.dxd.barcodescanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.HttpStatus;
import ch.boye.httpclientandroidlib.StatusLine;
import ch.boye.httpclientandroidlib.client.ClientProtocolException;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.params.BasicHttpParams;
import ch.boye.httpclientandroidlib.params.HttpConnectionParams;
import ch.boye.httpclientandroidlib.params.HttpParams;


public class ProductSearch extends Activity {
    String barcode = null;
    JSONObject jsono;
    ArrayList<Products> productKeyValues;
    PdtAdapter adapter;
    String IP = "192.168.1.6";
    String PORT = "8080";
    String error = "noError";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_product_search);
        setContentView(R.layout.product_details);
        barcode = getIntent().getStringExtra("barcode");
        String url = "http://" + IP + ":" + PORT + "/product/?q=" + barcode;
        productKeyValues = new ArrayList<Products>();
        new searchDatabaseAsyncTask().execute(url);


    }

    public void finishPdtdetail(View v) {
        Intent intent = new Intent(getApplicationContext(), ScannerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    // Handle Exceptions
    public void exceptionHandler(String e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductSearch.this);
        builder.setMessage(e)
                .setTitle("Error");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(getApplicationContext(), ScannerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

    }

    private void scaleImage(Bitmap bitmap) {
        // Get the ImageView and its bitmap
        ImageView view = (ImageView) findViewById(R.id.pdtImage);

        // Get current dimensions AND the desired bounding box
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int bounding = dpToPx(250);
        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;


        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);

        // Apply the scaled bitmap
        view.setImageDrawable(result);
    }

    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public class searchDatabaseAsyncTask extends AsyncTask<String, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ProductSearch.this);
            dialog.setMessage("Loading, please wait");
            dialog.setTitle("Connecting server");
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            // The default value is zero, that means the timeout is not used.
            int timeoutConnection = 30000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 50000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpClient httpclient = new DefaultHttpClient(httpParameters);
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
                    jsono = new JSONObject(responseString);
                    return true;

                } else {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                error = e.getMessage();

            } catch (IOException e) {
                error = e.getMessage();

            } catch (JSONException e) {
                error = e.getMessage();
            }

            return false;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (error.equals("noError") || error.equals("NOT FOUND")) {
                if (aBoolean == false) {
                    dialog.cancel();
                    AlertDialog.Builder ad = new AlertDialog.Builder(ProductSearch.this);
                    ad.setTitle("Sorry");
                    ad.setMessage("Product details corrosponding to this " + barcode + " was not found on the server\n Do you want to upload product details");
                    ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent intent = new Intent(getApplicationContext(), UploadProduct.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("barcode", barcode);
                            intent.putExtra("url", "http://" + IP + ":" + PORT + "/upload_product/");
                            getApplicationContext().startActivity(intent);
                            finish();
                        }
                    });
                    ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Intent intent = new Intent(getApplicationContext(), ScannerActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });
                    ad.show();

                } else {
                    ImageView iv = (ImageView) findViewById(R.id.pdtImage);
                    iv.setImageResource(R.drawable.ic_launcher);
                    Iterator<String> iter = jsono.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        try {
                            switch (key) {
                                case "title":
                                    Object value = jsono.get(key);
                                    TextView pdt = (TextView) findViewById(R.id.pdtName);
                                    pdt.setText(value.toString());
                                    break;
                                case "short_description":
                                    value = jsono.get(key);
                                    pdt = (TextView) findViewById(R.id.pdtDesc);
                                    pdt.setText(value.toString());
                                    break;
                                case "price":
                                    value = jsono.get(key);
                                    pdt = (TextView) findViewById(R.id.textViewPrice);
                                    pdt.setText(value.toString());
                                    pdt = (TextView) findViewById(R.id.price);
                                    pdt.setVisibility(View.VISIBLE);
                                    break;

                                case "list_price":
                                    value = jsono.get(key);
                                    pdt = (TextView) findViewById(R.id.textViewCostPrice);
                                    pdt.setText(value.toString());
                                    pdt = (TextView) findViewById(R.id.costPrice);
                                    pdt.setVisibility(View.VISIBLE);
                                    break;
                                case "is_active":
                                    value = jsono.get(key);
                                    CheckedTextView checkedTextView = (CheckedTextView) findViewById(R.id.checkboxAvail);
                                    if ((value.toString()).equalsIgnoreCase("true"))
                                        checkedTextView.setChecked(true);
                                    else checkedTextView.setChecked(false);
                                    checkedTextView.setVisibility(View.VISIBLE);
                                    Button button = (Button) findViewById(R.id.ok);
                                    button.setVisibility(View.VISIBLE);
                                    break;
                                case "image":
                                    value = jsono.get(key);
                                    new DownloadImageTask().execute("http://" + IP + ":" + PORT + "/media/" + value.toString());
                                    break;
                                case "status":
                                    break;
                                case "id":
                                    break;
                                case "description":
                                    break;

                                default:
                                    Products product = new Products();
                                    product.setKey(key);
                                    product.setValue((jsono.get(key)).toString());
                                    productKeyValues.add(product);
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Something went wrong in json parsing", Toast.LENGTH_SHORT).show();
                        }


                    }


                    ListView listview = (ListView) findViewById(R.id.listView);
                    adapter = new PdtAdapter(getApplicationContext(), R.layout.pdt_details_list, productKeyValues);

                    listview.setAdapter(adapter);

                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                                long id) {
                            // TODO Auto-generated method stub
                            Toast.makeText(getApplicationContext(), productKeyValues.get(position).getKey(), Toast.LENGTH_LONG).show();
                        }
                    });
                    ImageView imageView = (ImageView) findViewById(R.id.pdtImage);
                    while (((BitmapDrawable) imageView.getDrawable()).getBitmap() == null) {
                        continue;
                    }
                    dialog.cancel();
                }
            } else {
                dialog.cancel();
                exceptionHandler(error);
            }
        }

    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap imgPdt = null;
            try {
                InputStream in = new java.net.URL(params[0]).openStream();
                imgPdt = BitmapFactory.decodeStream(in);
                return imgPdt;
            } catch (Exception e) {
                e.printStackTrace();

            }
            return imgPdt;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            scaleImage(bitmap);
        }
    }

}