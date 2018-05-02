package com.ecargo.ecargo_grid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.CustomerActivites.CustomerTruckActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterCustomerForAllProduct extends BaseAdapter implements ListAdapter {

    public final Activity activity;
    public final JSONArray jsonArray;
    public final String ApiKey;

    static String pID;

    String TAG = "AdapterForAllProducts";

    public AdapterCustomerForAllProduct(Activity activity, JSONArray jsonArray,String ApiKey) {
        assert activity != null;
        assert jsonArray != null;
        assert  ApiKey !=null;

        this.jsonArray = jsonArray;
        this.activity = activity;
        this.ApiKey = ApiKey;
    }

    @Override
    public int getCount() {
        if(null==jsonArray)
            return 0;
        else
            return jsonArray.length();
    }

    @Override
    public JSONObject getItem(int position) {
        if(null==jsonArray) return null;
        else
            return jsonArray.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        JSONObject jsonObject = getItem(position);

        return jsonObject.optLong("id");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = activity.getLayoutInflater().inflate(R.layout.rowforallproduct, null);

        TextView productName = (TextView) convertView.findViewById(R.id.product_name_allProducts);
        TextView productFrom = (TextView) convertView.findViewById(R.id.product_from_allProducts);
        TextView productTo = (TextView) convertView.findViewById(R.id.product_to_allProducts);


        JSONObject json_data = getItem(position);
        if(null!=json_data ){
            String pname1= null;
            String from1 = null;
            String to1 = null;
            pID =null;
            try {
                pname1 = json_data.getString("ProductName");
                pID = json_data.getString("ProductID");
                from1 = json_data.getString("ProductFrom");
                to1 = json_data.getString("ProductTo");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            productName.setText(pname1);
            productFrom.setText(from1);
            productTo.setText(to1);
        }

        return convertView;
    }
}