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
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class JSONAdapterForTrucks extends BaseExpandableListAdapter {
    private Context _context;
    private List<String> _listDataHeader; // header titles
    private HashMap<String, List<String>> _listDataChild;
    String url = "https://ecargo.info/Api/v1/notifications/AddNotificationCustomer";
    final String TAG = "JSONADAPTER";
    final String ApiKey;
    final JSONArray prArray;
    String carid,productid,customerid,carrierid,routeid,carPlate;
    static int grouppos;

    // final String customerID,productID,carrierID,carID;
    public JSONAdapterForTrucks(Context context, List<String> listDataHeader,HashMap<String, List<String>> listChildData,String ApiKey,JSONArray pArray) {
        this._context = context;
        this._listDataHeader = listDataHeader;
        this._listDataChild = listChildData;
        this.ApiKey = ApiKey;
        this.prArray = pArray;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        JSONObject qwe = null;
        grouppos = groupPosition;

        try {
            qwe = new JSONObject((String) getGroup(groupPosition));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject childData = (JSONObject) qwe;

        //final JSONObject childData = (JSONObject) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.row3child, null);
        }
        TextView phoneNumber = (TextView) convertView.findViewById(R.id.phoneNumber);
        TextView driver_name = (TextView) convertView.findViewById(R.id.driver_name);
        TextView driver_surname = (TextView) convertView.findViewById(R.id.driver_surname);
        Button buttonIstek = (Button) convertView.findViewById(R.id.istekButton);

        buttonIstek.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(_context);
                final Spinner sp = new Spinner(_context);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(_context,android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                adapter.add("Select a Product..");
              Log.d(TAG,"ADAPTER ıcınde Customer products"+prArray);

                Log.d(TAG,"ADAPTER ıcınde header"+_listDataHeader);
                Log.d(TAG,"ADAPTER ıcınde child"+_listDataChild);
             for (int i = 0; i < prArray.length(); ++i)
                {

                    try {
                        String jsonStr = prArray.getString(i);
                        JSONObject myJsonObj = new JSONObject(jsonStr);
                        adapter.add(myJsonObj.getString("ProductName"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }


                sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
               sp.setAdapter(adapter);
                builder.setView(sp);
                builder.setMessage("Talep Etmek İstediğiniz Ürünü Seçin?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String text = sp.getSelectedItem().toString();
                                Log.d(TAG,"TEXT"+text);
                                for(int i=0;i<prArray.length();i++) {
                                    try {
                                        if(prArray.getJSONObject(i).getString("ProductName").equals(text)){
                                            productid =   prArray.getJSONObject(i).getString("ProductID");
                                            customerid =  prArray.getJSONObject(i).getString("CustomerID");
                                            Log.d(TAG,"proid ve cus id"+productid+customerid);
                                            JSONObject abc = new JSONObject(getGroup(grouppos).toString());
                                            routeid = abc.getString("RouteID");
                                            carid = abc.getString("CarID");
                                            carrierid = abc.getString("CarrierID");
                                            Log.d(TAG,"infolar "+productid+" "+customerid+" "+carid+" "+carrierid+" "+routeid);

                                            /** Notification eklencek request yapılıyor*/
                                            StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                                                    url,
                                                    new Response.Listener<String>() {
                                                        @Override
                                                        public void onResponse(String response) {
                                                            Log.d(TAG,"RESPONSE NOTF"+response);


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
                                                    params.put("CustomerID", customerid);
                                                    params.put("ProductID", productid);
                                                    params.put("CarID", carid);
                                                    params.put("CarrierID", carrierid);
                                                    params.put("RouteID", routeid);
                                                    Log.d(TAG,"INFO PARAMS "+productid+" "+customerid+" "+carid+" "+carrierid+" "+routeid);

                                                    return params;
                                                }

                                            };
                                            MySingleton.getmInstance(_context).addToRequestque(jsonObjRequest);
                                            /** End of notification request*/
                                        }
                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }

                                }



                                notifyDataSetChanged();
                            }
                        });
                builder.setNegativeButton("Vazgeç",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();


                }
        });

        try {
            phoneNumber.setText(childData.getJSONObject("UserInfo").getString("UserPhoneNumber"));
            driver_name.setText(childData.getJSONObject("UserInfo").getString("UserName"));
            driver_surname.setText(childData.getJSONObject("UserInfo").getString("UserSurname"));
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        JSONObject asd = null;
        try {
            asd = new JSONObject((String) getGroup(groupPosition));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject headerData = (JSONObject) asd;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.row3, null);
        }

        TextView carPlate = (TextView) convertView.findViewById(R.id.carID);
        TextView route_from = (TextView) convertView.findViewById(R.id.route_from);
        TextView route_to = (TextView) convertView.findViewById(R.id.route_to);

        try {
            carPlate.setText(headerData.getString("CarID"));
            route_from.setText(headerData.getString("RouteFrom"));
            route_to.setText(headerData.getString("RouteTo"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}