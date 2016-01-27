package com.eightmins.eightminutes.login;

import android.R.string;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.eightmins.eightminutes.MainActivity;
import com.eightmins.eightminutes.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

public class LoginActivity extends AppCompatActivity {

  @Bind(R.id.username)
  EditText username;
  @Bind(R.id.password)
  EditText password;
  @Bind(R.id.scroll_view)
  ScrollView scrollView;

  @Bind(R.id.facebook_login)
  FloatingActionButton facebook;
  @Bind(R.id.twitter_login)
  FloatingActionButton twitter;
  @Bind(R.id.google_login)
  FloatingActionButton google;
  @Bind(R.id.sign_up)
  FloatingActionButton signUp;
  @Bind(R.id.expand)
  FloatingActionButton expand;
  @Bind(R.id.login)
  FloatingActionButton login;

  private ProgressDialog progress;

  private static final int RC_SIGN_IN = 9001;
  private GoogleSignInOptions googleSignInOptions;
  private GoogleApiClient googleApiClient;
  private boolean isFabOpen;
  private Animation fabOpen;
  private Animation fabClose;
  private Animation rotateForward;
  private Animation rotateBackward;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    ButterKnife.bind(this);

    googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestId()
        .requestProfile()
        .build();

    googleApiClient = new GoogleApiClient.Builder(this)
        .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
          @Override
          public void onConnectionFailed(ConnectionResult connectionResult) {

          }
        })
        .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
        .build();

    fabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
    fabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
    rotateForward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
    rotateBackward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_login, menu);
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

    if (requestCode == RC_SIGN_IN) {
      GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
      if (result.isSuccess()) {
        result.getSignInAccount().getDisplayName();
      } else {
        // Signed out, show unauthenticated UI.
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @OnEditorAction(R.id.password)
  boolean password(int actionId) {
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      login.performClick();
      return true;
    }
    return false;
  }

  @OnClick(R.id.login)
  public void login(View view) {
    String username = this.username.getText().toString().trim();
    String password = this.password.getText().toString().trim();

    if (username.isEmpty()) {
      new Builder(this).setTitle(R.string.error_title).setMessage(R.string.username_cannot_be_empty)
          .setPositiveButton(string.ok, null).create().show();
    } else if (password.isEmpty()) {
      new Builder(this).setTitle(R.string.error_title).setMessage(R.string.password_cannot_be_empty)
          .setPositiveButton(string.ok, null).create().show();
    } else {
      showProgressBar();
      ParseUser.logInInBackground(username, password, new LogInCallback() {
        @Override
        public void done(ParseUser user, ParseException exception) {
          hideProgressBar();
          if (exception == null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
          } else {
//            if (user.getBoolean("authenticated")) {

              new Builder(LoginActivity.this).setTitle(R.string.error_title).setMessage(exception.getMessage())
                  .setPositiveButton(string.ok, null).create().show();
//            } else {
//              Toast.makeText(LoginActivity.this, "Please wait for proper authentication!", Toast.LENGTH_SHORT).show();
//            }
          }
        }
      });
    }
  }

  private void hideProgressBar() {
    if ((progress != null) && progress.isShowing()) {
      progress.dismiss();
    }
  }

  private void showProgressBar() {
    progress = new ProgressDialog(this);
    progress.setMessage("Logging In...");
    progress.setIndeterminate(true);
    progress.setProgress(0);
    progress.show();
  }

  @OnClick(R.id.sign_up)
  public void signUp(View view) {
    startActivity(new Intent(this, SignUpActivity.class));
  }

  @OnClick(R.id.expand)
  public void onExpand(View view) {
    if (isFabOpen) {
      expand.startAnimation(rotateBackward);
      facebook.startAnimation(fabClose);
      twitter.startAnimation(fabClose);
      google.startAnimation(fabClose);
      signUp.startAnimation(fabClose);
    } else {
      expand.startAnimation(rotateForward);
      facebook.startAnimation(fabOpen);
      twitter.startAnimation(fabOpen);
      google.startAnimation(fabOpen);
      signUp.startAnimation(fabOpen);
    }

    isFabOpen = !isFabOpen;
    facebook.setClickable(isFabOpen);
    twitter.setClickable(isFabOpen);
    google.setClickable(isFabOpen);
    signUp.setClickable(isFabOpen);
  }

  @OnClick(R.id.facebook_login)
  public void onFacebookLogin(View view) {
    ParseFacebookUtils.logInWithReadPermissionsInBackground(this, null, new LogInCallback() {
      @Override
      public void done(ParseUser user, ParseException err) {
        if (user == null) {
          Toast.makeText(LoginActivity.this, "Uh oh. The user cancelled the Facebook login.", Toast.LENGTH_SHORT).show();
        } else if (user.isNew()) {
          Toast.makeText(LoginActivity.this, "User signed up and logged in through Facebook!", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(LoginActivity.this, "User logged in through Facebook!", Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  @OnClick(R.id.twitter_login)
  public void onTwitterLogin(View view) {
    ParseTwitterUtils.logIn(this, new LogInCallback() {
      @Override
      public void done(ParseUser user, ParseException err) {
        if (user == null) {
          Toast.makeText(LoginActivity.this, "Uh oh. The user cancelled the Twitter login.", Toast.LENGTH_SHORT).show();
        } else if (user.isNew()) {
          Toast.makeText(LoginActivity.this, "User signed up and logged in through Twitter!", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(LoginActivity.this, "User logged in through Twitter!", Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  @OnClick(R.id.google_login)
  public void onGoogleLogin(View view) {
    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }
}
