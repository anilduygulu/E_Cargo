package com.ecargo.ecargo_grid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierNotificationsActivity;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierProductActivites;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierRouteActivity;
import com.ecargo.ecargo_grid.CustomerActivites.CustomerNotificationActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kaan on 4/26/2017.
 */

public class AdapterForCustomerNotifications  extends BaseAdapter implements ListAdapter {
    public final Activity activity;
    public final JSONArray jsonArray;
    public final String apikey;
    public final JSONObject jsonObject;
    final String TAG = "Notfy Adapter:";
    String url = "https://ecargo.info/Api/v1/notifications/DeleteNotification";
    String url2 = "https://ecargo.info/Api/v1/services/createRelation";
    static String notfyid;
    static String routeId,productId;


    public AdapterForCustomerNotifications(Activity activity, JSONArray jsonArray,String ApiKey,JSONObject jsonObject) {
        assert activity != null;
        assert jsonArray != null;
        assert ApiKey != null;
        assert jsonObject != null;
        this.jsonObject = jsonObject;
        this.jsonArray = jsonArray;
        this.activity = activity;
        this.apikey = ApiKey;
        Log.d(TAG, "notifyid: " + jsonArray);
    }


    @Override
    public int getCount() {
        if (null == jsonArray)
            return 0;
        else
            return jsonArray.length();
    }

    @Override
    public JSONObject getItem(int position) {
        if (null == jsonArray) return null;
        else
            return jsonArray.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        JSONObject jsonObject = getItem(position);

        return jsonObject.optLong("id");
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = activity.getLayoutInflater().inflate(R.layout.rowforcustomernotifications, null);


        TextView custome_user_name = (TextView) convertView.findViewById(R.id.carrier_user_name1);
        TextView urun_name = (TextView) convertView.findViewById(R.id.urun_name1);
        TextView check_icon = (TextView) convertView.findViewById(R.id.check_icon1);
        TextView cancel_icon = (TextView) convertView.findViewById(R.id.cancel_icon1);

        final JSONObject json_data = getItem(position);
        if (null != json_data) {
            String customer_user_name1 = null;
            String urun_name1 = null;

            try {
                AdapterForCarrierNotifications.notfyid = json_data.getString("NotificationID");
                AdapterForCarrierNotifications.routeId = json_data.getString("RouteID");
                AdapterForCarrierNotifications.productId = json_data.getString("ProductID");
                customer_user_name1 = json_data.getString("UserName");
                urun_name1 = json_data.getString("ProductName");

            } catch (JSONException e) {
                e.printStackTrace();
            }
            custome_user_name.setText(customer_user_name1);
            urun_name.setText(urun_name1);

        }

        custome_user_name.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                final Dialog dialog = new Dialog(activity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_for_show_user_info);

                TextView textusername = (TextView) dialog.findViewById(R.id.dialog_username);
                TextView textusersurname = (TextView) dialog.findViewById(R.id.dialog_usersurname);
                TextView textuserphonenumber = (TextView) dialog.findViewById(R.id.dialog_phonenumber);
                TextView textusermail = (TextView) dialog.findViewById(R.id.dialog_usermail);
                try {
                    textusername.setText(json_data.getString("UserName"));
                    textusersurname.setText(json_data.getString("UserSurname"));
                    textuserphonenumber.setText(json_data.getString("UserPhoneNumber"));
                    textusermail.setText(json_data.getString("UserMail"));
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

        urun_name.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                final Dialog dialog = new Dialog(activity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.dialog_for_show_product_detail);

                TextView textproductname = (TextView) dialog.findViewById(R.id.dialog_productname);
                TextView textproductdetail = (TextView) dialog.findViewById(R.id.dialog_productdetail);

                try {
                    textproductname.setText(json_data.getString("ProductName"));
                    textproductdetail.setText(json_data.getString("ProductDetails"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Button dialogButtonCancel1 = (Button) dialog.findViewById(R.id.dialog_cancelonproductdetail);
                dialogButtonCancel1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


                dialog.show();

            }
        });
        cancel_icon.setOnClickListener(new View.OnClickListener() {



            public void onClick(View v) {
                Log.d(TAG, "notifyid: " + AdapterForCarrierNotifications.notfyid);

                StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                        url,
                        new Response.Listener<String>() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "Response: " + response.toString());
                                try {
                                    JSONObject sonuc = new JSONObject(response.toString());
                                    if(sonuc.getString("error").equals("false")) {
                                        jsonArray.remove(position);
                                        CustomerNotificationActivity.adapterForCustomerNotifications.notifyDataSetChanged();
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
                        params.put("authorization", apikey);

                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        try {
                            params.put("NotificationID", json_data.getString("NotificationID"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return params;
                    }

                };
                MySingleton.getmInstance(activity.getApplicationContext()).addToRequestque(jsonObjRequest);
            }
        });
        check_icon.setOnClickListener(new View.OnClickListener() {



            public void onClick(View v) {
                Log.d(TAG, "notifyid: " + AdapterForCarrierNotifications.notfyid);

                StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                        url2,
                        new Response.Listener<String>() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "Response: " + response.toString());
                                try {
                                    JSONObject sonuc = new JSONObject(response.toString());
                                    if(sonuc.getString("error").equals("false")) {
                                        jsonArray.remove(position);
                                        CustomerNotificationActivity.adapterForCustomerNotifications.notifyDataSetChanged();
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
                        params.put("authorization", apikey);

                        return params;
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        try {
                            params.put("RouteID", json_data.getString("RouteID"));
                            params.put("ProductID", json_data.getString("ProductID"));
                            params.put("SenderID", json_data.getString("CarrierSenderID"));
                            params.put("UserType", "Carrier");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        return params;
                    }

                };
                MySingleton.getmInstance(activity.getApplicationContext()).addToRequestque(jsonObjRequest);
            }
        });

        return convertView;
    }
}