package robertorodrigues.curso.appcev.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;
import robertorodrigues.curso.appcev.helper.UsuarioFirebase;
import robertorodrigues.curso.appcev.model.Usuario;

public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        // getSupportActionBar().hide(); // esconde toolbar

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                  abrirLogin();

            }
        }, 1000); // 1 segundos

    }


    private void abrirLogin(){
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
    }



}