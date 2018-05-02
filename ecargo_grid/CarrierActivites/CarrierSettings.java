package com.ecargo.ecargo_grid.CarrierActivites;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.MySingleton;
import com.ecargo.ecargo_grid.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kaan on 4/18/2017.
 */

public class CarrierSettings extends AppCompatActivity {
    String usermail;
    static String carrierId;
    String url = "https://ecargo.info/Api/v1/cars/ListCars";
    String url2 = "https://ecargo.info/Api/v1/getTypeIDCarr";
    final String TAG = "Truck List";
    String ApiKey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_settings);


        try {
            JSONObject jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
            usermail = jsonObj.getString("UserMail");
            ApiKey = jsonObj.getString("apiKey");
        } catch (JSONException e) {

            e.printStackTrace();
        }


       



    }
}
