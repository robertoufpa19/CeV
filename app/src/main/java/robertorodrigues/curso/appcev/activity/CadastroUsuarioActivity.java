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


    /// autenticacao do dispositivo com a conta do google
    private LinearLayout buttonCadastroUsuarioGoogle;
    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient mGoogleSignInClient; // Cliente de login do Google
    private static final String TAG = "GoogleActivity";

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

                            usuario = new Usuario();
                            usuario.setNome(campoNome);
                            usuario.setEmail(campoEmail);
                            usuario.setSenhaUsuario(campoSenha);
                            usuario.setTipo("cliente"); // empresa

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
                                            usuario.setTokenUsuario(token);

                                        }
                                    });    // fim cadastro do token

                               cadastrarUsuario(usuario);

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


        buttonCadastroUsuarioGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dadosUsuario();
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

                                // salvar dados da empresa no NO(usuario) do database
                                String idUsuarioEmpresa = task.getResult().getUser().getUid(); // pega o id da empresa em autenticacao

                                usuarioCadastro.setIdUsuario(idUsuarioEmpresa);
                                usuarioCadastro.salvarUsuario();


                                //salvar dados no profile do firebase(autentication)
                                UsuarioFirebase.atualizarNomeUsuario(usuarioCadastro.getNome());

                                progressBarCadastro.setVisibility(View.GONE);
                                exibirMensagem("Sucesso ao Cadastrar Usuario!");
                                startActivity(new Intent(getApplicationContext(), AnunciosActivity.class)); // abrir a tela principal
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





    // erro no metodo de login da google

    /// metodos de login com a conta do google inicio

    public void dadosUsuario(){

        // Configurar o Login do Google para autenticar dispositivo

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id2))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        entrar();  // abrir layouts de contas disponiveis no dispositivo


    }


    private void entrar() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // verifica se o usuário fez login (não nulo) e atualiza a interface de acordo.
        FirebaseUser currentUser = autenticacao.getCurrentUser();
        updateUI(currentUser);

        if(currentUser != null){
            startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));
        }

    }

    private void updateUI(FirebaseUser user) {

    }


    // [ inicio no resultado da atividade]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado retornado ao iniciar o Intent de GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // O login do Google foi bem-sucedido, autentique-se com o Firebase
                GoogleSignInAccount contaLoginGoogle = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + contaLoginGoogle.getId());
                firebaseAuthWithGoogle(contaLoginGoogle.getIdToken());



            } catch (ApiException e) {

                // Falha no login do Google, atualize a interface do usuário adequadamente
                Log.w(TAG, "Falha no login do Google", e);

                exibirMensagem("Erro ao fazer Login");
            }
        }


    }
    // [fim no resultado da atividade]


    // autenticação do firebase com o Google
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        autenticacao.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login bem-sucedido, atualiza a interface do usuário com as informações do usuário conectado
                            Log.d(TAG, "entrar com credencial: sucesso");
                            FirebaseUser user = autenticacao.getCurrentUser();
                            updateUI(user);


                            // dados do usuario
                            String idUsuario = task.getResult().getUser().getUid();
                            String emailUsuario = user.getEmail();
                            String nomeUsuario = user.getDisplayName();
                            String fotoUsuario = String.valueOf(user.getPhotoUrl());

                            usuario = new Usuario();
                            usuario.setIdUsuario(idUsuario);
                            usuario.setEmail(emailUsuario);
                            usuario.setNome(nomeUsuario);
                            usuario.setUrlImagem(fotoUsuario);
                            usuario.setTipo("cliente");


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
                                            usuario.setTokenUsuario(token);
                                            usuario.salvarUsuario();

                                            if(usuario != null){

                                                exibirMensagem("Sucesso ao fazer Login");
                                                startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));
                                                // startActivity(new Intent(this, HomeActivity.class));
                                                finish();
                                            }


                                        }
                                    });    // fim cadastro do token


                        } else {
                            // Se o login falhar, exibe uma mensagem para o usuário.
                            Log.w(TAG, "entrar com credencial: falha", task.getException());
                            updateUI(null);
                        }
                    }
                });
    }
    // [TERMINAR autenticação com o google]


    /// metodos de login com a conta do google fim


    public void inicializarComponentes(){
        campoCadastroNome   = findViewById(R.id.editCadastroNomeUsuario);
        campoCadastroEmail  = findViewById(R.id.editCadastroEmailUsuario);
        campoCadastroSenha  = findViewById(R.id.editCadastroSenhaUsuario);

        buttonCadastrar     = findViewById(R.id.buttonCadastrarUsuario);
        buttonCadastroUsuarioGoogle     = findViewById(R.id.buttonCadastroGoogleUsuario);
        progressBarCadastro = findViewById(R.id.progressBarCadastroUsuario);

        campoCadastroNome.requestFocus();// foco no campo nome
    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }
}