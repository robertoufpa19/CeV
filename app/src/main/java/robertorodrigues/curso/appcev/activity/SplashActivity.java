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

    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

        // getSupportActionBar().hide(); // esconde toolbar

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                    verificarUsuarioLogado();

            }
        }, 3000); // 3 segundos

    }



    // abrir tela principal se o usuario estiver logado
    public void verificarUsuarioLogado(){

        if(autenticacao.getCurrentUser() != null){
            // startActivity(new Intent(getApplicationContext(), MainActivity.class)); // abrir a tela principal
            UsuarioFirebase.redirecionaUsuarioLogado(SplashActivity.this);

        }else{
            abrirLogin();
        }
    }

    private void abrirLogin(){
        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
    }



}