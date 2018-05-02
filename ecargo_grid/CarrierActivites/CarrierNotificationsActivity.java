package com.ecargo.ecargo_grid.CarrierActivites;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.AdapterForCarrierNotifications;
import com.ecargo.ecargo_grid.JSONAdapter;
import com.ecargo.ecargo_grid.JSONAdapterForTrucks;
import com.ecargo.ecargo_grid.MySingleton;
import com.ecargo.ecargo_grid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kaan on 4/18/2017.
 */

public class CarrierNotificationsActivity extends AppCompatActivity {
    final String TAG = "Carrier Notifications:";

    String url = "https://ecargo.info/Api/v1/notifications/ListCarrierNotifications";
    JSONObject jsonObj;
    String ApiKey;
    ListView listView;
    public static AdapterForCarrierNotifications adapterForCarrierNotifications;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carrier_notifications);
        listView = (ListView) findViewById(R.id.notification_listView);
        try {
            jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
            Log.d(TAG,"JsonObj"+jsonObj);
            ApiKey = jsonObj.getString("apiKey");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject result = null;
                        try {
                            result = new JSONObject(response);
                            String notifications = result.getString("Notifications");
                            JSONArray jsonArray = new JSONArray(notifications);

                            adapterForCarrierNotifications = new AdapterForCarrierNotifications(CarrierNotificationsActivity.this,jsonArray,ApiKey,jsonObj);//jArray is your json array

                            //Set the above adapter as the adapter of choice for our list
                            listView.setAdapter(adapterForCarrierNotifications);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


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


                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("ApiKey", ApiKey);
                return params;
            }

        };
        MySingleton.getmInstance(CarrierNotificationsActivity.this).addToRequestque(jsonObjRequest);

    }
}
