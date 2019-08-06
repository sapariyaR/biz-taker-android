package biz.biztaker.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import biz.biztaker.R;
import biz.biztaker.commonClasses.BizTakerApp;
import biz.biztaker.commonClasses.BizTakerWebServiceCallBack;
import biz.biztaker.entity.Person;
import biz.biztaker.services.LoginService;

/**
 * Created by Anand Jakhaniya on 11-02-2018.
 * @author Anand Jakhaniya
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int RC_GOOGLE_SIGN_IN = 2;
    private final String TAG = this.getClass().getSimpleName();

    private Boolean isNewRegistration = false;

    // UI references.
    private AutoCompleteTextView mEmailView, mUserNameView;
    private TextView mHeaderLable;
    private EditText mPasswordView;
    private Button mRegistrationButton, mSignInButton;
    private SignInButton gSignInButton;
    private View mProgressView;
    private View mLoginFormView, mUserNameInputLayout;
    private Context context;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        // Set up the login form.
        mEmailView = findViewById(R.id.email);
        populateAutoComplete();

        mHeaderLable = findViewById(R.id.header_label);
        mPasswordView = findViewById(R.id.password);
        mUserNameView = findViewById(R.id.user_name);
        mRegistrationButton = findViewById(R.id.register_button);
        mSignInButton = findViewById(R.id.sign_in_button);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mUserNameInputLayout = findViewById(R.id.user_name_inputLayout);
        gSignInButton = findViewById(R.id.googlesign_in_button);

        initGoogleSignIn();

        setOnClickEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BizTakerApp.getInstance().currentActivity = this;
    }

    private void initGoogleSignIn() {

        // Configure sign-in to request the person's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setOnClickEvent() {

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.sign_in_button || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mRegistrationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mRegistrationButton.getText().equals(context.getResources().getString(R.string.btn_register))) {
                    mUserNameInputLayout.setVisibility(View.VISIBLE);
                    mHeaderLable.setText(context.getResources().getString(R.string.new_registration));
                    mUserNameView.requestFocus();
                    mSignInButton.setVisibility(View.GONE);
                    mRegistrationButton.setText(context.getResources().getString(R.string.btn_register));
                    isNewRegistration = true;
                    mEmailView.setText("");
                    mPasswordView.setText("");
                } else {
                    attemptLogin();
                }
            }
        });

        gSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptGoogleSignIn();
            }
        });
    }


    private void attemptGoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    }).show();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (isNewRegistration){
            mUserNameInputLayout.setVisibility(View.GONE);
            mHeaderLable.setText(context.getResources().getString(R.string.login));
            mSignInButton.setVisibility(View.VISIBLE);
            mRegistrationButton.setText(context.getResources().getString(R.string.action_register));
            isNewRegistration = false;
            mUserNameView.setText("");
            mPasswordView.setText("");
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null){
            mEmailView.setText(data.getStringExtra("email"));
        }else if (requestCode == RC_GOOGLE_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Log.w(TAG, "signInResult:***************************************");
            Log.w(TAG, "signInResult:email =" + account.getEmail());
            Log.w(TAG, "signInResult:name =" + account.getDisplayName());
            Log.w(TAG, "signInResult:family name =" + account.getFamilyName());
            Log.w(TAG, "signInResult:photo url=" + account.getPhotoUrl());
            Log.w(TAG, "signInResult:id token=" + account.getIdToken());
            Log.w(TAG, "signInResult:id=" + account.getId());
            Log.w(TAG, "signInResult:***************************************");

            //make web call




            // Signed in successfully, show authenticated UI.
//            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            e.printStackTrace();
//            updateUI(null);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String userName = mUserNameView.getText().toString().trim();
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (isNewRegistration){
            if (TextUtils.isEmpty(userName)) {
                mUserNameView.setError(getString(R.string.error_field_required));
                focusView = mUserNameView;
                cancel = true;
            }
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } /*else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the person login attempt.

            if (!isNewRegistration) {
                userLoginTask(email, password);
            } else {
                Person person = new Person();
//                person.name = userName;
//                person.email = email;
//                person.password = password;
                UserRegistrationTask mRegTask = new UserRegistrationTask(person);
                mRegTask.execute((Void) null);
            }
        }
    }

    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        return password != null && pattern.matcher(password).matches();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device person's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the person hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }

    public void userLoginTask(final String mEmail, String mPassword){
        LoginService userService = new LoginService();

        try {
            userService.getPersonFromServer(mEmail, mPassword, new BizTakerWebServiceCallBack() {
                @Override
                public void onResponse(Object object) {
                    showProgress(true);
                    Person person = (Person) object;
                    Snackbar.make(mEmailView.getRootView(), "Login successfully..", Snackbar.LENGTH_LONG).show();
                    Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                    intent.putExtra("person", person);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onErrorResponse(Exception error) {
                    showProgress(false);
                    Toast toast = Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.NO_GRAVITY,0,0);
                    toast.show();
                }
            });
        } catch(Exception ex){
            ex.printStackTrace();
            Toast toast = Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.NO_GRAVITY,0,0);
            toast.show();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the person.
     */
    @SuppressLint("StaticFieldLeak")
    public class UserRegistrationTask extends AsyncTask<Void, Void, Boolean> {

        private final Person person;

        private String errorMessage;

        UserRegistrationTask(Person person) {
            this.person = person;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                //UserService userService = new UserService();
                //userService.addUser(person);
                return true;
            } catch (Exception e) {
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            showProgress(false);

            if (success) {
                mUserNameInputLayout.setVisibility(View.GONE);
                mHeaderLable.setText(context.getResources().getString(R.string.login));
                mSignInButton.setVisibility(View.VISIBLE);
                mRegistrationButton.setText(context.getResources().getString(R.string.action_register));
                isNewRegistration = false;
                mEmailView.setText(person.email);
                mUserNameView.setText("");
                mPasswordView.setText("");
            } else {
                Snackbar.make(mEmailView.getRootView(),errorMessage, Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }
}

