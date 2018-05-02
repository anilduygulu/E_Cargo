package com.ecargo.ecargo_grid;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierCarsActivity;
import com.google.android.gms.common.api.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class JSONAdapterForListCar extends BaseAdapter implements ListAdapter {
    public final Activity activity;
    public final JSONArray jsonArray;
    public String ApiKey;
    String url = "https://ecargo.info/Api/v1/cars/deleteCar";
    String TAG = "JSONAdapterForListCar activity";
    static String CarID;
    public JSONAdapterForListCar(Activity activity, JSONArray jsonArray, String ApiKey) {
        assert activity != null;
        assert jsonArray != null;
        this.jsonArray = jsonArray;
        this.activity = activity;
        this.ApiKey = ApiKey;
    }


    @Override public int getCount() {
        if(null==jsonArray)
            return 0;
        else
            return jsonArray.length();
    }

    @Override public JSONObject getItem(int position) {
        if(null==jsonArray) return null;
        else
            return jsonArray.optJSONObject(position);
    }

    @Override public long getItemId(int position) {
        JSONObject jsonObject = getItem(position);

        return jsonObject.optLong("id");
    }

    @Override public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = activity.getLayoutInflater().inflate(R.layout.rowforalltruck, null);


        TextView car_plate =(TextView)convertView.findViewById(R.id.car_plate);
        TextView car_brand =(TextView)convertView.findViewById(R.id.car_brand);
        TextView car_model =(TextView)convertView.findViewById(R.id.car_model);
        TextView carDeleteIcon =(TextView)convertView.findViewById(R.id.delete_icon);

        JSONObject json_data = getItem(position);
        if(null!=json_data ){
            String car_plate1= null;
            String car_brand1 = null;
            String car_model1 = null;
            try {
                car_plate1 = json_data.getString("CarPlate");
                car_brand1 = json_data.getString("CarBrand");
                car_model1 = json_data.getString("CarModel");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            car_plate.setText(car_plate1);
            car_brand.setText(car_brand1);
            car_model.setText(car_model1);


        }
        carDeleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json_data = getItem(position);
                try {
                    CarID = json_data.getString("CarID");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                StringRequest jsonObjRequest2 = new StringRequest(Request.Method.POST,
                        url,
                        new Response.Listener<String>() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG,"Delete Response"+response);
                                try {
                                    JSONObject sonuc = new JSONObject(response.toString());
                                    if(sonuc.getString("error").equals("false")) {
                                        jsonArray.remove(position);
                                        CarrierCarsActivity.jsonAdapterForListCar.notifyDataSetChanged();
                                        Toast.makeText(activity,sonuc.getString("message"),Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(activity,sonuc.getString("message"),Toast.LENGTH_LONG).show();
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
                        Log.d(TAG, "Apikey on product function: " + ApiKey);

                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("CarID",CarID);
                        Log.d(TAG,"CarID"+CarID);
                        return params;
                    }

                };
                MySingleton.getmInstance(activity).addToRequestque(jsonObjRequest2);
            }
        });

        return convertView;
    }
}
