package com.ecargo.ecargo_grid;

import java.net.Inet4Address;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUp_Fragment extends Fragment implements OnClickListener {
	private static View view;
	private static EditText Name,Surname, emailId, mobileNumber, location,
			password, confirmPassword, TcNo;
	private static TextView login;
	private static Button signUpButton;
	private static CheckBox terms_conditions,Sex_m,Sex_f,ClientType_Carr,ClientType_Cus;
    private String gender,cli_type,token;
    private static FragmentManager fragmentManager;

    private static final String TAG = "RegisterActivity";
    String url = "https://ecargo.info/Api/v1/register";


	public SignUp_Fragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.signup_layout, container, false);
		initViews();
		setListeners();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
        token = sharedPreferences.getString(getString(R.string.FCM_TOKEN),"");
		return view;
	}

	// Initialize all views
	private void initViews() {
		Name = (EditText) view.findViewById(R.id.Name);
		Surname = (EditText) view.findViewById(R.id.Surname);
		emailId = (EditText) view.findViewById(R.id.userEmailId);
		mobileNumber = (EditText) view.findViewById(R.id.mobileNumber);
		password = (EditText) view.findViewById(R.id.password);
		confirmPassword = (EditText) view.findViewById(R.id.confirmPassword);
		TcNo = (EditText) view.findViewById(R.id.TcNo);
		signUpButton = (Button) view.findViewById(R.id.signUpBtn);
		login = (TextView) view.findViewById(R.id.already_user);
		terms_conditions = (CheckBox) view.findViewById(R.id.terms_conditions);
        Sex_m = (CheckBox) view.findViewById(R.id.Male);
        Sex_f = (CheckBox) view.findViewById(R.id.Female);
        ClientType_Carr = (CheckBox) view.findViewById(R.id.Carrier);
        ClientType_Cus = (CheckBox) view.findViewById(R.id.Customer);

        Sex_f.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Sex_f.isChecked()) {
                        Sex_m.setChecked(false);
                        Sex_f.setChecked(true);

                        Sex_f.setEnabled(false);
                        Sex_m.setEnabled(true);

                        gender = "F";
                    }
                }
            }
        });
        Sex_m.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Sex_m.isChecked()) {
                        Sex_f.setChecked(false);
                        Sex_m.setChecked(true);
                        Sex_m.setEnabled(false);
                        Sex_f.setEnabled(true);

                        gender = "M";
                    }
                }
            }
        });
        ClientType_Carr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ClientType_Carr.isChecked()) {
                        ClientType_Cus.setChecked(false);
                        ClientType_Carr.setChecked(true);
                        ClientType_Carr.setEnabled(false);
                        ClientType_Cus.setEnabled(true);

                        cli_type = "Carrier";
                    }
                }
            }
        });
        ClientType_Cus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ClientType_Cus.isChecked()) {
                        ClientType_Carr.setChecked(false);
                        ClientType_Cus.setChecked(true);
                        ClientType_Cus.setEnabled(false);
                        ClientType_Carr.setEnabled(true);

                        cli_type = "Customer";
                    }
                }
            }
        });


		// Setting text selector over textviews
		XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
		try {
			ColorStateList csl = ColorStateList.createFromXml(getResources(),
					xrp);

			login.setTextColor(csl);
			terms_conditions.setTextColor(csl);
		} catch (Exception e) {
		}
	}

	// Set Listeners
	private void setListeners() {
		signUpButton.setOnClickListener(this);
		login.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.signUpBtn:

			// Call checkValidation method
			checkValidation();
			break;

		case R.id.already_user:

			// Replace login fragment
			new MainActivity().replaceLoginFragment();
			break;
		}

	}

	private void register(){

                StringRequest jsonObjRequest = new StringRequest(Request.Method.POST,
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d(TAG, "KEY: " + response);
                                try {
                                    JSONObject abc = new JSONObject(response);
                                    String mail = abc.getString("error");
                                    Log.d(TAG, "KEY: " + mail);
                                    if(mail.equals("false")){
                                        Toast.makeText(SignUp_Fragment.this.getContext(), abc.getString("message"), Toast.LENGTH_LONG).show();

                                        fragmentManager = getActivity().getSupportFragmentManager();
                                               fragmentManager.beginTransaction()
                                                .setCustomAnimations(R.anim.right_enter, R.anim.left_out)
                                                .replace(R.id.frameContainer, new Login_Fragment(),
                                                        Utils.Login_Fragment).commit();

                                    }else{
                                        Toast.makeText(SignUp_Fragment.this.getContext(), abc.getString("message"), Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                //        MyFunctions.toastShort(LoginActivity.this, response);
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("volley", "Error: " + error.getMessage());
                        error.printStackTrace();
                        // MyFunctions.croutonAlert(LoginActivity.this,
                        //       MyFunctions.parseVolleyError(error));
                        //loading.setVisibility(View.GONE);
                    }
                })
                {

                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=UTF-8";
                    }

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("UserName",Name.getText().toString());
                        params.put("UserSurname",Surname.getText().toString().trim());
                        params.put("UserMail", emailId.getText().toString().trim());
                        params.put("UserPassword", password.getText().toString().trim());
                        params.put("UserTcNo", TcNo.getText().toString().trim());
                        params.put("UserSex", gender);
                        params.put("UserType", cli_type);
                        params.put("UserPhoneNumber",mobileNumber.getText().toString());
                        params.put("FcmToken",token);
                        Log.d("TAG","Tokendeneme"+token);
                        return params;
                    }

                };
                MySingleton.getmInstance(SignUp_Fragment.this.getContext()).addToRequestque(jsonObjRequest);
            }




    public void onSignupFailed() {
        Toast.makeText(this.getContext(), "Login failed", Toast.LENGTH_LONG).show();
        signUpButton.setEnabled(true);
    }

	// Check Validation Method
	private void checkValidation() {

		// Get all edittext texts
		String getFullName = Name.getText().toString();
		String getEmailId = emailId.getText().toString();
		String getMobileNumber = mobileNumber.getText().toString();
		String getPassword = password.getText().toString();
		String getConfirmPassword = confirmPassword.getText().toString();
        String getTcNo = TcNo.getText().toString();

		// Pattern match for email id
		Pattern p = Pattern.compile(Utils.regEx);
		Matcher m = p.matcher(getEmailId);

		// Check if all strings are null or not
		if (getFullName.equals("") || getFullName.length() == 0
				|| getEmailId.equals("") || getEmailId.length() == 0
				|| getMobileNumber.equals("") || getMobileNumber.length() == 0
                || getTcNo.equals("") || getTcNo.length() == 0
				|| getPassword.equals("") || getPassword.length() == 0
				|| getConfirmPassword.equals("")
				|| getConfirmPassword.length() == 0)

			new CustomToast().Show_Toast(getActivity(), view,
					"All fields are required.");

		// Check if email id valid or not
		else if (!m.find())
			new CustomToast().Show_Toast(getActivity(), view,
					"Your Email Id is Invalid.");

		// Check if both password should be equal
		else if (!getConfirmPassword.equals(getPassword))
			new CustomToast().Show_Toast(getActivity(), view,
					"Both password doesn't match.");

		// Make sure user should check Terms and Conditions checkbox
		else if (!terms_conditions.isChecked())
			new CustomToast().Show_Toast(getActivity(), view,
					"Please select Terms and Conditions.");

		// Else do signup or do your stuff
		else
            register();

	}
}
