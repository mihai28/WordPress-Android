
package org.wordpress.android.ui.accounts;

import java.util.HashMap;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wordpress.rest.RestRequest;

import org.json.JSONException;
import org.json.JSONObject;

import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.WordPressDB;
import org.wordpress.android.util.AlertUtil;

public class NewAccountReviewPageFragment extends NewAccountAbstractPageFragment {

    private TextView email;
    private TextView username;
    private TextView siteTitle;
    private TextView siteAddress;
    private TextView siteLanguage;
    
    
    public NewAccountReviewPageFragment() {

    }
    
    View.OnClickListener nextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mSystemService.getActiveNetworkInfo() == null) {
                AlertUtil.showAlert(getActivity(), R.string.no_network_title, R.string.no_network_message);
                return;
            }

            pd = ProgressDialog.show(NewAccountReviewPageFragment.this
                    .getActivity(),
                    getString(R.string.account_setup),
                    getString(R.string.signing_up), true, false);
            
            validateUser();
        }
    };
    
    private class ResponseHandler implements RestRequest.Listener {
        
        public static final int VALIDATE_USER = 1;
        public static final int VALIDATE_SITE = 2;
        public static final int CREATE_USER = 3;
        public static final int AUTHENTICATE_USER = 4;
        public static final int CREATE_SITE = 5;
        
        private int currentStep = VALIDATE_USER;
        
        public ResponseHandler(int step) {
            super();
            this.currentStep = step;
        }

        @Override
        public void onResponse(JSONObject response) {
            Log.d("REST Response", String.format("Create Account step %d", currentStep));
            Log.d("REST Response", String.format("OK %s", response.toString()));
            try {
                if( currentStep == AUTHENTICATE_USER) {
                    createTheBlog();
                } else {
                    if(response.getBoolean("success")) {
                        switch (currentStep) { //Fire the next action
                            case VALIDATE_USER:
                                validateSite();
                                break;
                            case VALIDATE_SITE:
                                createUser();
                                break;
                            case CREATE_USER:
                                authenticaUser();
                                break;
                            case CREATE_SITE:
                                finishThisStuff();
                                break;      
                            default:
                                break;
                        }
                    } else {
                        showError(getString(R.string.error_generic));
                    }
                }
            } catch (JSONException e) {
                showError(getString(R.string.error_generic));
            }
        }
    }
    
    private void validateUser(){
        NewAccountActivity act = (NewAccountActivity)getActivity();
        String path = "users/new";
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", act.validatedUsername);
        params.put("password", act.validatedPassword);
        params.put("email", act.validatedEmail);
        params.put("validate", "1");
        params.put("client_id", WordPress.config.getProperty(WordPress.APP_ID_PROPERTY));
        params.put("client_secret", WordPress.config.getProperty(WordPress.APP_SECRET_PROPERTY));
        restClient.post(path, params, null, new ResponseHandler(ResponseHandler.VALIDATE_USER), new ErrorListener());
    }
    
    private void validateSite(){
        final NewAccountActivity act = (NewAccountActivity)getActivity();
        String path = "sites/new";
        Map<String, String> params = new HashMap<String, String>();
        params.put("blog_name", act.validatedBlogURL);
        params.put("blog_title", act.validatedBlogTitle);
        params.put("lang_id", act.validatedLanguageID);
        params.put("public", act.validatedPrivacyOption);
        params.put("validate", "1");
        params.put("client_id", WordPress.config.getProperty(WordPress.APP_ID_PROPERTY));
        params.put("client_secret", WordPress.config.getProperty(WordPress.APP_SECRET_PROPERTY));
        restClient.post(path, params, null, new ResponseHandler(ResponseHandler.VALIDATE_SITE), new ErrorListener());
    }
    
    private void createUser() {
        NewAccountActivity act = (NewAccountActivity)getActivity();
        String path = "users/new";
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", act.validatedUsername);
        params.put("password", act.validatedPassword);
        params.put("email", act.validatedEmail);
        params.put("validate", "0");
        params.put("client_id", WordPress.config.getProperty(WordPress.APP_ID_PROPERTY));
        params.put("client_secret", WordPress.config.getProperty(WordPress.APP_SECRET_PROPERTY));
        restClient.post(path, params, null, new ResponseHandler(ResponseHandler.CREATE_USER), new ErrorListener());
    }

    private void authenticaUser() {
        NewAccountActivity act = (NewAccountActivity)getActivity();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(act);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(WordPress.WPCOM_USERNAME_PREFERENCE, act.validatedUsername);
        editor.putString(WordPress.WPCOM_PASSWORD_PREFERENCE, WordPressDB.encryptPassword(act.validatedPassword));
        editor.commit();
        // fire off a request to get an access token
        WordPress.restClient.get("me", new ResponseHandler(ResponseHandler.AUTHENTICATE_USER), new ErrorListener());
    }
    
    private void createTheBlog(){
        final NewAccountActivity act = (NewAccountActivity)getActivity();
        String path = "sites/new";
        Map<String, String> params = new HashMap<String, String>();
        params.put("blog_name", act.validatedBlogURL);
        params.put("blog_title", act.validatedBlogTitle);
        params.put("lang_id", act.validatedLanguageID);
        params.put("public", act.validatedPrivacyOption);
        params.put("validate", "false");
        params.put("client_id", WordPress.config.getProperty(WordPress.APP_ID_PROPERTY));
        params.put("client_secret", WordPress.config.getProperty(WordPress.APP_SECRET_PROPERTY));
        WordPress.restClient.post(path, params, null, new ResponseHandler(ResponseHandler.CREATE_SITE), new ErrorListener());
    }
    
    private void finishThisStuff(){
        if (pd != null)
            pd.dismiss();
        final NewAccountActivity act = (NewAccountActivity)getActivity();
        Bundle bundle = new Bundle();
        bundle.putString("username", act.validatedUsername);
        Intent mIntent = new Intent();
        mIntent.putExtras(bundle);
        act.setResult(NewAccountActivity.RESULT_OK, mIntent);
        act.finish();
    }
    
    public void updateUI(){
        NewAccountActivity act = (NewAccountActivity)getActivity();
        email.setText(act.validatedEmail);
        username.setText(act.validatedUsername);
        siteTitle.setText(act.validatedBlogTitle);
        siteAddress.setText("http://"+act.validatedBlogURL+".wordpress.com");
        siteLanguage.setText(act.validatedLanguageID);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.new_account_review_fragment_screen, container, false);

        // Set the title view to show the page number.
        final TextView steps = (TextView) rootView.findViewById(R.id.text1);
        steps.setText(getString(R.string.title_template_step, mPageNumber + 1));
        
        ((TextView)rootView.findViewById(R.id.review_account_label)).setText(getString(R.string.review_your_information));

        email = (TextView) rootView.findViewById(R.id.email_text);
        username = (TextView) rootView.findViewById(R.id.username_text);
        siteTitle = (TextView) rootView.findViewById(R.id.title_text);
        siteAddress = (TextView) rootView.findViewById(R.id.address_text);
        siteLanguage = (TextView) rootView.findViewById(R.id.language_text);
        
        final Button nextButton = (Button) rootView.findViewById(R.id.next_button);
        nextButton.setOnClickListener(nextClickListener);
        
        final Button prevButton = (Button) rootView.findViewById(R.id.prev_button);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewAccountActivity act = (NewAccountActivity)getActivity();
                act.showPrevItem();
            }
        });

        return rootView;
    }
}