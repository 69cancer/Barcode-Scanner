package com.dxd.barcodescanner;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.NameValuePair;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.entity.ContentType;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;
import ch.boye.httpclientandroidlib.entity.mime.content.FileBody;
import ch.boye.httpclientandroidlib.impl.client.CloseableHttpClient;
import ch.boye.httpclientandroidlib.impl.client.HttpClients;
import ch.boye.httpclientandroidlib.message.BasicNameValuePair;


public class UploadProduct extends ActionBarActivity {
    int YOUR_SELECT_PICTURE_REQUEST_CODE = 0;
    EditText editText;
    private Uri outputFileUri;
    private Uri selectedImageUri;
    private String selectedImagePath = null;
    private String URL = null;
    private int responseCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_product);
        editText = (EditText) findViewById(R.id.barcodeID);
        editText.setText(getIntent().getStringExtra("barcode"));
    }


    public void uploadProd(View v){
        String sku = ((EditText)findViewById(R.id.sku)).getText().toString();

        String isFeatured = ((EditText) findViewById(R.id.isFeact)).getText().toString();


        String listPrice = ((EditText) findViewById(R.id.listPrice)).getText().toString();
        String desc = ((EditText) findViewById(R.id.desc)).getText().toString();
        String shortDesc = ((EditText) findViewById(R.id.shortDesc)).getText().toString();
        String  barcodeID = ((EditText) findViewById(R.id.barcodeID)).getText().toString();
        String prdName = ((EditText) findViewById(R.id.productName)).getText().toString();
        String storeID= ((EditText) findViewById(R.id.storeID)).getText().toString();
        String isActive = ((EditText) findViewById(R.id.isAct)).getText().toString();
        String price = ((EditText) findViewById(R.id.price)).getText().toString();
        String initStk = ((EditText) findViewById(R.id.initStk)).getText().toString();
        String stk = ((EditText) findViewById(R.id.stk)).getText().toString();

        List<NameValuePair> postPdtData = new ArrayList();
        if(selectedImagePath != null) {
            String filename = selectedImagePath.toString();
            postPdtData.add(new BasicNameValuePair("image",filename));
            postPdtData.add(new BasicNameValuePair("sku", sku));
            postPdtData.add(new BasicNameValuePair("is_featured", isFeatured));
            postPdtData.add(new BasicNameValuePair("description", desc));
            postPdtData.add(new BasicNameValuePair("barcode_id", barcodeID));
            postPdtData.add(new BasicNameValuePair("title", prdName));
            postPdtData.add(new BasicNameValuePair("storeid", storeID));
            postPdtData.add(new BasicNameValuePair("is_active", isActive));
            postPdtData.add(new BasicNameValuePair("short_description", shortDesc));
            postPdtData.add(new BasicNameValuePair("price", price));
            postPdtData.add(new BasicNameValuePair("initial_stock", initStk));
            postPdtData.add(new BasicNameValuePair("stock",stk));
            postPdtData.add(new BasicNameValuePair("list_price", listPrice));

            URL = getIntent().getStringExtra("url");
            new httpPostAsyncTask().execute(postPdtData);
        }
        else{
            Toast.makeText(getApplicationContext(), "Choose an image", Toast.LENGTH_SHORT).show();
        }
     }

    public void openImageIntent(View v) {

// Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fname = "img_" + System.currentTimeMillis() + ".jpg";
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, YOUR_SELECT_PICTURE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == YOUR_SELECT_PICTURE_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }


                if (isCamera) {

                    selectedImagePath = outputFileUri.getPath().toString();
                    Toast.makeText(getApplicationContext(), "Image selected " + selectedImagePath, Toast.LENGTH_SHORT).show();

                } else {
                    //selectedImageUri = (data == null) ? null : data.getData();


                    selectedImageUri = data.getData();

                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    // Check for the freshest data.
                    // getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);

    /* now extract ID from Uri path using getLastPathSegment() and then split with ":"
    then call get Uri to for Internal storage or External storage for media I have used getUri()
    */

                    String id = selectedImageUri.getLastPathSegment().split(":")[1];
                    final String[] imageColumns = {MediaStore.Images.Media.DATA};
                    final String imageOrderBy = null;

                    Uri uri = getUri();


                    Cursor imageCursor = managedQuery(uri, imageColumns,
                            MediaStore.Images.Media._ID + "=" + id, null, imageOrderBy);

                    if (imageCursor.moveToFirst()) {
                        selectedImagePath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    }


                    Toast.makeText(getApplicationContext(), "Image selected " + selectedImagePath, Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public class httpPostAsyncTask extends AsyncTask<List<NameValuePair>, Void, String> {

        @Override
        protected String doInBackground(List<NameValuePair>... nameValuePairs) {

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost uploadFile = new HttpPost(URL);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            for (int index = 0; index < nameValuePairs[0].size(); index++) {
                if (nameValuePairs[0].get(index).getName().equalsIgnoreCase("image")) {

                    // If the key equals to "image", we use FileBody to transfer the data
                    //  builder.addBinaryBody("image",file);
                    builder.addPart(nameValuePairs[0].get(index).getName(), new FileBody(new File(nameValuePairs[0].get(index).getValue())));
                } else {
                    // Normal string data
                    //entity.addPart(nameValuePairs.get(index).getName(), new StringBody(nameValuePairs.get(index).getValue()));

                    builder.addTextBody(nameValuePairs[0].get(index).getName(), nameValuePairs[0].get(index).getValue(), ContentType.TEXT_PLAIN);
                }
            }

            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);
            String result = "fail";


            try {

                HttpResponse response = null;
                response = httpClient.execute(uploadFile);
                //  result="got it";
                responseCode = response.getStatusLine().getStatusCode();
                HttpEntity responseEntity = response.getEntity();
                String responseServer = responseEntity.toString();
                return responseServer;
            } catch (IOException e) {
                result = e.getMessage();
                return result;
            }


        }

        @Override
        protected void onPostExecute(String result) {
            if (responseCode == 200) {

                AlertDialog.Builder builder = new AlertDialog.Builder(UploadProduct.this);
                builder.setMessage("Product Details was uploaded successfully to server.")
                        .setTitle("Upload");
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
            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(UploadProduct.this);
                builder.setMessage("Oops! Something went wrong.\n" + result)
                        .setTitle("Error");
                builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
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
        }
    }
}
