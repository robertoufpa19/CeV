package robertorodrigues.curso.appcev.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskedittext.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import me.abhinay.input.CurrencyEditText;
import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;
import robertorodrigues.curso.appcev.helper.Permissoes;
import robertorodrigues.curso.appcev.helper.UsuarioFirebase;
import robertorodrigues.curso.appcev.model.Anuncio;
import robertorodrigues.curso.appcev.model.Usuario;

public class CadastrarAnuncioActivity extends AppCompatActivity
        implements View.OnClickListener{

    private EditText campoTitulo, campoDescricao;
    private CurrencyEditText campoValor;
    private MaskEditText campoTelefone;
    private ImageView imagem1, imagem2, imagem3;
    private Spinner campoEstado, campoCategoria;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private List<String> listaFotosRecuperadas = new ArrayList<>(); // lista o caminho de fotos no dispositivo do usuario
    private List<String> listaURLFotos = new ArrayList<>(); // lista o caminho de fotos no firebase

    private Anuncio anuncio;
    private StorageReference storage;
    private AlertDialog dialog;

    private Usuario usuario;
    private String idUsuarioLogado;
    private DatabaseReference firebaseRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);

        // configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Cadastrar Anúncio");
        setSupportActionBar(toolbar);

        //configuracoes iniciais
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //validar permissoes
        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();
        carregarDadosSpinner();
        recuperarDadosUsuario();

        usuario = UsuarioFirebase.getDadosUsuarioLogado();


    }

    public void salvarAnuncio(){

       dialog = new SpotsDialog.Builder()
               .setContext(this)
               .setMessage("Salvando Anuncio!")
               .setCancelable(false)
               .build();
       dialog.show();

           // salvar a imagem no storage
          // o indice ZERO corresponde a imagem 1
          for(int i =0; i < listaFotosRecuperadas.size(); i++){
              String urlImagem = listaFotosRecuperadas.get(i);
              int tamanhoLIsta = listaFotosRecuperadas.size();

             salvarFotoStorage( urlImagem, tamanhoLIsta, i);

          }

    }

    private void salvarFotoStorage(String urlString, int totalFotos, int contador){

         //criar no no storage
       final StorageReference imagemAnuncio = storage.child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem"+contador); //imagem1, imagem2, imagem3

        //fazer upload das imagens
        UploadTask uploadTask = imagemAnuncio.putFile(Uri.parse(urlString));
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    imagemAnuncio.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            Uri firebaseUrl = task.getResult();

                             String urlConvertida = firebaseUrl.toString();
                             listaURLFotos.add(urlConvertida);

                             if(totalFotos == listaURLFotos.size()){
                                 anuncio.setFotos(listaURLFotos);
                                 anuncio.salvarAnuncio();
                                 dialog.dismiss(); // fecha dialog
                                 finish(); // finaliza a activity

                             }
                        }
                    });

                        exibirMensagem("Sucesso ao fazer upload da imagem!");


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        exibirMensagem("Erro ao fazer upload da imagem!");
                    }
                });

    }

    private Anuncio configurarAnuncio(){
        String estado = campoEstado.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTitulo.getText().toString();
        String valor = campoValor.getText().toString();
        String telefone = campoTelefone.getText().toString();
        String descricao = campoDescricao.getText().toString();

        anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria(categoria);
        anuncio.setTitulo(titulo);
        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
        anuncio.setDescricao(descricao);
        //falta salvar foto e nome do vendedor
         if(usuario != null){
             String  nomeVendedor =  usuario.getNome();
             String  fotoVendedor = usuario.getUrlImagem();
             String idUsuario = ConfiguracaoFirebase.getIdUsuario();
             anuncio.setNomeVendedor(nomeVendedor);
             anuncio.setFotoVendedor(fotoVendedor);
             anuncio.setIdUsuario(idUsuario);
             anuncio.setUsuarioExibicao(usuario);
         }



    return  anuncio;

    }

    public  void validarDadosAnuncios(View view){

        anuncio = configurarAnuncio();

        String fone = ""; // campo nao pode esta nulo senoa da erro
        if(campoTelefone.getRawText() != null){
            fone = campoTelefone.getRawText().toString();
        }


         if(listaFotosRecuperadas.size() != 0){
              if(!anuncio.getEstado().isEmpty()){
                  if(!anuncio.getCategoria().isEmpty()){
                      if(!anuncio.getTitulo().isEmpty()){
                          if(!anuncio.getValor().isEmpty()){
                              if(!anuncio.getTelefone().isEmpty() && fone.length() >= 10){ // condicao para obrigar o usuario a colocar todos os numeros no campo telefone
                                  if(!anuncio.getDescricao().isEmpty()){

                                     salvarAnuncio();

                                  }else {
                                      exibirMensagem("Digite uma descricao");
                                  }
                              }else{
                                  exibirMensagem("Digite um telefone com ao menos 10 numeros");
                              }
                          }else{
                              exibirMensagem("Digite um valor");
                          }

                      }else{
                          exibirMensagem("Digite um titulo");
                      }

                  }else {
                      exibirMensagem("Selecione uma categoria");
                  }

              }else{
                  exibirMensagem("Selecione um Estado");
              }
         }else{
           exibirMensagem("Selecione uma foto!");
         }

    }




    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.imageCadastro1:
                escolherImagem(1);
                break;

            case R.id.imageCadastro2:
                escolherImagem(2);
                break;

            case R.id.imageCadastro3:
                escolherImagem(3);
                break;


        }

    }

    public  void escolherImagem(int requestCode){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);
    }

    // captura a imagem escolhida
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            //recuperar imagem
            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            //configiurar imagem no ImageView
            if(requestCode == 1){
                imagem1.setImageURI(imagemSelecionada);

            }else if(requestCode == 2){
                imagem2.setImageURI(imagemSelecionada);
            }else if(requestCode == 3){
                imagem3.setImageURI(imagemSelecionada);
            }

            listaFotosRecuperadas.add(caminhoImagem);
        }
    }

    private void carregarDadosSpinner(){
      /* String[] estados = new String[]{
          "SP", "PA"
       };*/
         /* String[] categoria = new String[]{
          "Moda", "Automoveis"
       };*/
        // spinner estado
        String[] estados = getResources().getStringArray(R.array.regiao);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                estados
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoEstado.setAdapter(adapter);

          // spinner categoria
        String[] categoria = getResources().getStringArray(R.array.categorias);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                categoria
        );

        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoCategoria.setAdapter(adapterCategoria);

    }

    private void recuperarDadosUsuario(){
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference usuarioRef = firebaseRef
                .child("usuarios")
                .child(idUsuarioLogado);
        // recupera dados uma unica vez
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    usuario = snapshot.getValue(Usuario.class);
                }
               // recuperarAnunciosPublicos();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private  void inicializarComponentes(){
        campoDescricao = findViewById(R.id.editDescricao);
        campoTitulo = findViewById(R.id.editTitulo);
        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        imagem1 = findViewById(R.id.imageCadastro1);
        imagem2 = findViewById(R.id.imageCadastro2);
        imagem3 = findViewById(R.id.imageCadastro3);
        campoEstado = findViewById(R.id.spinnerEstado);
        campoCategoria = findViewById(R.id.spinnerCategoria);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);

        // configurar localidade para pt -> portugues BR -> Brasil
        Locale locale = new Locale("pt", "BR");
        campoValor.setTextLocale(locale);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for( int permissaoResultado : grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidarPermissao();
            }
        }
    }


    private void alertaValidarPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissoes Negadas");
        builder.setMessage("Para utilizar o app e necessario aceitar as permissoes");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

}