package com.ecargo.ecargo_grid.CarrierActivites;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.CustomerActivites.CustomerProductActivity;
import com.ecargo.ecargo_grid.JSONAdapter;
import com.ecargo.ecargo_grid.JSONAdapterForListCar;
import com.ecargo.ecargo_grid.Login_Fragment;
import com.ecargo.ecargo_grid.MySingleton;
import com.ecargo.ecargo_grid.R;
import com.ecargo.ecargo_grid.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CarrierCarsActivity extends AppCompatActivity {
    String url = "https://ecargo.info/Api/v1/cars/addNewCar";
    String url2 = "https://ecargo.info/Api/v1/cars/ListCars";
    String TAG = "Carrier Car Activity";
    Button carButton;
    JSONObject jsonObj =null;
    static String userMail=null;
    static String carrierId;
    String ApiKey;
    static JSONObject responseObj;
    public static JSONAdapterForListCar jsonAdapterForListCar;
    private static Animation shakeAnimation;
    public static JSONArray productArray;

    EditText input_CarPlate,input_CarBrand;
    Spinner input_CarModel;
    ListView listView;
    LinearLayout addCarLayout;

    String CarPlate,CarBrand,CarModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.carrier_cars);


        carButton = (Button) findViewById(R.id.add_carButton);
        input_CarPlate = (EditText) findViewById(R.id.input_plate);
        input_CarBrand = (EditText) findViewById(R.id.input_brand);
        input_CarModel = (Spinner) findViewById(R.id.input_model);
        listView = (ListView) findViewById(R.id.cars_listView);
        shakeAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake);
        addCarLayout = (LinearLayout) findViewById(R.id.add_car_layout);

        Intent intent = getIntent();
        try {
            jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
            Log.d(TAG,"JsonObj"+jsonObj);
            userMail = jsonObj.getString("UserMail");
            ApiKey = jsonObj.getString("apiKey");
            carrierId = jsonObj.getString("CarrierID");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        /************************ListCarRequest***************************************************/
        StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: " + response);
                        try {

                            responseObj = new JSONObject(response);
                            Log.d(TAG, "responseObj: " + responseObj);
                            String pArray = responseObj.getString("cars");
                            productArray = new JSONArray(pArray);
                            jsonAdapterForListCar = new JSONAdapterForListCar(CarrierCarsActivity.this,productArray,ApiKey);//jArray is your json array

                            //Set the above adapter as the adapter of choice for our list
                            listView.setAdapter(jsonAdapterForListCar);
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
                params.put("CarrierID",carrierId);

                return params;
            }

        };
        MySingleton.getmInstance(CarrierCarsActivity.this).addToRequestque(jsonObjRequest);

        carButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
                int valid = checkValidation();
                if(valid == 1){
                    StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                            url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d(TAG, "Response: " + response.toString());
                                    JSONObject abc = null;
                                    try {
                                        abc = new JSONObject(response);

                                    String error1 = abc.getString("error");
                                    if (error1.equals("true")) {


                                        Toast.makeText(CarrierCarsActivity.this, abc.getString("message"),
                                                Toast.LENGTH_LONG).show();

                                    } else if (error1.equals("false")) {
                                        String CarID = abc.getString("CarID");
                                        String CarrierID = carrierId;
                                        String CarPlate = input_CarPlate.getText().toString();
                                        String CarBrand =input_CarBrand.getText().toString();
                                        String CarModel = input_CarModel.getSelectedItem().toString();

                                        JSONObject newRoute = new JSONObject();
                                        newRoute.put("CarID",CarID);
                                        newRoute.put("CarrierID",CarrierID);
                                        newRoute.put("CarPlate",CarPlate);
                                        newRoute.put("CarBrand",CarBrand);
                                        newRoute.put("CarModel",CarModel);
                                        Log.d(TAG,"New Car: "+newRoute);

                                        productArray.put(newRoute);
                                        Log.d(TAG,"Product Array: "+productArray);
                                        jsonAdapterForListCar.notifyDataSetChanged();
                                    }

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
                            params.put("CarrierID", carrierId);
                            params.put("CarBrand", input_CarBrand.getText().toString());
                            params.put("CarPlate", input_CarPlate.getText().toString());
                            params.put("CarModel", input_CarModel.getSelectedItem().toString());



                            Log.d(TAG, carrierId + " "+input_CarBrand.getText().toString() +  input_CarPlate.getText().toString() + input_CarModel.getSelectedItem().toString() );
                            return params;
                        }

                    };
                    MySingleton.getmInstance(getApplicationContext()).addToRequestque(jsonObjRequest);

                }


            }

        });



    }

    private int checkValidation() {
        // Get email id and password
        String input_model1 = input_CarModel.getSelectedItem().toString();
        String input_plate11 = input_CarPlate.getText().toString();
        String input_brand1 = input_CarBrand.getText().toString();

        // Check patter for email id
        Pattern p = Pattern.compile(Utils.regEx);


        // Check for both field is empty or not
        if (input_plate11.equals("") || input_brand1.equals("") || input_model1.equals("Model"))
        {
            addCarLayout.startAnimation(shakeAnimation);
            Toast.makeText(getApplicationContext(),"Enter all credentials.",Toast.LENGTH_LONG).show();
            return 0;

        }

        else
            return 1;

    }

}
