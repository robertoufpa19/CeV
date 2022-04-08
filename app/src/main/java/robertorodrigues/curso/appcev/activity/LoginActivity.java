package robertorodrigues.curso.appcev.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.messaging.FirebaseMessaging;

import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;
import robertorodrigues.curso.appcev.helper.UsuarioFirebase;
import robertorodrigues.curso.appcev.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    private Button botaoAcesso;
    private EditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;
    private Usuario usuario;
    private ProgressBar progressBarCadastro;
    private Button cadastrarEmpresa;
    private Button loginEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

      //  getSupportActionBar().hide(); // esconde toolbar

        exibirMensagem("Nao tem conta?");
        exibirMensagem("Cadastre-se!");

        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();



        //cadastrar usuario
        progressBarCadastro.setVisibility(View.GONE);
        botaoAcesso.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = campoEmail.getText().toString();
                String senha= campoSenha.getText().toString();

                   usuario = new Usuario();
                   usuario.setEmail(email);


                if(!email.isEmpty()){
                    if(!senha.isEmpty()){

                            autenticacao.signInWithEmailAndPassword(
                                    email, senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){

                                        // limpar campos
                                        campoEmail.setText("");
                                        campoSenha.setText("");
                                        //entra na pagina de anuncios
                                       // startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));

                                        // verifica o tipo de usuario(empresa ou cliente)
                                        UsuarioFirebase.redirecionaUsuarioLogado(LoginActivity.this);


                                    }else{
                                        Toast.makeText(LoginActivity.this,
                                                "Erro ao fazer Login. Verifique sua conexão com a internet!",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    Toast.makeText(LoginActivity.this,
                                            "Logado com Sucesso!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

                        // vizualizar quando clicado o botão de cadastro
                        progressBarCadastro.setVisibility(View.VISIBLE);

                    }else{
                        Toast.makeText(LoginActivity.this,
                                "Preencha o senha",
                                Toast.LENGTH_SHORT).show();

                    }

                }else{
                    Toast.makeText(LoginActivity.this,
                            "Preencha o email",
                            Toast.LENGTH_SHORT).show();

                }


            }
        });


        cadastrarEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //entra na pagina de cadastro empresa
                startActivity(new Intent(getApplicationContext(), CadastroEmpresaActivity.class));

            }
        });

        loginEmpresa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //entra na pagina de login empresa
                startActivity(new Intent(getApplicationContext(), CadastroUsuarioActivity.class));

            }
        });



    }

    private void  inicializarComponentes(){
        campoEmail = findViewById(R.id.editCadastroEmailEmpresa);
        campoSenha = findViewById(R.id.editCadastroSenhaEmpresa);
        botaoAcesso = findViewById(R.id.buttonAcesso);
        progressBarCadastro = findViewById(R.id.progressBarCadastro);

        cadastrarEmpresa = findViewById(R.id.buttonCadastrarEmpresa1);
        loginEmpresa = findViewById(R.id.buttonCadastroUsuario);
    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }


    private void redirecionarUsuario(){
        if(autenticacao != null){
            // verifica o tipo de usuario(empresa ou cliente)
            UsuarioFirebase.redirecionaUsuarioLogado(LoginActivity.this);

        }else{



        }
    }




    @Override
    protected void onStart() {
        super.onStart();

    }
}