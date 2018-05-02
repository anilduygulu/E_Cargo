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

public class AdapterForCarrierProductsOnLoad extends BaseAdapter implements ListAdapter {

    public final Activity activity;
    public final JSONArray jsonArray;
    public final String ApiKey;

    static String pID;

    String TAG = "AdapterForCarrierOnLAd";

    public AdapterForCarrierProductsOnLoad(Activity activity, JSONArray jsonArray,String ApiKey) {
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
            convertView = activity.getLayoutInflater().inflate(R.layout.row_for_on_load, null);

        TextView routefrom = (TextView) convertView.findViewById(R.id.rowroutefrom);
        TextView routeto = (TextView) convertView.findViewById(R.id.rowrouteto);
        TextView routedate = (TextView) convertView.findViewById(R.id.rowroutedate);


        JSONObject json_data = getItem(position);
        if(null!=json_data ){
            String routefrom1= null;
            String routeto1 = null;
            String routedate1 = null;

            try {
                routefrom1 = json_data.getString("RouteFrom");
                routeto1 = json_data.getString("RouteDate");
                routedate1 = json_data.getString("RouteDate");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            routefrom.setText(routefrom1);
            routeto.setText(routeto1);
            routedate.setText(routedate1);
        }

        return convertView;
    }
}