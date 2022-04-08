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

public class CadastroEmpresaActivity extends AppCompatActivity {

    private EditText campoCadastroNome, campoCadastroEmail, campoCadastroSenha;
    private Button buttonCadastrar;
    private ProgressBar progressBarCadastro;
    private Usuario empresaUsuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_empresa);

      inicializarComponentes();

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

                            empresaUsuario = new Usuario();
                            empresaUsuario.setNome(campoNome);
                            empresaUsuario.setEmail(campoEmail);
                            empresaUsuario.setSenhaUsuario(campoSenha);
                            empresaUsuario.setTipo("empresa"); // empresa

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
                                            empresaUsuario.setTokenUsuario(token);

                                        }
                                    });    // fim cadastro do token

                            cadastrarEmpresa(empresaUsuario);

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

    public void cadastrarEmpresa(Usuario empresa){
        progressBarCadastro.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
                empresa.getEmail(),
                empresa.getSenhaUsuario()
        ).addOnCompleteListener(
                this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            try{

                                // salvar dados da empresa no NO(usuario) do database
                                String idUsuarioEmpresa = task.getResult().getUser().getUid(); // pega o id da empresa em autenticacao

                                empresa.setIdUsuario(idUsuarioEmpresa);
                                empresa.salvarEmpresa(); // salva no NO empresa
                                empresa.salvarUsuario(); // salva no NO usuarios


                                //salvar dados no profile do firebase(autentication)
                                UsuarioFirebase.atualizarNomeUsuario(empresa.getNome());

                                progressBarCadastro.setVisibility(View.GONE);
                                exibirMensagem("Sucesso ao Cadastrar Empresa!");
                                startActivity(new Intent(getApplicationContext(), EmpresaActivity.class)); // abrir a tela principal
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

                            Toast.makeText(CadastroEmpresaActivity.this,
                                    excecao,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }




    public void inicializarComponentes(){
        campoCadastroNome   = findViewById(R.id.editCadastroNomeEmpresa);
        campoCadastroEmail  = findViewById(R.id.editCadastroEmailEmpresa);
        campoCadastroSenha  = findViewById(R.id.editCadastroSenhaEmpresa);
        buttonCadastrar     = findViewById(R.id.buttonCadastrarEmpresa);
        progressBarCadastro = findViewById(R.id.progressBarCadastroEmpresa);

        campoCadastroNome.requestFocus();// foco no campo nome
    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }
}