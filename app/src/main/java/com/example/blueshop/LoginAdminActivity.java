package com.example.blueshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginAdminActivity extends AppCompatActivity {
    EditText etnum, etcod;
    Button bCon, bVeri;
    private String verifId, phoneNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth auth;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_admin);
        etnum = (EditText) findViewById(R.id.etNumAdm);
        etcod = (EditText) findViewById(R.id.etCodAdm);
        bCon = (Button) findViewById(R.id.btnConfAdm);
        bVeri = (Button) findViewById(R.id.btnVerifyAdm);

        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);

        bVeri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumber = etnum.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(LoginAdminActivity.this, "Ingrese numero telefonico por favor", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.setTitle("Validando numero telefonico...");
                    dialog.setMessage("Por favor espere mientras validamos su numero.");
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(true);

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth).setPhoneNumber(phoneNumber).setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(LoginAdminActivity.this).setCallbacks(callbacks).build();
                    PhoneAuthProvider.verifyPhoneNumber(options);//Envia el numero
                }
            }
        });

        bCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bVeri.setVisibility(View.GONE);
                etnum.setVisibility(View.GONE);
                String veriCodigo = etcod.getText().toString();
                if(TextUtils.isEmpty(veriCodigo)){
                    Toast.makeText(LoginAdminActivity.this, "Ingrese el codigo enviado por favor", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.setTitle("Verificando");
                    dialog.setMessage("Espere por favor...");
                    dialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verifId, veriCodigo);
                    IngresadoConExito(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                IngresadoConExito(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                dialog.dismiss();
                Toast.makeText(LoginAdminActivity.this, "Fallo el Inicio de Sesion. Causas:\n" +
                        "1.Numero Invalido\n2.Sin conexion a internet\n3.Sin Codigo de Region", Toast.LENGTH_SHORT).show();
                etnum.setVisibility(View.VISIBLE);etcod.setVisibility(View.GONE);
                bVeri.setVisibility(View.VISIBLE);bCon.setVisibility(View.GONE);


            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                verifId = s;
                resendingToken = token;
                dialog.dismiss();
                Toast.makeText(LoginAdminActivity.this, "Codigo enviado correctamente," +
                        " revise su bandeja de entrada por favor", Toast.LENGTH_SHORT).show();
                etnum.setVisibility(View.GONE);etcod.setVisibility(View.VISIBLE);
                bVeri.setVisibility(View.GONE);bCon.setVisibility(View.VISIBLE);

            }
        };
    }

    private void IngresadoConExito(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    dialog.dismiss();
                    Toast.makeText(LoginAdminActivity.this, "Ingresado con Exito!", Toast.LENGTH_SHORT).show();
                    EnviarAlaPrincipal();
                }else{
                    String err = task.getException().toString();
                    Toast.makeText(LoginAdminActivity.this, "Error" + err, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser != null){
            EnviarAlaPrincipal();
        }
    }

    private void EnviarAlaPrincipal() {
        Intent i = new Intent(LoginAdminActivity.this, PrincipalActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra("phone", phoneNumber);
        i.putExtra("papel", "usuario");
        startActivity(i);
        finish();
    }
}