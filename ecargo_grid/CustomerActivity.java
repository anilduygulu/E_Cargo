package com.ecargo.ecargo_grid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierRouteActivity;
import com.ecargo.ecargo_grid.CustomerActivites.CustomerAddProductActivity;
import com.ecargo.ecargo_grid.CustomerActivites.CustomerNotificationActivity;
import com.ecargo.ecargo_grid.CustomerActivites.CustomerProductActivity;
import com.ecargo.ecargo_grid.CustomerActivites.CustomerTruckActivity;
import com.goka.blurredgridmenu.GridMenuFragment;
import com.goka.blurredgridmenu.GridMenu;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kaan on 4/18/2017.
 */

public class CustomerActivity extends AppCompatActivity {

    String TAG = "Customer Activity";
    String url = "https://ecargo.info/Api/v1/user/UpdateFcm";
    private GridMenuFragment mGridMenuFragment;
    JSONObject jsonObj =null;
    static String usermail,token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
        token = sharedPreferences.getString(getString(R.string.FCM_TOKEN),"");
        mGridMenuFragment = GridMenuFragment.newInstance(R.drawable.back);
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.customer_frame, mGridMenuFragment);
        tx.addToBackStack(null);
        tx.commit();

        setupGridMenu();





        Intent intent = getIntent();
        try {
            if(getIntent().getExtras() != null){
                jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if(getIntent().getExtras() != null){
                jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
                usermail = jsonObj.getString("UserMail");
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                    Intent intent = new Intent(getApplicationContext(), CustomerAddProductActivity.class);
                    intent.putExtra("jsonObj",jsonObj.toString());
                    startActivity(intent);
                }else if(position == 1){
                    Intent intent = new Intent(getApplicationContext(), CustomerTruckActivity.class);
                    intent.putExtra("jsonObj",jsonObj.toString());
                    startActivity(intent);
                }else if(position == 2){
                    Intent intent = new Intent(getApplicationContext(), CustomerProductActivity.class);
                    intent.putExtra("jsonObj",jsonObj.toString());
                    startActivity(intent);

                }else if(position == 3){
                    Intent intent = new Intent(getApplicationContext(), CustomerNotificationActivity.class);
                    intent.putExtra("jsonObj",jsonObj.toString());
                    startActivity(intent);


                }else if(position == 4){

                }

            }
        });
    }

    private void setupGridMenu() {
        List<GridMenu> menus = new ArrayList<>();
        menus.add(new GridMenu("Add Product", R.drawable.add50));
        menus.add(new GridMenu("Routes", R.drawable.compass_50));
        menus.add(new GridMenu("Products", R.drawable.product50));
        menus.add(new GridMenu("Notifications", R.drawable.message_50));
        menus.add(new GridMenu("Settings", R.drawable.settings_50));

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
