package com.example.appsenasoft2020.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.example.appsenasoft2020.MainActivity
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper


import com.example.appsenasoft2020.R
import com.example.appsenasoft2020.StepRegisterActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        val username:TextInputEditText = findViewById(R.id.CorreoP)
        val password:TextInputEditText = findViewById(R.id.clave)
        val login = findViewById<Button>(R.id.idBotonsegunda)
     //  val loading = findViewById<ProgressBar>(R.id.loading)
        val register = findViewById<TextView>(R.id.btnRegistro)
        val loginHuawei = findViewById<ImageView>(R.id.buttonHuawei)

        val authParams =
            HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode()
                .createParams()

        val service = HuaweiIdAuthManager.getService(this@LoginActivity, authParams)

        loginHuawei.setOnClickListener{
            startActivityForResult(service.signInIntent, 8888)
        }


        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            //loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.getText().toString() ,
                password.getText().toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.getText().toString(),
                    password.getText().toString()
                )
            }


            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.toString(),
                            password.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                auth = Firebase.auth
                var correo=username.text.toString()
                var pass = password.text.toString()
                try {
                    auth.signInWithEmailAndPassword(correo, pass)
                            .addOnCompleteListener(this@LoginActivity) { task ->
                                if (task.isSuccessful) {
                                    // Sign in success, update UI with the signed-in user's information
                                    val user = Firebase.auth.currentUser
                                    Toast.makeText(this@LoginActivity,"Se inicio con exito",Toast.LENGTH_SHORT).show()
                                     var intent:Intent = Intent(this@LoginActivity,MainActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(
                                            baseContext, "Error al iniciar verifique sus datos",
                                            Toast.LENGTH_SHORT
                                    ).show()

                                    // ...
                                }

                                // ...
                            }
                }catch (e: Exception){

                }
            }



            register.setOnClickListener{
              var intent:Intent = Intent(this@LoginActivity, StepRegisterActivity::class.java)
                startActivity(intent)
            }


        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,  data: Intent?) {
// Process the authorization result and obtain the authorization code from AuthHuaweiId.
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 8888) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                // The sign-in is successful, and the user's HUAWEI ID information and authorization code are obtained.
                val huaweiAccount = authHuaweiIdTask.result
                Log.i("Huawei", "Inicio huawei:" + huaweiAccount.authorizationCode)
            } else {
                // The sign-in failed.
                Log.e("Huawei", "No inicio Huawei : " + (authHuaweiIdTask.exception as ApiException).statusCode)
            }
        }
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}