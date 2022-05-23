package robertorodrigues.curso.appcev.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskedittext.MaskEditText;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;
import robertorodrigues.curso.appcev.helper.UsuarioFirebase;
import robertorodrigues.curso.appcev.model.Usuario;

public class ConfiguracoesEmpresaActivity extends AppCompatActivity {

    private EditText editUsuarioNome,  editUsuarioEmail, editUsuarioCidade, editUsuarioBairro,
            editUsuarioRua, editUsuarioNumeroCasa;
    private MaskEditText editUsuarioTelefone;

    //private ImageView imagePerfilUsuario;
    private CircleImageView imagePerfilUsuario;

    private static  final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private DatabaseReference firebaseRef;
    private String idUsuarioLogado;
    private String urlImagemSelecionada = null;
    private AlertDialog dialog;

    private Usuario usuarioLogado;
    private String tipoUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes_empresa);

        //  getSupportActionBar().setTitle("Configurações");
        // configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);


        //configuracoes iniciais
        inicializarComponentes();
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        // selecionar foto de perfil
        imagePerfilUsuario.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, SELECAO_GALERIA);

                }

            }
        });

        //recuperar dados do empresa
        recuperarDadosEmpresa();

    }

    private  void recuperarDadosEmpresa(){
        DatabaseReference usuarioRef = firebaseRef
                .child("empresas")
                .child(idUsuarioLogado);
        usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    editUsuarioNome.setText(usuario.getNome());
                    editUsuarioEmail.setText(usuario.getEmail());
                    editUsuarioCidade.setText(usuario.getCidade());
                    editUsuarioBairro.setText(usuario.getBairro());
                    editUsuarioRua.setText(usuario.getRua());
                    editUsuarioNumeroCasa.setText(usuario.getNumero());
                    editUsuarioTelefone.setText(usuario.getTelefone());
                    String tipo = usuario.getTipo();

                    tipoUsuarioLogado = tipo;

                    //recuperar imagem de perfil da empresa
                    urlImagemSelecionada = usuario.getUrlImagem();
                    if (  urlImagemSelecionada != null ){ // urlImagemSelecionada != ""
                        Picasso.get()
                                .load(urlImagemSelecionada)
                                .into(imagePerfilUsuario);
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void validarDadosEmpresa(View view){  // metodo salvar

        // validar os campos que foram preenchidos
        String nome = editUsuarioNome.getText().toString();
        String email = editUsuarioEmail.getText().toString(); // ja esta recuperando o email
        String cidade = editUsuarioCidade.getText().toString();
        String bairro = editUsuarioBairro.getText().toString();
        String rua = editUsuarioRua.getText().toString();
        String numero = editUsuarioNumeroCasa.getText().toString();
        String telefone = editUsuarioTelefone.getText().toString();

        String foto = urlImagemSelecionada;


        if(foto != null) {
            if (!nome.isEmpty()) {
                if (!cidade.isEmpty()) {
                    if (!bairro.isEmpty()) {
                        if (!rua.isEmpty()) {
                            if (!numero.isEmpty()) {
                                if (!telefone.isEmpty()) {

                                    if(!email.isEmpty()){

                                        atualizarFotoUsuario(Uri.parse(foto));
                                        // usuarioLogado.setIdUsuario(idUsuarioLogado); // teste colocar depois se dar  erro

                                        atualizarNomeUsuario(nome);
                                        usuarioLogado.setCidade(cidade);
                                        usuarioLogado.setBairro(bairro);
                                        usuarioLogado.setRua(rua);
                                        usuarioLogado.setNumero(numero);
                                        usuarioLogado.setTelefone(telefone);
                                        usuarioLogado.setTipo("empresa");
                                        usuarioLogado.setEmail(email);




                                        // indentificador Usuario Token para enviar notificação para um usuario
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
                                                        usuarioLogado.setTokenUsuario(token);
                                                        usuarioLogado.setIdUsuario(idUsuarioLogado); // teste
                                                        usuarioLogado.atualizarEmpresa();
                                                        usuarioLogado.atualizar();
                                                       // usuarioLogado.atualizar();
                                                        exibirMensagem("Dados atualizados");

                                                        abrirHomeEmpresa();
                                                        finish();



                                                    }
                                                });    // fim cadastro do token




                                    }else{
                                        exibirMensagem("Digite seu E-mail!");
                                    }


                                } else { //telefone
                                    exibirMensagem("Digite seu numero de telefone!");
                                }

                            } else {
                                exibirMensagem("Digite o numero da sua casa!");
                            }

                        } else {
                            exibirMensagem("Digite o nome da sua rua!");
                        }

                    } else {
                        exibirMensagem("Digite o nome do seu bairro!");
                    }

                } else {
                    exibirMensagem("Digite o nome da sua cidade!");
                }


            } else {
                exibirMensagem("Digite seu nome!");
            }

        }else {
            exibirMensagem("Configure uma foto de Perfil!");
        }


    }

    public void atualizarFotoUsuario(Uri url){
        UsuarioFirebase.atualizarFotoUsuario(url);
        boolean retorno = UsuarioFirebase.atualizarFotoUsuario(url);
        if(retorno){
            usuarioLogado.setUrlImagem(url.toString());
            usuarioLogado.setIdUsuario(idUsuarioLogado); // teste
            usuarioLogado.atualizarEmpresa();
            usuarioLogado.atualizar();
            // exibirMensagem("foto alterada!");
        }

    }

    public void atualizarNomeUsuario(String nome){
        UsuarioFirebase.atualizarNomeUsuario(nome);
        boolean retorno = UsuarioFirebase.atualizarNomeUsuario(nome);
        if(retorno){
            usuarioLogado.setNome(nome);
            usuarioLogado.setIdUsuario(idUsuarioLogado); // teste
            usuarioLogado.atualizarEmpresa();
            usuarioLogado.atualizar();
            //  exibirMensagem("nome alterado!");
        }

    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {
                switch (requestCode){
                    case  SELECAO_GALERIA:
                        Uri localImagem = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagem);
                        break;
                }

                if(imagem != null){
                    imagePerfilUsuario.setImageBitmap(imagem);

                    // fazer upload da imagem para o firebase storage
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    // fazer upload da imagem antes de preencher outros campos
                    dialog = new SpotsDialog.Builder()
                            .setContext(this)
                            .setMessage("Carregando dados")
                            .setCancelable(false)
                            .build();
                    dialog.show();

                    final  StorageReference imageRef = storageReference
                            .child("imagens")
                            .child("usuarios")
                            .child(idUsuarioLogado + "jpeg");

                    UploadTask uploadTask = imageRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ConfiguracoesEmpresaActivity.this,
                                    "Erro ao fazer upload da imagem!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // recupera a url da imagem (versao atualizada do firebase)
                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Uri url =   task.getResult();
                                    urlImagemSelecionada = url.toString();


                                }
                            });

                            dialog.dismiss(); // fecha o carregando
                            Toast.makeText(ConfiguracoesEmpresaActivity.this,
                                    "Sucesso ao fazer upload da imagem!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }



    private void abrirHomeEmpresa(){
        startActivity(new Intent(ConfiguracoesEmpresaActivity.this, EmpresaActivity.class));
    }

    private void inicializarComponentes(){
        editUsuarioNome = findViewById(R.id.nomeEmpresaConfig);
        editUsuarioEmail = findViewById(R.id.emailEmpresaConfig);
        // evento de clique no campo email
        editUsuarioEmail.setFocusable(false); // nao vai poder selecionar esse campo de email
        editUsuarioEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exibirMensagem("Você não pode alterar o email de cadastro!");
            }
        });
        editUsuarioCidade = findViewById(R.id.cidadeEmpresaConfig);
        editUsuarioBairro = findViewById(R.id.bairroEmpresaConfig);
        editUsuarioRua = findViewById(R.id.ruaEmpresaConfig);
        editUsuarioNumeroCasa = findViewById(R.id.numeroLocalEmpresaConfig);
        editUsuarioTelefone = findViewById(R.id.phoneEmpresaConfig);
        imagePerfilUsuario = findViewById(R.id.imagePerfilEmpresaConfig);
    }
}