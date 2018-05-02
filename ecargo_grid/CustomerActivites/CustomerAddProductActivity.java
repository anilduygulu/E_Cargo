package com.ecargo.ecargo_grid.CustomerActivites;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.ecargo.ecargo_grid.CarrierActivites.CarrierCarsActivity;
import com.ecargo.ecargo_grid.CarrierActivites.CarrierRouteActivity;
import com.ecargo.ecargo_grid.CustomToast;
import com.ecargo.ecargo_grid.CustomerActivity;
import com.ecargo.ecargo_grid.JSONAdapterForListCar;
import com.ecargo.ecargo_grid.JSONAdapterForListProduct;
import com.ecargo.ecargo_grid.MySingleton;
import com.ecargo.ecargo_grid.R;
import com.ecargo.ecargo_grid.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerAddProductActivity extends AppCompatActivity {
    JSONObject jsonObj;
    String ApiKey,userMail;
    final String TAG = "Customer Route Activity";
    static String customerId;
    String url = "https://ecargo.info/Api/v1/products/ListProducts";
    String url2 = "https://ecargo.info/Api/v1/products/addNewProduct";
    Button addProduct;
    public static EditText input_detail,input_name;
    Spinner input_from,input_to;
    private static Animation shakeAnimation;
    JSONObject resp ;
    public static JSONAdapterForListProduct jsonAdapterForListProduct;
    private static LinearLayout addproductLayout;
    static JSONObject responseObj;
    public static JSONArray productArray;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_add_product);
        addProduct = (Button) findViewById(R.id.add_productsButton);
        input_detail = (EditText) findViewById(R.id.input_details);
        input_name = (EditText) findViewById(R.id.product_name);
        input_from = (Spinner) findViewById(R.id.input_fromCity);
        input_to = (Spinner) findViewById(R.id.input_toCity);
        addproductLayout = (LinearLayout) findViewById(R.id.add_product_layout);
        listView = (ListView) findViewById(R.id.products_listView);
        shakeAnimation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.shake);
        try {
            jsonObj = new JSONObject(getIntent().getExtras().getString("jsonObj"));
            Log.d(TAG,"JsonObj"+jsonObj);
            ApiKey = jsonObj.getString("apiKey");
            userMail = jsonObj.getString("UserMail");
            customerId = jsonObj.getString("CustomerID");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            responseObj = new JSONObject(response);
                            Log.d(TAG, "responseObj: " + responseObj);
                            String pArray = responseObj.getString("products");
                            productArray = new JSONArray(pArray);
                            jsonAdapterForListProduct = new JSONAdapterForListProduct(CustomerAddProductActivity.this,productArray,ApiKey);//jArray is your json array

                            //Set the above adapter as the adapter of choice for our list
                            listView.setAdapter(jsonAdapterForListProduct);
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
                params.put("CustomerID",customerId);

                return params;
            }

        };
        MySingleton.getmInstance(CustomerAddProductActivity.this).addToRequestque(jsonObjRequest);



        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag = checkValidation();
                Log.d(TAG,"flag"+flag);
                if(flag==1) {
                    StringRequest jsonObjRequest1 = new StringRequest(Request.Method.POST,
                            url2,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    JSONObject abc = null;
                                    try {
                                        abc = new JSONObject(response);

                                        String error1 = abc.getString("error");
                                        if (error1.equals("true")) {


                                            Toast.makeText(CustomerAddProductActivity.this, abc.getString("message"),
                                                    Toast.LENGTH_LONG).show();

                                        } else if (error1.equals("false")) {
                                            String ProductID = abc.getString("ProductID");
                                            String ProductName = input_name.getText().toString();
                                            String ProductDetail = input_detail.getText().toString();
                                            String ProductTo =input_to.getSelectedItem().toString();
                                            String ProductFrom = input_from.getSelectedItem().toString();

                                            JSONObject newRoute = new JSONObject();
                                            newRoute.put("ProductID",ProductID);
                                            newRoute.put("ProductName",ProductName);
                                            newRoute.put("ProductDetail",ProductDetail);
                                            newRoute.put("ProductTo",ProductTo);
                                            newRoute.put("ProductFrom",ProductFrom);
                                            Log.d(TAG,"New Car: "+newRoute);

                                            productArray.put(newRoute);
                                            Log.d(TAG,"Product Array: "+productArray);
                                            jsonAdapterForListProduct.notifyDataSetChanged();
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
                            Log.d(TAG, "APIKEY :)" + ApiKey);

                            return params;
                        }

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("CustomerID", customerId);
                            params.put("ProductName", input_name.getText().toString());
                            params.put("ProductFrom", input_from.getSelectedItem().toString());
                            params.put("ProductTo", input_to.getSelectedItem().toString());
                            params.put("ProductDetails", input_detail.getText().toString());
                            Log.d(TAG, "PARAMETERS" + customerId + input_name.getText().toString() + input_from.getSelectedItem().toString() + input_to.getSelectedItem().toString() + input_detail.getText().toString());

                            return params;
                        }

                    };
                    MySingleton.getmInstance(getApplicationContext()).addToRequestque(jsonObjRequest1);
                }

            }
        });

    }
    private int checkValidation() {
        // Get email id and password
        String input_name1 = input_name.getText().toString();
        String input_detail1 = input_detail.getText().toString();

        // Check patter for email id
        Pattern p = Pattern.compile(Utils.regEx);


        // Check for both field is empty or not
        Log.d(TAG,"asd"+input_name1);
        Log.d(TAG,"asd"+input_detail1);
        if (input_name1.equals("") || input_detail1.equals(""))
        {
            addproductLayout.startAnimation(shakeAnimation);
            Toast.makeText(getApplicationContext(),"Enter all credentials.",Toast.LENGTH_LONG).show();
            return 0;

        }

        else
            return 1;

    }
}
