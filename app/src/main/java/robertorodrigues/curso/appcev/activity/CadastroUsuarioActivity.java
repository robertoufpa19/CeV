package robertorodrigues.curso.appcev.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;
import robertorodrigues.curso.appcev.helper.UsuarioFirebase;
import robertorodrigues.curso.appcev.model.Usuario;

public class CadastroUsuarioActivity extends AppCompatActivity {

    private EditText campoCadastroNome, campoCadastroEmail, campoCadastroSenha;
    private Button buttonCadastrar;
    private ProgressBar progressBarCadastro;
    private Usuario usuario;
    private FirebaseAuth autenticacao;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_usuario);

        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();


        //cadastrar usuario
        progressBarCadastro.setVisibility(View.GONE);
        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String campoNome = campoCadastroNome.getText().toString();
                String campoEmail = campoCadastroEmail.getText().toString();
                String campoSenha = campoCadastroSenha.getText().toString();

                if(!campoNome.isEmpty()){
                    if(!campoEmail.isEmpty()){
                        if(!campoSenha.isEmpty()){

                            // inicio cadastro do token usuario
                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            if (!task.isSuccessful()) {
                                                Log.w("Cadastro token", "Fetching FCM registration token failed", task.getException());
                                                return;
                                            }
                                            // Get new FCM registration token
                                            String token = task.getResult();
                                            usuario = new Usuario();
                                            usuario.setNome(campoNome);
                                            usuario.setEmail(campoEmail);
                                            usuario.setSenhaUsuario(campoSenha);
                                            usuario.setTipo("cliente"); // empresa
                                            usuario.setTokenUsuario(token);
                                            cadastrarUsuario(usuario);


                                        }
                                    });    // fim cadastro do token



                        }else {
                            exibirMensagem("Preencha o campo Senha");
                        }

                    }else {
                        exibirMensagem("Preencha o campo Email");
                    }
                }else {
                    exibirMensagem("Preencha o campo Nome");
                }
            }
        });





    }

    public void cadastrarUsuario(Usuario usuarioCadastro){
        progressBarCadastro.setVisibility(View.VISIBLE);

        autenticacao.createUserWithEmailAndPassword(
                usuarioCadastro.getEmail(),
                usuarioCadastro.getSenhaUsuario()
        ).addOnCompleteListener(
                this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            try{

                                String idUsuario = task.getResult().getUser().getUid();

                                usuarioCadastro.setIdUsuario(idUsuario);
                                //salvar dados no profile do firebase(autentication)
                               // UsuarioFirebase.atualizarNomeUsuario(usuarioCadastro.getNome());
                                usuarioCadastro.salvarUsuario();

                                progressBarCadastro.setVisibility(View.GONE);
                                exibirMensagem("Sucesso ao Cadastrar Usuario!");
                                startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));
                                finish();

                            }catch (Exception e){
                                e.printStackTrace();
                            }


                        }else{
                            progressBarCadastro.setVisibility(View.GONE);

                            String excecao = "";

                            try{
                                throw task.getException();
                            }catch (FirebaseAuthWeakPasswordException e){
                                excecao = "Digite uma senha mais forte!";
                            }catch (FirebaseAuthInvalidCredentialsException e){
                                excecao = "Digite uma email valido!";
                            }catch (FirebaseAuthUserCollisionException e){
                                excecao = "Esta conta ja foi cadastrada!";
                            }catch (Exception e){
                                excecao = "Erro ao cadastrar o usuario!"+ e.getMessage();
                                e.printStackTrace();
                            }

                            Toast.makeText(CadastroUsuarioActivity.this,
                                    excecao,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }







    public void inicializarComponentes(){
        campoCadastroNome   = findViewById(R.id.editCadastroNomeUsuario);
        campoCadastroEmail  = findViewById(R.id.editCadastroEmailUsuario);
        campoCadastroSenha  = findViewById(R.id.editCadastroSenhaUsuario);

        buttonCadastrar     = findViewById(R.id.buttonCadastrarUsuario);
        progressBarCadastro = findViewById(R.id.progressBarCadastroUsuario);

        campoCadastroNome.requestFocus();// foco no campo nome
    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }
}