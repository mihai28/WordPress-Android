
package org.wordpress.android.ui.accounts;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wordpress.rest.RestRequest;

import org.json.JSONException;
import org.json.JSONObject;

import org.wordpress.android.Constants;
import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.util.AlertUtil;

public class NewUserPageFragment extends NewAccountAbstractPageFragment {

    private EditText emailTextField;
    private EditText passwordTextField;
    private EditText usernameTextField;

    public NewUserPageFragment() {
    }
    
    private boolean checkUserData() {
        // try to create the user
        final String email = emailTextField.getText().toString().trim();
        final String password = passwordTextField.getText().toString().trim();
        final String username = usernameTextField.getText().toString().trim();
        
        if (email.equals("") || password.equals("")) {
            AlertUtil.showAlert(NewUserPageFragment.this.getActivity(), R.string.required_fields,
                    R.string.username_password_required);
            return false;
        }

        if (password.length() < 4) {
            AlertUtil.showAlert(NewUserPageFragment.this
                    .getActivity(), R.string.invalid_password_title,
                    R.string.invalid_password_message);
            return false;
        }
        
        if (username.length() > 60) {
            AlertUtil.showAlert(NewUserPageFragment.this
                    .getActivity(), R.string.invalid_username_title,
                    R.string.invalid_username_length);
            return false;
        }

        final String emailRegEx = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
        final Pattern emailRegExPattern = Pattern.compile(emailRegEx,
                Pattern.DOTALL);
        Matcher matcher = emailRegExPattern.matcher(email);
        if (!matcher.find() || email.length() > 100) {
            AlertUtil.showAlert(NewUserPageFragment.this
                    .getActivity(),
                    R.string.invalid_email_title,
                    R.string.invalid_email_message);
            return false;
        }

        return true;
    }

    View.OnClickListener signupClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        
            //TODO: The following lines ensure that no .com account are available in the app - change this!!!!
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(getActivity()).edit();
                editor.remove(WordPress.WPCOM_USERNAME_PREFERENCE);
                editor.remove(WordPress.WPCOM_PASSWORD_PREFERENCE);
                editor.remove(WordPress.ACCESS_TOKEN_PREFERENCE);
                editor.commit();
                WordPress.wpDB.deactivateAccounts();
                WordPress.wpDB.updateLastBlogId(-1);
                WordPress.currentBlog = null;
                WordPress.restClient.clearAccessToken();
            
            //reset the data
            NewAccountActivity act = (NewAccountActivity)getActivity();
            act.validatedEmail = null;
            act.validatedPassword = null;
            act.validatedUsername = null;
            
            if (mSystemService.getActiveNetworkInfo() == null) {
                AlertUtil.showAlert(getActivity(), R.string.no_network_title, R.string.no_network_message);
                return;
            }
            
            // try to create the user
            final String email = emailTextField.getText().toString().trim();
            final String password = passwordTextField.getText().toString().trim();
            final String username = usernameTextField.getText().toString().trim();

            if (false == checkUserData())
                return;

            pd = ProgressDialog.show(NewUserPageFragment.this
                    .getActivity(),
                    getString(R.string.account_setup),
                    getString(R.string.validating_user_data), true, false);

            String path = "users/new";
            Map<String, String> params = new HashMap<String, String>();
            params.put("username", username);
            params.put("password", password);
            params.put("email", email);
            params.put("validate", "1");
            params.put("client_id", WordPress.config.getProperty(WordPress.APP_ID_PROPERTY));
            params.put("client_secret", WordPress.config.getProperty(WordPress.APP_SECRET_PROPERTY));
            
            restClient.post(path, params, null,
                    new RestRequest.Listener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (pd != null)
                                pd.dismiss();
                            Log.d("1. New User PAGE", String.format("OK %s", response.toString()));
                            try {
                                if(response.getBoolean("success")) {
                                    NewAccountActivity act = (NewAccountActivity)getActivity();
                                    act.validatedEmail = email;
                                    act.validatedPassword = password;
                                    act.validatedUsername = username;
                                    act.showNextItem();
                                } else {
                                    showError(getString(R.string.error_generic));
                                }
                            } catch (JSONException e) {
                                showError(getString(R.string.error_generic));
                            }
                        }
                    },
                    new ErrorListener()
                    );
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.new_account_user_fragment_screen, container, false);

        // Set the title view to show the page number.
        final TextView steps = (TextView) rootView.findViewById(R.id.text1);
        steps.setText(getString(R.string.title_template_step, mPageNumber + 1));
        
        rootView.findViewById(R.id.l_agree_terms_of_service).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(Constants.URL_TOS);
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                    }
                }
                );
        
        final Button signupButton = (Button) rootView.findViewById(R.id.signup_button);
        signupButton.setOnClickListener(signupClickListener);

        emailTextField = (EditText) rootView.findViewById(R.id.email_address);
        passwordTextField = (EditText) rootView.findViewById(R.id.password);
        usernameTextField = (EditText) rootView.findViewById(R.id.username);

        return rootView;
    }
}