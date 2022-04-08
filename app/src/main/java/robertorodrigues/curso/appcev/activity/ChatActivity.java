package robertorodrigues.curso.appcev.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.adapter.MensagensAdapter;
import robertorodrigues.curso.appcev.api.NotificacaoService;
import robertorodrigues.curso.appcev.helper.Base64Custom;
import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;
import robertorodrigues.curso.appcev.helper.UsuarioFirebase;
import robertorodrigues.curso.appcev.model.Anuncio;
import robertorodrigues.curso.appcev.model.Conversa;
import robertorodrigues.curso.appcev.model.Mensagem;
import robertorodrigues.curso.appcev.model.Notificacao;
import robertorodrigues.curso.appcev.model.NotificacaoDados;
import robertorodrigues.curso.appcev.model.Usuario;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private ImageView circleImageViewFoto;

    private Anuncio anuncioUsuarioSelecionado;

    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;

    private String idUsuarioRemetente;
    private String  idUsuarioDestinatario;

    private EditText editMensagem;

    // recuperar mensagens
    private DatabaseReference database;
    private StorageReference storage;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;

    private List<Mensagem> mensagens = new ArrayList<>();
    private MensagensAdapter adapter;

    private RecyclerView recyclerMensagens;
    // variares de clique
    private ImageView imageCamera;
    private static final int SELECAO_CAMERA = 100;
    private ImageView imageGaleria;
    private static final int SELECAO_GALERIA = 200;

    private String baseUrl;
    private Retrofit retrofit;

    private String nomeUsuario, fotoUsuario;
    private DatabaseReference usuarioRef;

    // notificação
    private String token;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

      //  getSupportActionBar().hide();

        // configuracoes iniciais
        textViewNome = findViewById(R.id.textViewNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.imageCamera); // recupera foto tirada na hora para enviar no chat
        imageGaleria      = findViewById(R.id.imageGaleria);// recupera foto da galeria para enviar no chat


        //recupera dados do usuario remetente id
        idUsuarioRemetente = UsuarioFirebase.getIdUsuario();
        usuarioRemetente =  UsuarioFirebase.getDadosUsuarioLogado();

       // anuncioUsuarioSelecionado = UsuarioFirebase.getDadosUsuarioLogado();
       // usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase();


        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            if(bundle.containsKey("anuncioSelecionado")){

                anuncioUsuarioSelecionado  = (Anuncio) bundle.getSerializable("anuncioSelecionado");
                textViewNome.setText(anuncioUsuarioSelecionado.getNomeVendedor());
                //recuperar foto se o usuario tiver colocado
                String url = anuncioUsuarioSelecionado.getFotoVendedor();
                if(url != ""){
                    Picasso.get().load(url).into(circleImageViewFoto);
                }else{

                }

                idUsuarioDestinatario = anuncioUsuarioSelecionado.getIdUsuario(); // id destinatario
               // idUsuarioDestinatario = Base64Custom.codificarBase64( anuncioUsuarioSelecionado.getIdUsuario());

                recuperarTokenDestinatario();

            }else  if(bundle.containsKey("chat")){

                usuarioDestinatario  = (Usuario) bundle.getSerializable("chat");

                nomeUsuario = usuarioDestinatario.getNome();
               // idUsuarioDestinatario = UsuarioFirebase.getIdUsuario();
                fotoUsuario = usuarioDestinatario.getUrlImagem();

                textViewNome.setText(nomeUsuario);
                if(fotoUsuario != ""){
                    Picasso.get()
                            .load(fotoUsuario)
                            .into(circleImageViewFoto);
                }else{
                    circleImageViewFoto.setImageResource(R.drawable.perfil);
                }

                idUsuarioDestinatario = usuarioDestinatario.getIdUsuario();

                recuperarTokenDestinatario();

            }


        }


        //Configuração adapter
        adapter = new MensagensAdapter(mensagens, getApplicationContext() );

        //Configuração recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager( layoutManager );
        recyclerMensagens.setHasFixedSize( true );
        recyclerMensagens.setAdapter( adapter );

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storage = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child( idUsuarioRemetente )
                .child( idUsuarioDestinatario );

        // evento de click na camera do chat
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if(intent.resolveActivity(getPackageManager())!= null){
                    startActivityForResult(intent, SELECAO_CAMERA);
                }
            }
        });
        // evento de click na galeria do chat

        imageGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(intent.resolveActivity(getPackageManager())!= null){
                    startActivityForResult(intent, SELECAO_GALERIA);
                }
            }
        });



        //Configuração da retrofit para enviar requisição ao firebase e então para ele enviar a notificação
        baseUrl = "https://fcm.googleapis.com/fcm/";
        retrofit = new Retrofit.Builder()
                .baseUrl( baseUrl )
                .addConverterFactory(GsonConverterFactory.create())
                .build();


    }


    public void enviarMensagem(View view) {

        Bundle bundleEnviarMensagem = getIntent().getExtras();

        if(bundleEnviarMensagem != null){
            String textoMensagem = editMensagem.getText().toString();
            if ( !textoMensagem.isEmpty() ){
                if(bundleEnviarMensagem.containsKey("anuncioSelecionado")){ // se o usuario selecionar o botao enviar mensagens ao vendedor
                    anuncioUsuarioSelecionado = (Anuncio) bundleEnviarMensagem.getSerializable("anuncioSelecionado");

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario( idUsuarioRemetente );
                    mensagem.setMensagem( textoMensagem );

                    //Salvar mensagem para o remetente
                    salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                    //Salvar mensagem para o destinatario(falta salvar pro destinatario)
                    salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                    //Salvar conversa para o remetente
                    salvarConversa( idUsuarioRemetente, idUsuarioDestinatario, anuncioUsuarioSelecionado.getUsuarioExibicao(),  mensagem, false);


                    //Salvar conversa para o destinatario
                    salvarConversa(  idUsuarioDestinatario, idUsuarioRemetente,usuarioRemetente ,  mensagem, true);


                 
                }else if(bundleEnviarMensagem.containsKey("chat")){ // se caso o usuario abra o menu de conversas
                    usuarioDestinatario = (Usuario) bundleEnviarMensagem.getSerializable("chat");


                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario( idUsuarioRemetente );
                    mensagem.setMensagem( textoMensagem );

                    //Salvar mensagem para o remetente
                    salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                    //Salvar mensagem para o destinatario(falta salvar pro destinatario)
                    salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                    //Salvar conversa para o remetente                  // tem que recuperar usuario exibicao
                    salvarConversa( idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario,  mensagem, false);


                    //Salvar conversa para o destinatario
                    salvarConversa(  idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente,  mensagem, true);


                }

            }else{
                exibirMensagem("Digite Sua Mensagem!");
            }

        }

        enviarNotificacao();

    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

        //Limpar texto
        editMensagem.setText("");

    }


    // salva conversa para a empresa
    private void salvarConversa(String idRemetente, String idDestinatario,Usuario usuarioExibição,  Mensagem msg, boolean novaMensagem){

        //Salvar conversa remetente
        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente( idRemetente);
        conversaRemetente.setIdDestinatario( idDestinatario );
        conversaRemetente.setUltimaMensagem(msg.getMensagem());
        conversaRemetente.setUsuarioExibicaoAnuncio(anuncioUsuarioSelecionado); // dados do anuncio

        // salvar  mensagens novas
        conversaRemetente.setNovaMensagem(String.valueOf(novaMensagem));


        conversaRemetente.setUsuarioExibicao(usuarioExibição);


        conversaRemetente.salvar();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        recuperarTokenDestinatario();

        if(resultCode == RESULT_OK){
            Bitmap imagem = null;
            try {
                switch (requestCode){
                    case SELECAO_CAMERA:
                        imagem =(Bitmap) data.getExtras().get("data");
                        break;

                    case SELECAO_GALERIA:
                        Uri localImagemSelecionada = data.getData();
                        imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);

                        break;
                }

                if(imagem != null){

                    //  imageGaleria.setImageBitmap(imagem);


                    // recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    // cria nome que não se repete
                    String nomeImagem = UUID.randomUUID().toString();

                    // configurar referencia do firebase
                    final StorageReference imageRef = storage.child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem);

                    UploadTask uploadTask = imageRef.putBytes(dadosImagem);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Erro", "Erro ao fazer upload");

                            Toast.makeText(ChatActivity.this,
                                    "Erro ao fazer upload da imagem!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // String dowloadUrl = taskSnapshot.getDownloadUrl().toString();

                            //teste para nova versão
                            imageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String dowloadUrl =  task.getResult().toString();

                                    Bundle bundleEnviarMensagem = getIntent().getExtras();


                                    if(bundleEnviarMensagem != null){

                                        if(bundleEnviarMensagem.containsKey("anuncioSelecionado")){
                                            anuncioUsuarioSelecionado  = (Anuncio) bundleEnviarMensagem.getSerializable("anuncioSelecionado");

                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario( idUsuarioRemetente );
                                            mensagem.setMensagem("imagem.jpeg");
                                            mensagem.setImagem(dowloadUrl);

                                            //Salvar mensagem para o remetente
                                            salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                                            //Salvar mensagem para o destinatario(falta salvar pro destinatario)
                                            salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);


                                            //Salvar conversa para o remetente
                                            salvarConversa( idUsuarioRemetente, idUsuarioDestinatario, anuncioUsuarioSelecionado.getUsuarioExibicao(),  mensagem, false);

                                            //Salvar conversa para o destinatario
                                            salvarConversa(  idUsuarioDestinatario, idUsuarioRemetente,usuarioRemetente ,  mensagem, true);

                                            enviarNotificacao();


                                        } else if(bundleEnviarMensagem.containsKey("chat")){
                                            usuarioDestinatario  = (Usuario) bundleEnviarMensagem.getSerializable("chat");


                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario( idUsuarioRemetente );
                                            mensagem.setMensagem("imagem.jpeg");
                                            mensagem.setImagem(dowloadUrl);

                                            //Salvar mensagem para o remetente
                                            salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                                            //Salvar mensagem para o destinatario(falta salvar pro destinatario)
                                            salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);


                                            //Salvar conversa para o remetente
                                                               // tem que recuperar usuario exibicao
                                            salvarConversa( idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario,  mensagem, false);

                                            //Salvar conversa para o destinatario
                                            salvarConversa(  idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente,  mensagem, true);

                                              enviarNotificacao();

                                        }





                                    }


                                }
                            });



                        }
                    });

                }

            }catch (Exception e){
                e.printStackTrace();
            }


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
        recuperarTokenDestinatario();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener( childEventListenerMensagens );
    }

    private void recuperarMensagens(){

        mensagens.clear(); //teste
        recuperarTokenDestinatario();

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Mensagem mensagem = dataSnapshot.getValue( Mensagem.class );
                mensagens.add( mensagem );
                adapter.notifyDataSetChanged();
                // da o foco na ultima mensagem enviada
                recyclerMensagens.scrollToPosition(mensagens.size() -1 );

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public  void  recuperarTokenDestinatario(){

        Bundle bundleToken = getIntent().getExtras();
        if(bundleToken  != null){
            if(bundleToken.containsKey("anuncioSelecionado")){

                anuncioUsuarioSelecionado  = (Anuncio) bundleToken.getSerializable("anuncioSelecionado");
                token = anuncioUsuarioSelecionado.getTokenVendedor();


            }else if(bundleToken.containsKey("chat")){


                usuarioDestinatario = (Usuario) bundleToken.getSerializable("chat");
                // token = usuarioDestinatario.getTokenUsuario();
                // recuperar token do NO usuarios
                usuarioRef =  ConfiguracaoFirebase.getFirebaseDatabase()
                               .child("usuarios")
                               .child(idUsuarioDestinatario)
                               .child("tokenUsuario");
                usuarioRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String tokenUsuario =  snapshot.getValue().toString();
                        token = tokenUsuario;

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        }
    }


    public void enviarNotificacao(){

        Bundle bundleNotificacao = getIntent().getExtras();
        if(bundleNotificacao.containsKey("anuncioSelecionado")){
             anuncioUsuarioSelecionado  = (Anuncio) bundleNotificacao.getSerializable("anuncioSelecionado");
            token = anuncioUsuarioSelecionado.getTokenVendedor();

            String tokenDestinatario = token;
            String to = "";// para quem vou enviar a menssagem
            to = tokenDestinatario ;

            //Monta objeto notificação
            Notificacao notificacao = new Notificacao("CeV", "Nova Mensagem\n " + usuarioRemetente.getNome());
            NotificacaoDados notificacaoDados = new NotificacaoDados(to, notificacao );

            NotificacaoService service = retrofit.create(NotificacaoService.class);
            Call<NotificacaoDados> call = service.salvarNotificacao( notificacaoDados );

            call.enqueue(new Callback<NotificacaoDados>() {
                @Override
                public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {
                    if( response.isSuccessful() ){

                        //teste para verificar se enviou a notificação
                           /*  Toast.makeText(getApplicationContext(),
                                     "codigo: " + response.code(),
                                     Toast.LENGTH_LONG ).show();

                            */

                    }
                }

                @Override
                public void onFailure(Call<NotificacaoDados> call, Throwable t) {

                }
            });

        }else if(bundleNotificacao.containsKey("chat")){

            usuarioDestinatario  = (Usuario) bundleNotificacao.getSerializable("chat");
           // token = usuarioDestinatario.getTokenUsuario();
             // recuperar token do NO usuarios
            usuarioRef =  ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("usuarios")
                    .child(idUsuarioDestinatario)
                    .child("tokenUsuario");
            usuarioRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String tokenUsuario =  snapshot.getValue().toString();
                    token = tokenUsuario;

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            String tokenDestinatario = token;
            String to = "";// para quem vou enviar a menssagem
            to = tokenDestinatario ;

            //Monta objeto notificação
            Notificacao notificacao = new Notificacao("CeV", "Nova Mensagem\n " + usuarioRemetente.getNome());
            NotificacaoDados notificacaoDados = new NotificacaoDados(to, notificacao );

            NotificacaoService service = retrofit.create(NotificacaoService.class);
            Call<NotificacaoDados> call = service.salvarNotificacao( notificacaoDados );

            call.enqueue(new Callback<NotificacaoDados>() {
                @Override
                public void onResponse(Call<NotificacaoDados> call, Response<NotificacaoDados> response) {
                    if( response.isSuccessful() ){

                        //teste para verificar se enviou a notificação
                           /*  Toast.makeText(getApplicationContext(),
                                     "codigo: " + response.code(),
                                     Toast.LENGTH_LONG ).show();

                            */

                    }
                }

                @Override
                public void onFailure(Call<NotificacaoDados> call, Throwable t) {

                }
            });
        }
    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }


}