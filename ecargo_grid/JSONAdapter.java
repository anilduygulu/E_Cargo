package com.ecargo.ecargo_grid;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ecargo.ecargo_grid.CarrierActivites.CarrierProductActivites;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;


public class JSONAdapter extends BaseAdapter implements ListAdapter{

    static String TAG="JSONAdapter";
    public final Activity activity;
    public final JSONArray jsonArray;

 //   CustomFilter filter;
    public JSONAdapter(Activity activity, JSONArray jsonArray) {
        assert activity != null;
        assert jsonArray != null;

        this.jsonArray = jsonArray;
        this.activity = activity;


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


    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = activity.getLayoutInflater().inflate(R.layout.row2, null);



        TextView pname =(TextView)convertView.findViewById(R.id.productName);
        TextView from =(TextView)convertView.findViewById(R.id.from);
        TextView to =(TextView)convertView.findViewById(R.id.to);

        JSONObject json_data = getItem(position);
        if(null!=json_data ){
            String pname1= null;
            String from1 = null;
            String to1 = null;
            try {
                pname1 = json_data.getString("ProductName");
                from1 = json_data.getString("ProductFrom");
                to1 = json_data.getString("ProductTo");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pname.setText(pname1);
            from.setText(from1);
            to.setText(to1);
        }

        return convertView;
    }
}
