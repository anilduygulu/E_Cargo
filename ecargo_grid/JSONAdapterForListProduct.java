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
import com.ecargo.ecargo_grid.CustomerActivites.CustomerAddProductActivity;
import com.google.android.gms.common.api.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class JSONAdapterForListProduct extends BaseAdapter implements ListAdapter {
    public final Activity activity;
    public final JSONArray jsonArray;
    public String ApiKey;
    String url = "https://ecargo.info/Api/v1/products/deleteProduct";
    String TAG = "JSONAdapterForListProduct activity";
    static String productID;
    public JSONAdapterForListProduct(Activity activity, JSONArray jsonArray, String ApiKey) {
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
            convertView = activity.getLayoutInflater().inflate(R.layout.rowforallproductonlist, null);


        TextView pname =(TextView)convertView.findViewById(R.id.pro_name);
        TextView pfrom =(TextView)convertView.findViewById(R.id.pro_from);
        TextView pto =(TextView)convertView.findViewById(R.id.pro_to);
        TextView pdetail =(TextView)convertView.findViewById(R.id.pro_detail);
        TextView pDeleteIcon =(TextView)convertView.findViewById(R.id.delete_icon1);

        JSONObject json_data = getItem(position);
        if(null!=json_data ){
            String pname1= null;
            String pfrom1 = null;
            String pto1 = null;
            String pdetail1 = null;
            try {
                pname1 = json_data.getString("ProductName");
                pfrom1 = json_data.getString("ProductFrom");
                pto1 = json_data.getString("ProductTo");
                pdetail1 = json_data.getString("ProductDetails");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            pname.setText(pname1);
            pfrom.setText(pfrom1);
            pto.setText(pto1);
            pdetail.setText(pdetail1);


        }
        pDeleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json_data = getItem(position);
                try {
                    productID = json_data.getString("ProductID");
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
                                        CustomerAddProductActivity.jsonAdapterForListProduct.notifyDataSetChanged();
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
                        params.put("ProductID",productID);
                        Log.d(TAG,"ProductID"+productID);
                        return params;
                    }

                };
                MySingleton.getmInstance(activity).addToRequestque(jsonObjRequest2);
            }
        });

        return convertView;
    }
}
