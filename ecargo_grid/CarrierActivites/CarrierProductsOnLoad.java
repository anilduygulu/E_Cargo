package com.ecargo.ecargo_grid.CarrierActivites;

import android.app.Dialog;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.AdapterCustomerForAllProduct;
import com.ecargo.ecargo_grid.AdapterForCarrierProductsOnLoad;
import com.ecargo.ecargo_grid.CustomerActivites.CustomerProductActivity;
import com.ecargo.ecargo_grid.JSONAdapter;
import com.ecargo.ecargo_grid.MySingleton;
import com.ecargo.ecargo_grid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarrierProductsOnLoad extends AppCompatActivity {


    final String TAG = "CarrierProductOnLOAD";
    String url = "https://ecargo.info/Api/v1/cars/ListCars";
    String url2 = "https://ecargo.info/Api/v1/routes/ListRoutesByCarPlate";
    String url3 = "https://ecargo.info/Api/v1/products/getProductByRoute";
    String url4 = "https://ecargo.info/Api/v1/services/deleteRelation";
    static String carrierID,ApiKey,usermail;
    JSONObject jsonObj;
    AdapterForCarrierProductsOnLoad adapterForCarrierProductsOnLoad;
    static Spinner spcar,sproute,spProducts;
    static JSONArray jsonDataset1;
    static JSONArray jsonDataset2;
    List<String> spinnerArray = new ArrayList<String>();
    List<String> spinnerArray2 = new ArrayList<String>();
    static ArrayAdapter<String> adapter;
    static ArrayAdapter<String> adapter2;
    static String routeid;
    JSONAdapter jSONAdapter;
    ListView lstTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carrier_products_on_load);
        spcar = (Spinner) findViewById(R.id.spinner_cars);
        sproute = (Spinner) findViewById(R.id.spinner_route);
        lstTest = (ListView) findViewById(R.id.product_listView1);



        try {
            jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
            ApiKey = jsonObj.getString("apiKey");
            usermail = jsonObj.getString("UserMail");
            carrierID = jsonObj.getString("CarrierID");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: " + response);
                        try {
                            JSONObject result = new JSONObject(response);

                            jsonDataset1 = result.getJSONArray("cars");


                            Log.d(TAG, "Spinner" + spinnerArray);

                            for (int i = 0; i < jsonDataset1.length(); i++) {
                                JSONObject result2 = new JSONObject(jsonDataset1.get(i).toString());

                                spinnerArray.add(result2.getString("CarPlate"));
                            }
                            Log.d(TAG, "spinnerArray: " + spinnerArray);

                            adapter = new ArrayAdapter<String>(CarrierProductsOnLoad.this, android.R.layout.simple_spinner_item, spinnerArray);
                            CarrierProductsOnLoad.adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                            CarrierProductsOnLoad.spcar.setAdapter(CarrierProductsOnLoad.adapter);

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
                params.put("CarrierID", carrierID);
                return params;
            }

        };
        MySingleton.getmInstance(CarrierProductsOnLoad.this).addToRequestque(jsonObjRequest);
        spcar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.d(TAG,"clicked"+spcar.getSelectedItem());
                spinnerArray2.clear();


                StringRequest jsonObjRequest1 = new StringRequest(Request.Method.POST,
                        url2,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject result1 = new JSONObject(response);

                                    jsonDataset2 = result1.getJSONArray("routes");

                                    for (int i=0; i<jsonDataset2.length(); i++) {
                                        JSONObject actor = jsonDataset2.getJSONObject(i);
                                        routeid  = actor.getString("RouteID");

                                    }

                                    Log.d(TAG, "routeid: " + routeid);


                                    Log.d(TAG, "Spinner" + spinnerArray2);

                                    for (int i = 0; i < jsonDataset2.length(); i++) {
                                        JSONObject result2 = new JSONObject(jsonDataset2.get(i).toString());

                                        spinnerArray2.add(result2.getString("RouteFrom")+"-"+result2.getString("RouteTo"));
                                    }
                                    Log.d(TAG, "spinnerArray2: " + spinnerArray2);

                                    adapter2 = new ArrayAdapter<String>(CarrierProductsOnLoad.this, android.R.layout.simple_spinner_item, spinnerArray2);
                                    CarrierProductsOnLoad.adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                    CarrierProductsOnLoad.sproute.setAdapter(CarrierProductsOnLoad.adapter2);

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
                        params.put("CarPlate", spcar.getSelectedItem().toString());
                        return params;
                    }

                };
                MySingleton.getmInstance(CarrierProductsOnLoad.this).addToRequestque(jsonObjRequest1);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


        sproute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                        url3,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "ResponseAFTER: " + response.toString());
                                try {
                                    JSONObject abc = new JSONObject(response);
                                    JSONArray jsonArray = new JSONArray(abc.getString("products"));
                                    jSONAdapter = new JSONAdapter(CarrierProductsOnLoad.this, jsonArray);//jArray is your json array

                                    //Set the above adapter as the adapter of choice for our list
                                    lstTest.setAdapter(jSONAdapter);
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
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("authorization", ApiKey);
                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("RouteID", routeid);
                        return params;
                    }

                };
                MySingleton.getmInstance(CarrierProductsOnLoad.this).addToRequestque(jsonObjRequest);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        lstTest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final JSONObject dnm1 = (JSONObject) lstTest.getItemAtPosition(position);
                Log.d(TAG, "ProductDetails: " + dnm1);
                try {
                    String textDetail = dnm1.getString("ProductDetails");
                    Log.d(TAG, "ProductDetails: " + textDetail);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final Dialog dialog = new Dialog(CarrierProductsOnLoad.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialogcloserelation);


               Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialog_cancel3);
                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                Button dialogButtonOk = (Button) dialog.findViewById(R.id.dialog_ok3);
                dialogButtonOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                                url4,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d(TAG, "ResponseAFTERdeleted: " + response);



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
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("authorization", ApiKey);
                                return params;
                            }

                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                try {
                                    params.put("RouteID", routeid);
                                    params.put("CustomerID", dnm1.getString("CustomerID"));
                                    params.put("ProductName", dnm1.getString("ProductName"));
                                    params.put("ProductID", dnm1.getString("ProductID"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                return params;
                            }

                        };
                        MySingleton.getmInstance(CarrierProductsOnLoad.this).addToRequestque(jsonObjRequest);

                        dialog.dismiss();
                    }
                });



                dialog.show();


            }
        });

    }
    }

