package com.example.raghavandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "Tag";
    //Ratings array
    int ratingsArr[] = new int[] {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    String heartRate;
    String respRate;
    String coordinates;
    float starRatings = 0.0f;
    RatingBar myRatingBar;
    Spinner mySpinner;
    int symptomIndex;
    db covidDB;
    ProgressDialog progress;
    MainActivity2 activity2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Intent intent = getIntent();
        heartRate = intent.getStringExtra("heart");
        respRate = intent.getStringExtra("resp");
        coordinates = intent.getStringExtra("gps");
        covidDB = new db(getApplicationContext());

        //Symptom Logging Page Text view
        TextView tv = (TextView)findViewById(R.id.textView2);
        tv.setText("Symptom Logging Page");

        //Spinner Button
        Button btn = (Button)findViewById(R.id.button2);

        //Spinner (mySpinner)
        mySpinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity2.this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.names));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);

        //New symptom is selected
        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                symptomIndex = mySpinner.getSelectedItemPosition();
                myRatingBar.setRating(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //Rating Bar (ratingBar)
        myRatingBar = (RatingBar)findViewById(R.id.ratingBar);

        //Rating entering star values
        myRatingBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                starRatings = myRatingBar.getRating();
                ratingsArr[symptomIndex] = (int)starRatings;
                return false;
            }
        });

        //Upload Symptoms Button [Upload database to the server]
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext() , "Pushing values in Database!", Toast.LENGTH_LONG).show();
                covidDB.insertData(ratingsArr, heartRate, respRate, coordinates);

                //Asynchronous Request
                RequestParams params = new RequestParams();


                try {
                    params.put("uploaded_file", new File("/data/data/com.example.raghavandroidapp/databases/covid.db"));
                    params.put("id","raghav");
                    params.put("accept","1");

                } catch(FileNotFoundException e) {}

                AsyncHttpClient client = new AsyncHttpClient();
                client.post("http://192.168.0.18:8080/testing/save_file.php", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                        // handle success response
                        Log.e("msg success",statusCode+"");
                        if(statusCode==200) {
                            Toast.makeText(MainActivity2.this, "Success", Toast.LENGTH_SHORT).show();
                            activity2.finish();

                        }
                        else {
                            Toast.makeText(MainActivity2.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                        // handle failure response
                        Log.e("Message Fail",statusCode+"");

                        //Toast.makeText(videoActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();

                    }
                    @Override
                    public void onProgress(long bytesWritten, long totalSize) {
                        super.onProgress(bytesWritten, totalSize);
                    }


                    @Override
                    public void onStart() {

                        super.onStart();
                    }

                    @Override
                    public void onFinish() {

                        super.onFinish();
                    }
                });


            }
        });
    }


//    private void upload_to_server() {
//
//        progress = new ProgressDialog(MainActivity2.this);
//        progress.setTitle("Uploading");
//        progress.setMessage("Please wait...");
//        progress.show();
//
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                File f  = new File("/data/data/com.example.raghavandroidapp/databases/covid.db");
//                String content_type  = getMimeType(f.getPath());
//
//                String file_path = f.getAbsolutePath();
//                OkHttpClient client = new OkHttpClient();
//                RequestBody file_body = RequestBody.create(MediaType.parse(content_type),f);
//
//                RequestBody request_body = new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("type",content_type)
//                        .addFormDataPart("uploaded_file", "/data/data/com.example.raghavandroidapp/databases/covid.db", file_body)
//                        .build();
//
//                Request request = new Request.Builder()
//                        .url("http://192.168.0.18/testing/save_file.php")
//                        .post(request_body)
//                        .build();
//
//                try {
//                    Response response = client.newCall(request).execute();
//
//                    if(!response.isSuccessful()){
//                        throw new IOException("Error : "+response);
//                    }
//
//                    progress.dismiss();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//        });
//
//        t.start();
//    }
//
//    private String getMimeType(String path) {
//
//        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
//
//        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//    }
//

}