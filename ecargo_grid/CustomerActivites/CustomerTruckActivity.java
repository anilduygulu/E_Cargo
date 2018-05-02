package com.ecargo.ecargo_grid.CustomerActivites;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierProductActivites;
import com.ecargo.ecargo_grid.JSONAdapter;
import com.ecargo.ecargo_grid.JSONAdapterForTrucks;
import com.ecargo.ecargo_grid.MySingleton;
import com.ecargo.ecargo_grid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class CustomerTruckActivity extends AppCompatActivity {
    final String TAG = "Customer Truck Activity";
    String url = "https://ecargo.info/Api/v1/routes/ListRoutes";
    String url2 = "https://ecargo.info/Api/v1/getTypeIDCust";
    String url3 = "https://ecargo.info/Api/v1/products/AllProductsAndOnRoute";
    ExpandableListView lstTest;
    String ApiKey;
    JSONObject jsonObj;
    JSONAdapterForTrucks truckAdapter;
    static JSONArray abc,productArray;
    static String customid = ""  ;
    String usermail;
    SearchView sv;

    List<String> listHeaderData;
    HashMap<String, List<String>> listChildData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customer_trucks);
        sv = (SearchView) findViewById(R.id.searchView2);


        lstTest = (ExpandableListView) findViewById(R.id.list_trucks);
        try {
            jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
            Log.d(TAG, "JsonObj" + jsonObj);
            ApiKey = jsonObj.getString("apiKey");
            usermail = jsonObj.getString("UserMail");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "APikey: " + ApiKey);

        StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: " + response);
                        try {

                            JSONObject result = new JSONObject(response);
                            JSONArray jsonRoutes = result.getJSONArray("routes");
                            JSONObject jsonRoute,jsonUserArr;

                            JSONArray routeJsonArray = new JSONArray();
                            JSONArray userInfoJsonArray = new JSONArray();

                            for (int i=0;i<jsonRoutes.length();i++){
                                jsonRoute = new JSONObject(jsonRoutes.get(i).toString());
                                jsonUserArr = jsonRoute.getJSONObject("UserInfo");

                                routeJsonArray.put(jsonRoute.toString());
                                userInfoJsonArray.put(jsonUserArr.toString());

                            }


                            Log.d(TAG,"jsonRoute: "+routeJsonArray);
                            Log.d(TAG,"jsonUser: "+userInfoJsonArray);

                            listHeaderData = new ArrayList<String>();
                            JSONArray jArray = routeJsonArray;
                            if (jArray != null) {
                                for (int i=0;i<jArray.length();i++){
                                    try {
                                        listHeaderData.add(jArray.getString(i));

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            List<String> userInfo = new ArrayList<String>();
                            listChildData = new HashMap<String,List<String>>();
                            JSONArray jArray2 = userInfoJsonArray;
                            if (jArray2 != null) {
                                for (int i=0;i<jArray2.length();i++){
                                    try {
                                        userInfo.add(jArray2.getString(i));
                                        listChildData.put(routeJsonArray.get(i).toString(),userInfo);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }


                            /*Customer Id için request */
                            StringRequest jsonObjRequest3 = new StringRequest(Request.Method.POST,
                                    url2,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject d3 = new JSONObject(response.toString());
                                                CustomerTruckActivity.customid = d3.getString("CustomerID");
                                                Log.d(TAG, "customid: " + CustomerTruckActivity.customid);
                                                // productArray = getMyProduct(CustomerTruckActivity.customid,ApiKey);


                                                /**array request yapılıyor*/
                                                StringRequest jsonObjRequest2 = new StringRequest(Request.Method.POST,
                                                        url3,
                                                        new Response.Listener<String>() {
                                                            @Override
                                                            public void onResponse(String response) {

                                                                try {
                                                                    JSONObject k1 = new JSONObject(response);
                                                                    CustomerTruckActivity.productArray = new JSONArray(k1.getString("AllProducts"));
                                                                   Log.d(TAG,"ALL PRODUCTS OF USER not on route"+ CustomerTruckActivity.productArray);
                                                                    Log.d(TAG,"ListHeader"+listHeaderData);
                                                                    Log.d(TAG,"ListChild"+listChildData);

                                                                    truckAdapter = new JSONAdapterForTrucks(CustomerTruckActivity.this,listHeaderData,listChildData,ApiKey,productArray);
                                                                    lstTest.setAdapter(truckAdapter);
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
                                                        Log.d(TAG, "Apikey on product function: " + ApiKey);

                                                        return params;
                                                    }

                                                    @Override
                                                    protected Map<String, String> getParams() throws AuthFailureError {
                                                        Map<String, String> params = new HashMap<String, String>();
                                                        params.put("CustomerID", CustomerTruckActivity.customid);
                                                        return params;
                                                    }

                                                };
                                                MySingleton.getmInstance(getApplicationContext()).addToRequestque(jsonObjRequest2);




                                                /**array request bitiyor */


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
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("UserMail", usermail);
                                    Log.d(TAG,"USermail on params:" +usermail);


                                    return params;
                                }

                            };
                            MySingleton.getmInstance(CustomerTruckActivity.this).addToRequestque(jsonObjRequest3);


                            Log.d(TAG,"cıkd"+CustomerTruckActivity.customid);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("authorization", ApiKey);
                Log.d(TAG,"Apikey on header"+ApiKey);


                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

        };
        MySingleton.getmInstance(CustomerTruckActivity.this).addToRequestque(jsonObjRequest);



       /* lstTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject dnm1 = (JSONObject) lstTest.getItemAtPosition(position);
                Log.d(TAG,"ProductDetails: " +dnm1);
                try {
                    String textDetail = dnm1.getString("ProductDetails");
                    Log.d(TAG,"ProductDetails: " +textDetail);
                } catch (JSONException e) {
                    e.printStackTrace();
                }




            }
        });*/


    }
    /**  public JSONArray getMyProduct(String customId,String apikey){
     final String akey = apikey;
     final String cid = customId;


     return abc;
     }


     /**   public String getMyCustomerId(String usermailow){
     final String usermailo = usermailow;

     StringRequest jsonObjRequest3 = new StringRequest(Request.Method.POST,
     url2,
     new Response.Listener<String>() {
    @Override
    public void onResponse(String response) {
    try {
    JSONObject d3 = new JSONObject(response.toString());
    CustomerTruckActivity.customid = d3.getString("CustomerID");
    Log.d(TAG, "customid: " + CustomerTruckActivity.customid);
    } catch (JSONException e) {
    e.printStackTrace();
    }


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
     params.put("UserMail", usermailo);


     return params;
     }

     };
     MySingleton.getmInstance(CustomerTruckActivity.this).addToRequestque(jsonObjRequest3);
     return CustomerTruckActivity.customid;
     }*/
    public void showDialog(Activity activity, String msg) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_carrier);

        TextView text = (TextView) dialog.findViewById(R.id.dialog_text);
        text.setText(msg);

        Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialog_cancel);
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Button dialogButtonOk = (Button) dialog.findViewById(R.id.dialog_ok);
        dialogButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

        dialog.show();

    }
}