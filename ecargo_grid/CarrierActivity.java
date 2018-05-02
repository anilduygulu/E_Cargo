package com.ecargo.ecargo_grid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierCarsActivity;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierNotificationsActivity;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierProductActivites;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierProductsOnLoad;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierRouteActivity;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierSettings;
import com.goka.blurredgridmenu.GridMenu;
import com.goka.blurredgridmenu.GridMenuFragment;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Kaan on 4/18/2017.
 */

public class CarrierActivity extends AppCompatActivity {
    String url = "https://ecargo.info/Api/v1/user/UpdateFcm";
    String url2 = "https://ecargo.info/Api/v1/location/saveLocation";
    String TAG = "Carrier Activity";
    private GridMenuFragment mGridMenuFragment;
    JSONObject jsonObj =null;
    static String usermail,token;
    GPSTracker gps;
    double latitude;
    double longitude;
    static String carrierID,ApiKey;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrier);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
        token = sharedPreferences.getString(getString(R.string.FCM_TOKEN),"");

        mGridMenuFragment = GridMenuFragment.newInstance(R.drawable.back);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.carrier_frame, mGridMenuFragment);
        tx.addToBackStack(null);
        tx.commit();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setupGridMenu();
        try {
            if(getIntent().getExtras() != null){
                jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
                Log.d(TAG,"obj"+jsonObj);
                usermail = jsonObj.getString("UserMail");
                carrierID = jsonObj.getString("CarrierID");
                ApiKey = jsonObj.getString("apiKey");

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        /* *** */

        gps = new GPSTracker(CarrierActivity.this);
        // check if GPS enabled    

        if(gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            // \n is for new line

        }else{
            gps.showSettingsAlert();
        }
        StringRequest jsonObjRequest2 = new StringRequest(Request.Method.POST,
                url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    Log.d(TAG,"RESPONSE AFTER send location"+response);

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("volley", "Error: " + error.getMessage());
                error.printStackTrace();

            }
        })

        {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("authorization", ApiKey);
                Log.d(TAG, "Apikey on product function: " + ApiKey);

                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Latitude",latitude+"");
                params.put("Longitude",longitude+"");
                params.put("CarrierID",carrierID);
                Log.d(TAG,"FCmToken ve usermail"+latitude+" "+longitude+" "+carrierID);
                return params;
            }

        };
        MySingleton.getmInstance(getApplicationContext()).addToRequestque(jsonObjRequest2);



        /* *** */



        StringRequest jsonObjRequest = new StringRequest(Request.Method.PUT,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "response after fcm update: " + response);


                        //        MyFunctions.toastShort(LoginActivity.this, response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("volley", "Error: " + error.getMessage());
                error.printStackTrace();
                // MyFunctions.croutonAlert(LoginActivity.this,
                //       MyFunctions.parseVolleyError(error));
                //loading.setVisibility(View.GONE);
            }
        })
        {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("UserMail",usermail);
                params.put("FcmToken",token);
                Log.d(TAG,"FCmToken ve usermail"+token+" "+usermail);


                return params;
            }

        };
        MySingleton.getmInstance(getApplicationContext()).addToRequestque(jsonObjRequest);


        mGridMenuFragment.setOnClickMenuListener(new GridMenuFragment.OnClickMenuListener() {
            @Override
            public void onClickMenu(GridMenu gridMenu, int position) {


                // menüdeki iconlara tıklayarak CarrierActivites klasörünün altındaki aktivitelere geçilicek
                if(position == 0){
                    Intent intent = new Intent(getApplicationContext(), CarrierCarsActivity.class);
                    intent.putExtra("jsonObj",jsonObj.toString());
                    startActivity(intent);
                }else if(position == 1){
                    Intent intent = new Intent(getApplicationContext(), CarrierRouteActivity.class);
                    intent.putExtra("jsonObj",jsonObj.toString());
                    startActivity(intent);
                }else if(position == 2){
                    Intent intent = new Intent(getApplicationContext(), CarrierProductActivites.class);
                    intent.putExtra("jsonObj",jsonObj.toString());
                    startActivity(intent);
                }else if(position == 3){
                    Intent intent = new Intent(getApplicationContext(), CarrierNotificationsActivity.class);
                    intent.putExtra("jsonObj",jsonObj.toString());
                    startActivity(intent);
                }else if(position == 4){
                    Intent intent = new Intent(getApplicationContext(), CarrierSettings.class);
                    intent.putExtra("jsonObj",jsonObj.toString());
                    startActivity(intent);
                }
                else if(position == 5){
                    Intent intent = new Intent(getApplicationContext(), CarrierProductsOnLoad.class);
                    intent.putExtra("jsonObj",jsonObj.toString());
                    startActivity(intent);
                }

            }
        });
    }

    private void setupGridMenu() {
        List<GridMenu> menus = new ArrayList<>();
        menus.add(new GridMenu("Cars", R.drawable.truck_5000));
        menus.add(new GridMenu("Add Route", R.drawable.compass_50));
        menus.add(new GridMenu("Products", R.drawable.product50));
        menus.add(new GridMenu("Notifications", R.drawable.message_50));
        menus.add(new GridMenu("Settings", R.drawable.settings_50));
        menus.add(new GridMenu("Products On Load", R.drawable.load1));

        mGridMenuFragment.setupMenu(menus);
    }

    @Override
    public void onBackPressed() {
        if (0 == getSupportFragmentManager().getBackStackEntryCount()) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }


}
