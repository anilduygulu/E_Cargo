package com.ecargo.ecargo_grid.CarrierActivites;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.JSONAdapter;
import com.ecargo.ecargo_grid.MySingleton;
import com.ecargo.ecargo_grid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class CarrierProductActivites extends AppCompatActivity {
    static String ApiKey, carrID;
    final String TAG = "Carrier Product";
    String url = "https://ecargo.info/Api/v1/products/ListAllProducts";
    String url2 = "https://ecargo.info/Api/v1/notifications/AddNotificationCarrier";
    ListView lstTest;
    JSONAdapter jSONAdapter;
    JSONObject jsonObj;
    SearchView sv;
    static JSONArray productArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carrier_products);
        lstTest = (ListView) findViewById(R.id.product_listView);
       sv = (SearchView) findViewById(R.id.searchView1);

        try {
            jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
            Log.d(TAG, "JsonObj" + jsonObj);
            ApiKey = jsonObj.getString("apiKey");
            carrID = jsonObj.getString("CarrierID");
            //  userMail = jsonObj.getString("UserMail");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "APikey: " + ApiKey);

        StringRequest jsonObjRequest = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: " + response.toString());
                        try {


                            JSONObject result = new JSONObject(response);
                            String pArray = result.getString("products");
                            productArray = new JSONArray(pArray);
                            jSONAdapter = new JSONAdapter(CarrierProductActivites.this, productArray);//jArray is your json array

                            //Set the above adapter as the adapter of choice for our list
                            lstTest.setAdapter(jSONAdapter);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

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
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("authorization", ApiKey);


                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

        };
        MySingleton.getmInstance(CarrierProductActivites.this).addToRequestque(jsonObjRequest);

     sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                JSONArray changedproductArray= new JSONArray();
                if(newText.length()>2 ){
                    Log.d(TAG,"newTExt"+newText);
                    for(int i=0;i<productArray.length();i++){
                        try {
                            JSONObject jobj = (JSONObject) productArray.get(i);
                            Log.d(TAG,"jobj"+jobj);
                            if(jobj.getString("ProductName").toLowerCase().contains(newText.toLowerCase()) || jobj.getString("ProductFrom").toLowerCase().contains(newText.toLowerCase()) ||jobj.getString("ProductTo").toLowerCase().contains(newText.toLowerCase())){

                               Log.d(TAG,"Name found"+jobj);
                                changedproductArray.put(jobj);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    jSONAdapter = new JSONAdapter(CarrierProductActivites.this, changedproductArray);//jArray is your json array


                    //Set the above adapter as the adapter of choice for our list

                }else{
                    jSONAdapter = new JSONAdapter(CarrierProductActivites.this, productArray);
                }
                lstTest.setAdapter(jSONAdapter);


                return false;
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

                final Dialog dialog = new Dialog(CarrierProductActivites.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_carrier);

                TextView text = (TextView) dialog.findViewById(R.id.dialog_text);
                try {
                    text.setText(dnm1.getString("ProductDetails"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

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
                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialogButtonOk.setOnClickListener(new View.OnClickListener() {
                                                      @Override
                                                      public void onClick(View v) {
                                                          StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                                                                  url2,
                                                                  new Response.Listener<String>() {
                                                                      @Override
                                                                      public void onResponse(String response) {
                                                                          Log.d(TAG, "RESPONSE NOTF" + response);
                                                                          try {
                                                                              JSONObject resp = new JSONObject(response);
                                                                              Toast.makeText(CarrierProductActivites.this, resp.getString("message"), Toast.LENGTH_LONG).show();

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
                                                                  Log.d(TAG, "Apikey on product function: " + ApiKey);

                                                                  return params;
                                                              }

                                                              @Override
                                                              protected Map<String, String> getParams() throws AuthFailureError {
                                                                  Map<String, String> params = new HashMap<String, String>();
                                                                  try {
                                                                      params.put("ProductID", dnm1.getString("ProductID"));
                                                                      params.put("CustomerID", dnm1.getString("CustomerID"));
                                                                      params.put("RouteFrom", dnm1.getString("ProductFrom"));
                                                                      params.put("RouteTo", dnm1.getString("ProductTo"));
                                                                      params.put("CarrierID", carrID);
                                                                      Log.d(TAG, "PARAMS" + dnm1.getString("ProductID") + " " + dnm1.getString("CustomerID") + " " + dnm1.getString("ProductFrom") + " " + dnm1.getString("ProductTo") + " " + carrID);
                                                                  } catch (JSONException e) {
                                                                      e.printStackTrace();
                                                                  }


                                                                  return params;
                                                              }

                                                          };
                                                          MySingleton.getmInstance(getApplicationContext()).addToRequestque(jsonObjRequest);
                                                          dialog.dismiss();
                                                      }

                                                  }
                );

                dialog.show();


            }
        });

    }

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
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialogButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                        url2,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "RESPONSE NOTF" + response);


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
                        Log.d(TAG, "Apikey on product function: " + ApiKey);

                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        //  params.put("CustomerID", customerid);


                        return params;
                    }

                };
                MySingleton.getmInstance(getApplicationContext()).addToRequestque(jsonObjRequest);
            }
        });

        dialog.show();

    }
}
