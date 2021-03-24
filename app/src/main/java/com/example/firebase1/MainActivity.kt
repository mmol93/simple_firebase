package com.example.firebase1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.firebase1.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {
    private lateinit var binder : ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth : FirebaseAuth
    private val loginSuccessCode = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        binder = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // firebase storge 변수 설정
        val storage = Firebase.storage

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        // 구글 로그인 버튼 클릭 시
        binder.googleSign.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // id, 비밀번호 입력후 회원가입 버튼 클릭 시
        binder.buttonMakeAccount.setOnClickListener {
            val id = binder.editId.text.toString()
            val password = binder.editPs.text.toString()

            // 아이디 or 비밀번호를 입력하지 않았을 시
            if (id == "" || password == ""){
                val toast = Toast.makeText(this, "id 또는 비밀번호를 입력해주세요", Toast.LENGTH_LONG)
                toast.show()
            }else{
                // 입력한 해당 아이디로 회원가입
                val toast = Toast.makeText(this, "회원가입 성공", Toast.LENGTH_LONG)
                toast.show()
                createUser(id, password)
            }
        }

        // id, 비밀번호 입력호 로그인 버튼 클릭 시
        binder.buttonLogin.setOnClickListener {
            val id = binder.editId.text.toString()
            val password = binder.editPs.text.toString()

            // 아이디 or 비밀번호를 입력하지 않았을 시
            if (id == "" || password == ""){
                val toast = Toast.makeText(this, "id 또는 비밀번호를 입력해주세요", Toast.LENGTH_LONG)
                toast.show()
            }else{
                // 입력한 해당 아이디로 로그인
                loginWithUser(id, password)
                val toast = Toast.makeText(this, "로그인 진행중", Toast.LENGTH_LONG)
                toast.show()
            }
        }

        // 로그아웃 버튼을 클릭했을 때
        binder.buttonLogout.setOnClickListener {
            Firebase.auth.signOut()
        }

        setContentView(binder.root)
    }

    override fun onStart() {
        super.onStart()
        // 이미 이메일 or 구글 등으로 로그인 되어있는지 확인
        val currentUser = auth.currentUser
        loginCheck(currentUser)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 구글 아이디 생성 또는 로그인에서 돌아왔을 때
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    // 구글로 로그인 시도
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    // 로그인이 성공되었을 때
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val user = auth.currentUser
                        val loginActivity = Intent(this, LoginSuccessActivity::class.java)
                        startActivityForResult(loginActivity, 100)
                    }
                    // 로그인이 실패했을 때
                    else {
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                    }
                }
    }

    // 유저 이메일 계정 생성
    private fun createUser(email : String, password : String){
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    // 로그인 성공 시
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        Toast.makeText(baseContext, "이메일 계정 생성 성공",
                                Toast.LENGTH_SHORT).show()
                        val user = auth.currentUser

                        // 로그인 실패 시
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "이메일 계정 생성 실패",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    // 유저 이메일로 로그인 시도
    private fun loginWithUser(email : String, password : String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    Toast.makeText(baseContext, "이메일 로그인 성공",
                            Toast.LENGTH_SHORT).show()
                    val loginIntent = Intent(this, LoginSuccessActivity::class.java)
                    startActivityForResult(loginIntent, loginSuccessCode)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)

                    Toast.makeText(baseContext, "이메일 로그인 실패",
                            Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loginCheck(user: FirebaseUser?){
        // 로그인 되어 있을 때
        if (user != null){
            val toast = Toast.makeText(this, "로그인 되어있습니다.", Toast.LENGTH_LONG)
            toast.show()
        }
        // 로그인 되어있지 않을 때
        else{
            val toast = Toast.makeText(this, "로그인 되어있지 않습니다.", Toast.LENGTH_LONG)
            toast.show()
        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}