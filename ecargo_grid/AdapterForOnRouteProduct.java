package com.ecargo.ecargo_grid;

import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.CustomerActivites.CustomerProductActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class AdapterForOnRouteProduct extends BaseAdapter implements ListAdapter {

    public final Activity activity;
    public final JSONArray jsonArray;
    public final String ApiKey;
    public Button requestButton;
    String url = "https://ecargo.info/Api/v1/location/getLocation";
    String TAG = "AdapterOnRouteProduct";
    static String pID;
    LinearLayout layout_for_product;
    public static String lat,longi;


    public AdapterForOnRouteProduct(Activity activity, JSONArray jsonArray,String ApiKey) {
        assert activity != null;
        assert jsonArray != null;
        assert  ApiKey !=null;

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
            convertView = activity.getLayoutInflater().inflate(R.layout.rowforallproductchild, null);




        LinearLayout layout_for_product = (LinearLayout) convertView.findViewById(R.id.layout_for_product);
        TextView pname =(TextView)convertView.findViewById(R.id.product_name_onadapter);
        TextView from =(TextView)convertView.findViewById(R.id.product_from_onadapter);
        TextView to =(TextView)convertView.findViewById(R.id.product_to_onadapter);


        requestButton = (Button) convertView.findViewById(R.id.locationRequestButton) ;
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
            pname.setText(pname1);
            from.setText(from1);
            to.setText(to1);
        }
        layout_for_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json_data = getItem(position);
                final Dialog dialog = new Dialog(activity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_for_show_user_info);

                TextView textusername = (TextView) dialog.findViewById(R.id.dialog_username);
                TextView textusersurname = (TextView) dialog.findViewById(R.id.dialog_usersurname);
                TextView textuserphonenumber = (TextView) dialog.findViewById(R.id.dialog_phonenumber);
                TextView textusermail = (TextView) dialog.findViewById(R.id.dialog_usermail);
                try {
                    textusername.setText(json_data.getString("CarrierName"));
                    textusersurname.setText(json_data.getString("CarrierSurname"));
                    textuserphonenumber.setText(json_data.getString("CarrierPhoneNumber"));
                    textusermail.setText(json_data.getString("CarrierMail"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialog_cancelforuserinfo);
                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


                dialog.show();
            }
        });
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                        url,
                        new Response.Listener<String>() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "Response On Adapter: " + response);
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    JSONArray jsonDataset1 = jsonObject.getJSONArray("Location");
                                    JSONObject locationObj = jsonDataset1.getJSONObject(0);



                                    lat = locationObj.getString("Latitude");
                                    longi = locationObj.getString("Longitude");



                                    final Dialog dialog = new Dialog(activity);
                                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                    dialog.setContentView(R.layout.map_layout);
                                    dialog.setCanceledOnTouchOutside(true);
                                    dialog.show();
                                    GoogleMap googleMap;
                                    ImageView img = (ImageView) dialog.findViewById(R.id.imageView_close);
                                    img.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });


                                            MapView mMapView = (MapView) dialog.findViewById(R.id.mapView);
                                    MapsInitializer.initialize(activity);

                                    mMapView = (MapView) dialog.findViewById(R.id.mapView);
                                    mMapView.onCreate(dialog.onSaveInstanceState());



                                    mMapView.onResume();// needed to get the map to display immediately
                                    googleMap = mMapView.getMap();
                                    Log.d(TAG,"Latlong: "+lat + " - "+longi);
                                    LatLng myLaLn = new LatLng(Double.parseDouble(lat), Double.parseDouble(longi));

                                    CameraPosition camPos = new CameraPosition.Builder().target(myLaLn)
                                            .zoom(15)
                                            .bearing(45)
                                            .tilt(70)
                                            .build();

                                    CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
                                       googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat), Double.parseDouble(longi))
                                       )).setTitle("Product Location");
                                    googleMap.animateCamera(camUpd3);

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
                        Log.d(TAG, "Apikey On Adapter: " + ApiKey);


                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("ProductID", pID);
                        Log.d(TAG, "ProductID On Adapter: " + pID);
                        return params;
                    }

                };
                MySingleton.getmInstance(activity).addToRequestque(jsonObjRequest);


;
            }
        });



        return convertView;
    }
}
