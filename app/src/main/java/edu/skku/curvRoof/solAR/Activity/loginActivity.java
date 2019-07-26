package edu.skku.curvRoof.solAR.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.skku.curvRoof.solAR.R;

public class loginActivity extends AppCompatActivity {
    private Button loginBtn, registerBtn;
    private EditText idEt, pwdEt;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        loginBtn = (Button)findViewById(R.id.loginBtn);
        idEt = (EditText)findViewById(R.id.idEt);
        pwdEt = (EditText)findViewById(R.id.pwdEt);
        registerBtn = (Button)findViewById(R.id.registerBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = idEt.getText().toString();
                String password = pwdEt.getText().toString();
                if(!email.trim().equals("") && !password.trim().equals("")){
                    signIn(email, password);
                }
                else{
                    idEt.setText("");
                    pwdEt.setText("");
                    Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder newAccountDialog = new AlertDialog.Builder(loginActivity.this);
                newAccountDialog.setTitle("Input Account Information");
                LayoutInflater inflater = getLayoutInflater();
                View custom_view = inflater.inflate(R.layout.dialog_register, null);
                newAccountDialog.setView(custom_view);

                final EditText newEmailEt = custom_view.findViewById(R.id.newEmailEt);
                final EditText newPwdEt = custom_view.findViewById(R.id.newPwdEt);

                newAccountDialog.setPositiveButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String email = newEmailEt.getText().toString();
                        String password = newPwdEt.getText().toString();

                        if(!email.trim().equals("") && !password.trim().equals("")){
                            createAccount(email, password);
                        }
                        else{
                            newEmailEt.setText("");
                            newPwdEt.setText("");
                            Toast.makeText(getApplicationContext(), "Register Failed", Toast.LENGTH_SHORT).show();
                        }
                        dialogInterface.dismiss();
                    }
                });
                newAccountDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                newAccountDialog.show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void createAccount(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                }
                else{
                    Toast.makeText(loginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    Intent intent = new Intent(loginActivity.this, MainActivity.class);
                    try{
                        intent.putExtra("ID", user.getEmail());
                    }catch(NullPointerException e){
                        Toast.makeText(getApplicationContext(), "Wrong Email", Toast.LENGTH_SHORT).show();
                    }
                    startActivity(intent);
                }
                else{
                    Toast.makeText(loginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
