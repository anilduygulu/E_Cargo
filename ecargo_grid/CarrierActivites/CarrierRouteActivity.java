package com.ecargo.ecargo_grid.CarrierActivites;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.ecargo.ecargo_grid.CarrierActivity;
import com.ecargo.ecargo_grid.JSONAdapterForListCar;
import com.ecargo.ecargo_grid.JSONAdapterForListRoutes;
import com.ecargo.ecargo_grid.MySingleton;
import com.ecargo.ecargo_grid.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CarrierRouteActivity extends AppCompatActivity {

    static Spinner carSpinner, fromSpinner, toSpinner;
    static String carrierId;
    static TextView departureDate;
    static JSONArray jsonDataset1;
    static Calendar calendar;
    static ArrayAdapter<String> adapter;
    final String TAG = "Carrier Route Activity";
    String url = "https://ecargo.info/Api/v1/cars/ListCars";
    String url2 = "https://ecargo.info/Api/v1/getTypeIDCarr";
    String url3 = "https://ecargo.info/Api/v1/carrier/CreateRoute";
    String url4 = "https://ecargo.info/Api/v1/routes/ListRoutes";
    String ApiKey;
    String userMail;
    JSONObject jsonObj;
    static JSONObject responseObj;
    String carId;
    Button addRoute;
    JSONObject abc;
    int gun, ay, yil;
    List<String> spinnerArray = new ArrayList<String>();
    Button cancelRoute, confirmRoute;
    public static JSONAdapterForListRoutes jsonAdapterForListRoutes;
    public static int flag;
    public static JSONArray productArray;
    ListView listView;

    public static String DBtarih(String str) {
        try {
            str = str.replaceAll(" ", "");
            str = str + " 00:00:00";
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = (Date) formatter.parse(str);
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String finalString = newFormat.format(date);

            return finalString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.carrier_route);
        addRoute = (Button) findViewById(R.id.btn_addRoute);
        CarrierRouteActivity.fromSpinner = (Spinner) findViewById(R.id.input_fromSpinner);
        CarrierRouteActivity.toSpinner = (Spinner) findViewById(R.id.input_toSpinner);
        calendar = Calendar.getInstance();
        listView = (ListView) findViewById(R.id.routes_listView);

        departureDate = (TextView) this.findViewById(R.id.add_route);
        departureDate.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR));


        departureDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                yil = calendar.get(Calendar.YEAR);
                ay = calendar.get(Calendar.MONTH) + 1;
                gun = calendar.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(CarrierRouteActivity.this, new DatePickerDialog.OnDateSetListener() {


                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {


                        departureDate.setText(" " + dayOfMonth + " / " + (month) + " / " + year);


                    }

                }, yil, ay, gun);
                datePickerDialog.show();


            }
        });


        try {
            jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
            Log.d(TAG, "JsonObj" + jsonObj);
            ApiKey = jsonObj.getString("apiKey");
            userMail = jsonObj.getString("UserMail");
            carrierId = jsonObj.getString("CarrierID");


        } catch (JSONException e) {
            e.printStackTrace();
        }

        StringRequest jsonObjRequest1 = new StringRequest(Request.Method.POST,
                url2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        try {
                            abc = new JSONObject(response);

                            CarrierRouteActivity.carrierId = abc.getString("CarrierID");
                            StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                                    url,
                                    new Response.Listener<String>() {
                                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                        @Override
                                        public void onResponse(String response) {

                                            try {
                                                JSONObject result = new JSONObject(response);

                                                jsonDataset1 = result.getJSONArray("cars");


                                                Log.d(TAG, "Spinner" + spinnerArray);

                                                for (int i = 0; i < jsonDataset1.length(); i++) {
                                                    JSONObject result2 = new JSONObject(jsonDataset1.get(i).toString());
                                                    Log.d(TAG, "plate: " + result2.getString("CarPlate"));
                                                    spinnerArray.add(result2.getString("CarPlate"));
                                                }


                                                adapter = new ArrayAdapter<String>(CarrierRouteActivity.this, android.R.layout.simple_spinner_item, spinnerArray);
                                                CarrierRouteActivity.adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                CarrierRouteActivity.carSpinner = (Spinner) findViewById(R.id.input_carsSpinner);
                                                CarrierRouteActivity.carSpinner.setAdapter(CarrierRouteActivity.adapter);

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
                                    Log.d(TAG, "KEY1: " + CarrierRouteActivity.carrierId);
                                    params.put("CarrierID", CarrierRouteActivity.carrierId);
                                    return params;
                                }

                            };
                            MySingleton.getmInstance(getApplicationContext()).addToRequestque(jsonObjRequest);


                            Log.d(TAG, "Carrier ID->" + CarrierRouteActivity.carrierId);


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
        }) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("UserMail", userMail);


                return params;
            }

        };
        MySingleton.getmInstance(getApplicationContext()).addToRequestque(jsonObjRequest1);


        StringRequest jsonObjRequest4 = new StringRequest(Request.Method.POST,
                url4,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: " + response.toString());
                        try {
                            responseObj = new JSONObject(response);
                            Log.d(TAG, "responseObj: " + responseObj);
                            String pArray = responseObj.getString("routes");
                            productArray = new JSONArray(pArray);
                            jsonAdapterForListRoutes = new JSONAdapterForListRoutes(CarrierRouteActivity.this,productArray,ApiKey);//jArray is your json array
                            //Set the above adapter as the adapter of choice for our list
                            listView.setAdapter(jsonAdapterForListRoutes);

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
                return params;
            }

        };
        MySingleton.getmInstance(getApplicationContext()).addToRequestque(jsonObjRequest4);




        addRoute.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if(carSpinner.getSelectedItem() == null){
                    Toast.makeText(getApplicationContext(),"You don't have a car.",Toast.LENGTH_LONG).show();
                }else{


                final Dialog dialog = new Dialog(CarrierRouteActivity.this);
                dialog.setContentView(R.layout.confirmation_dialog);
                dialog.setTitle("Confirmation");
                TextView textDia= (TextView) dialog.findViewById(R.id.confirmation_dialog_text);
                textDia.setText("Do you want to add route?");


                cancelRoute = (Button) dialog.findViewById(R.id.cofirmation_dialog_cancel);
                // if button is clicked, cancel adding route
                cancelRoute.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog.dismiss();

                    }
                });


                confirmRoute = (Button) dialog.findViewById(R.id.confirmation_dialog_ok);
                // if button is clicked, confirm  adding route
                confirmRoute.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StringRequest jsonObjRequest2 = new StringRequest(Request.Method.POST,
                                url3,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d(TAG, "ResponseAFTERBUTTON: " + response.toString());

                                        JSONObject abc = null;
                                        try {
                                            abc = new JSONObject(response);
                                            String error1 = abc.getString("error");
                                            if (error1.equals("true")) {


                                                Toast.makeText(CarrierRouteActivity.this, abc.getString("message"),
                                                        Toast.LENGTH_LONG).show();

                                            } else if (error1.equals("false")) {
                                                Toast.makeText(CarrierRouteActivity.this, abc.getString("message"),
                                                        Toast.LENGTH_LONG).show();
                                                String RouteID = abc.getString("RouteID");
                                                String RouteFrom = CarrierRouteActivity.fromSpinner.getSelectedItem().toString();
                                                String RouteTo = CarrierRouteActivity.toSpinner.getSelectedItem().toString();
                                                String RouteDate = DBtarih(CarrierRouteActivity.departureDate.getText().toString());

                                                JSONObject newRoute = new JSONObject();
                                                newRoute.put("RouteID",RouteID);
                                                newRoute.put("RouteFrom",RouteFrom);
                                                newRoute.put("RouteTo",RouteTo);
                                                newRoute.put("RouteDate",RouteDate);
                                                Log.d(TAG,"New Route: "+newRoute);

                                                productArray.put(newRoute);
                                                Log.d(TAG,"Product Array: "+productArray);
                                                jsonAdapterForListRoutes.notifyDataSetChanged();
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
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("authorization", ApiKey);

                                return params;
                            }

                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("CarID", carId);
                                params.put("RouteFrom", CarrierRouteActivity.fromSpinner.getSelectedItem().toString());
                                params.put("RouteTo", CarrierRouteActivity.toSpinner.getSelectedItem().toString());
                                Log.d(TAG, "PARAMS: " +carId+" "+CarrierRouteActivity.fromSpinner.getSelectedItem().toString()+" "+ CarrierRouteActivity.toSpinner.getSelectedItem().toString() +" "+DBtarih(CarrierRouteActivity.departureDate.getText().toString()));
                                params.put("RouteDate", DBtarih(CarrierRouteActivity.departureDate.getText().toString()));

                                return params;
                            }

                        };
                        MySingleton.getmInstance(getApplicationContext()).addToRequestque(jsonObjRequest2);

                        dialog.dismiss();
                    }
                });

                dialog.show();






                    String item = carSpinner.getSelectedItem().toString();
                    for (int i = 0; i < jsonDataset1.length(); i++) {
                        try {
                            JSONObject result3 = new JSONObject(jsonDataset1.get(i).toString());
                            if (result3.getString("CarPlate").equals(item)) {
                                carId = result3.getString("CarID");
                                Log.d(TAG, "Spinner: " + CarrierRouteActivity.fromSpinner.getSelectedItem().toString());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }




                }



        });



    }

}
