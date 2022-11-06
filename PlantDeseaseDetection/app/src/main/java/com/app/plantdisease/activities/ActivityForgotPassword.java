package com.app.plantdisease.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.app.plantdisease.R;
import com.app.plantdisease.config.AppConfig;
import com.app.plantdisease.models.Value;
import com.app.plantdisease.rests.ApiInterface;
import com.app.plantdisease.rests.RestAdapter;
import com.app.plantdisease.utils.Constant;
import com.app.plantdisease.utils.NetworkCheck;
import com.app.plantdisease.utils.Tools;

import id.solodroid.validationlibrary.Rule;
import id.solodroid.validationlibrary.Validator;
import id.solodroid.validationlibrary.annotation.Email;
import id.solodroid.validationlibrary.annotation.Required;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityForgotPassword extends AppCompatActivity implements Validator.ValidationListener {

    @Required(order = 1)
    @Email(order = 2, message = "Please Check and Enter a valid Email Address")
    EditText edtEmail;
    String strEmail;
    private Validator validator;
    Button btn_forgot;
    LinearLayout layout;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_user_forgot);

        if (AppConfig.ENABLE_RTL_MODE) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        edtEmail = findViewById(R.id.etUserName);
        btn_forgot = findViewById(R.id.btnForgot);
        layout = findViewById(R.id.view);

        btn_forgot.setOnClickListener(v -> validator.validateAsync());

        validator = new Validator(this);
        validator.setValidationListener(this);

    }

    public void showProgress(String title, String message) {
        progressDialog = new ProgressDialog(ActivityForgotPassword.this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void onValidationSucceeded() {
        strEmail = edtEmail.getText().toString();
        if (NetworkCheck.isNetworkAvailable(ActivityForgotPassword.this)) {
            showProgress(getString(R.string.title_please_wait), getString(R.string.forgot_verify_email));
            new Handler(Looper.getMainLooper()).postDelayed(() -> requestForgotAPI(strEmail), Constant.DELAY_TIME);
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        String message = failedRule.getFailureMessage();
        if (failedView instanceof EditText) {
            failedView.requestFocus();
            ((EditText) failedView).setError(message);
        } else {
            Toast.makeText(this, "Record Not Saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestForgotAPI(String email) {

        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<Value> call = apiInterface.checkEmail(email);

        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                final Value resp = response.body();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (resp != null) {
                        if (resp.value.equals("1")) {
                            progressDialog.dismiss();
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                showProgress(getString(R.string.title_please_wait), getString(R.string.forgot_send_email));
                            }, 100);
                            sendMail(resp.message);
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), getString(R.string.forgot_failed_message), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, Constant.DELAY_TIME);
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendMail(String password) {

        ApiInterface apiInterface = RestAdapter.phpMailerAPI();
        Call<Value> call = apiInterface.forgotPassword(strEmail, password);
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                final Value resp = response.body();
                progressDialog.dismiss();
                if (resp != null) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ActivityForgotPassword.this);
                    dialog.setTitle(R.string.dialog_success);
                    dialog.setMessage(R.string.forgot_success_message);
                    dialog.setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
                        Intent intent = new Intent(ActivityForgotPassword.this, ActivityUserLogin.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                    Log.d("SMTP", resp.value + " " + resp.message);
                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                t.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show();
                Log.d("SMTP", "Error" + t);
            }
        });
    }

//    @SuppressWarnings("deprecation")
//    private class MyTaskForgot extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            progressBar.setVisibility(View.VISIBLE);
//            layout.setVisibility(View.INVISIBLE);
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//            return NetworkCheck.getJSONString(params[0]);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            if (null == result || result.length() == 0) {
//                Toast.makeText(getApplicationContext(), getResources().getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show();
//
//            } else {
//
//                try {
//                    JSONObject mainJson = new JSONObject(result);
//                    JSONArray jsonArray = mainJson.getJSONArray(Constant.CATEGORY_ARRAY_NAME);
//                    JSONObject objJson = null;
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        objJson = jsonArray.getJSONObject(i);
//                        strMessage = objJson.getString(Constant.MSG);
//                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                new Handler().postDelayed(() -> {
//                    progressBar.setVisibility(View.GONE);
//                    setResult();
//                }, Constant.DELAY_PROGRESS_DIALOG);
//            }
//
//        }
//    }
//
//    public void setResult() {
//
//        if (Constant.GET_SUCCESS_MSG == 0) {
//            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//            dialog.setTitle(R.string.whops);
//            dialog.setMessage(R.string.forgot_failed_message);
//            dialog.setPositiveButton(R.string.dialog_ok, null);
//            dialog.setCancelable(false);
//            dialog.show();
//
//            layout.setVisibility(View.VISIBLE);
//            edtEmail.setText("");
//            edtEmail.requestFocus();
//
//        } else {
//
//            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
//            dialog.setTitle(R.string.dialog_success);
//            dialog.setMessage(R.string.forgot_success_message);
//            dialog.setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
//                Intent intent = new Intent(ActivityForgotPassword.this, ActivityUserLogin.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                finish();
//            });
//            dialog.setCancelable(false);
//            dialog.show();
//
//        }
//
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }
}


