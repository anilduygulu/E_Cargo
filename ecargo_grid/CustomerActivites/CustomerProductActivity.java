package com.ecargo.ecargo_grid.CustomerActivites;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.AdapterCustomerForAllProduct;
import com.ecargo.ecargo_grid.AdapterForOnRouteProduct;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierProductActivites;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierRouteActivity;
import com.ecargo.ecargo_grid.CarrierActivity;
import com.ecargo.ecargo_grid.JSONAdapter;
import com.ecargo.ecargo_grid.JSONAdapterForTrucks;
import com.ecargo.ecargo_grid.MySingleton;
import com.ecargo.ecargo_grid.R;
import com.ecargo.ecargo_grid.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CustomerProductActivity extends AppCompatActivity {
    final String TAG = "CustomerProduct Act:";
    String url = "https://ecargo.info/Api/v1/products/AllProductsAndOnRoute";
    ListView lstTest,allProductsListView;
    String ApiKey;
    JSONObject jsonObj;
    static JSONObject responseObj;
    Button showRoute,allProductsButton;

    String usermail;
    String custid;
    List<String> listHeaderDataForAllProduct;
    HashMap<String, List<String>> listChildDataForAllProduct;
    AdapterCustomerForAllProduct adapterCustomerForAllProduct;
    AdapterForOnRouteProduct jSONAdapter;
    AdapterCustomerForAllProduct jsonAdapterForAllProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_product);
        lstTest = (ListView) findViewById(R.id.list_on_route_products);
        showRoute = (Button) findViewById(R.id.show_product_on_route);
        allProductsListView = (ListView) findViewById(R.id.list_all_products);
        allProductsButton = (Button) findViewById(R.id.show_product_all);

        try {
            jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
         //   Log.d(TAG, "JsonObj" + jsonObj);
            ApiKey = jsonObj.getString("apiKey");
            usermail = jsonObj.getString("UserMail");
            custid = jsonObj.getString("CustomerID");

        } catch (JSONException e) {
            e.printStackTrace();
        }

      //  Log.d(TAG, "APikey: " + ApiKey);

        StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: " + response);
                        try {
                            responseObj = new JSONObject(response.toString());
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
                params.put("CustomerID", custid);
                return params;
            }

        };
        MySingleton.getmInstance(CustomerProductActivity.this).addToRequestque(jsonObjRequest);

        showRoute.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                JSONArray productArray = null;

                try {

                    if(responseObj.getBoolean("error") == true){
                        Toast.makeText(getApplicationContext(),responseObj.getString("message"),Toast.LENGTH_LONG).show();
                    }else{
                        productArray = new JSONArray(responseObj.getString("ProductsOnRoute"));
                        Log.d(TAG,"responsesss"+productArray.toString());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jSONAdapter = new AdapterForOnRouteProduct(CustomerProductActivity.this,productArray,ApiKey);//jArray is your json array
                //Set the above adapter as the adapter of choice for our list
                lstTest.setAdapter(jSONAdapter);
                allProductsListView.setAdapter(jsonAdapterForAllProducts);
            }
        });

        allProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray allProductArray = null;
                try {

                    if(responseObj.getBoolean("error") == true){
                        Toast.makeText(getApplicationContext(),responseObj.getString("message"),Toast.LENGTH_LONG).show();
                    }else{
                        allProductArray = new JSONArray(responseObj.getString("AllProducts"));
                        Log.d(TAG,"responsesss"+allProductArray.toString());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonAdapterForAllProducts = new AdapterCustomerForAllProduct(CustomerProductActivity.this,allProductArray,ApiKey);
                //Set the above adapter as the adapter of choice for our list
                allProductsListView.setAdapter(jsonAdapterForAllProducts);
            }
        });




    }




}
