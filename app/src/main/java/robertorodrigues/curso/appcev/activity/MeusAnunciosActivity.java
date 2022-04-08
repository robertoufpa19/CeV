package robertorodrigues.curso.appcev.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;
import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.adapter.AdapterAnuncios;
import robertorodrigues.curso.appcev.adapter.AdapterMeusAnuncios;
import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;
import robertorodrigues.curso.appcev.helper.RecyclerItemClickListener;
import robertorodrigues.curso.appcev.helper.UsuarioFirebase;
import robertorodrigues.curso.appcev.model.Anuncio;
import robertorodrigues.curso.appcev.model.Usuario;

public class MeusAnunciosActivity extends AppCompatActivity  {

      private RecyclerView recyclerAnuncios;
      private List<Anuncio> anuncios = new ArrayList<>();
     // private AdapterAnuncios adapterAnuncios;
      private AdapterMeusAnuncios adapterMeusAnuncios;
      private DatabaseReference anunciosUsuarioRef;
      private AlertDialog dialog;

    private Usuario usuario;
    private String idUsuarioLogado;
    private DatabaseReference firebaseRef;
    private FirebaseAuth autenticacao;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_anuncios);

        // configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Meus Anúncios");
        setSupportActionBar(toolbar);

        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();
        //configuracoes iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        usuario = UsuarioFirebase.getDadosUsuarioLogado();

        inicializarComponentes();


        FloatingActionButton fab =  findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //condicao para verificar se o usuario configurou seus dados(perfil)
                if(usuario.getNome() != null){ //teste
                    //entra na pagina de cadastro de anuncios
                    startActivity(new Intent(getApplicationContext(), CadastrarAnuncioActivity.class));

                }else{
                    exibirMensagem("Configure seu Perfil, antes de publicar produtos");
                    abrirConfiguracoes();
                }

            }
        });

       // configurar recyclerView
        recyclerAnuncios.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnuncios.setHasFixedSize(true);
        adapterMeusAnuncios = new AdapterMeusAnuncios(anuncios, this);
        recyclerAnuncios.setAdapter(adapterMeusAnuncios);

        // recuperar anuncios para os usuarios
         recuperarAnuncios(); // erro ao recuperar anuncios particulares de cada usuario(esta pegando todos)
         recuperarDadosUsuario();


        // adicionar evento de clique no recycler view
        /*
        recyclerAnuncios.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerAnuncios,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                 // unico clique
                                  exibirMensagem("Dei dois clique no icone para excluir");
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                  // clique longo

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        ); */


    }


    private void recuperarAnuncios(){

        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anuncios!")
                .setCancelable(false)
                .build();
        dialog.show();

        //  configuracoes iniciais
        anunciosUsuarioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("meus_anuncios")
                .child(idUsuarioLogado);



        anunciosUsuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 anuncios.clear();

                 for (DataSnapshot ds : snapshot.getChildren()){
                     anuncios.add(ds.getValue(Anuncio.class));

                 }

                Collections.reverse(anuncios); // exibicao reversa dos anuncios
                adapterMeusAnuncios.notifyDataSetChanged();
                 dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {



            }
        });
    }

    private void recuperarDadosUsuario(){

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

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    public void inicializarComponentes(){
        recyclerAnuncios = findViewById(R.id.recyclerAnuncios);

    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    private void abrirConfiguracoes(){
        startActivity(new Intent(MeusAnunciosActivity.this, ConfiguracoesUsuarioActivity.class));
    }




}