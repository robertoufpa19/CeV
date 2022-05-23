package robertorodrigues.curso.appcev.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dmax.dialog.SpotsDialog;
import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.adapter.AdapterAnuncios;
import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;
import robertorodrigues.curso.appcev.helper.RecyclerItemClickListener;
import robertorodrigues.curso.appcev.helper.UsuarioFirebase;
import robertorodrigues.curso.appcev.model.Anuncio;
import robertorodrigues.curso.appcev.model.Usuario;

public class AnunciosActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerAnunciosPublicos;
    private Button buttonRegiao, buttonCategoria;
    private AdapterAnuncios adapterAnuncios;
    private List<Anuncio> listaAnuncios = new ArrayList<>();
    private DatabaseReference anunciosPublicosRef;
    private AlertDialog dialog;
    private String filtroEstado = "";
    private String filtroCategoria = "";
    private boolean filtrandoPorEstado = false;

    private Usuario usuario;

    private String idUsuarioLogado;
    private DatabaseReference firebaseRef;

    private MaterialSearchView searchViewPesquisaProduto;

     // id bloco de anuncios:  ca-app-pub-9224110911314505/2559694128



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncios);

        // configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("CeV");
        setSupportActionBar(toolbar);


        //configuracoes iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("anuncios");

        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();



        // criar condição para verificar se usuario esta logado
        if(autenticacao.getCurrentUser() != null) { //logado
            idUsuarioLogado = UsuarioFirebase.getIdUsuario();
            recuperarDadosUsuario();



            // configurar botao de navegacao
            configuraBotaoNavegacao();
        }else{
            exibirMensagem("Você precisa está logado para utilizar o App");
            abrirLogin();
            // nao recupera os anuncios se nao estiver logado
          //  recuperarAnunciosPublicos();
        }




        inicializarComponentes();
        

        // configurar recyclerView
        recyclerAnunciosPublicos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnunciosPublicos.setHasFixedSize(true);
        adapterAnuncios = new AdapterAnuncios(listaAnuncios, this);
        recyclerAnunciosPublicos.setAdapter(adapterAnuncios);


        //Listener para o search view
        searchViewPesquisaProduto.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {


            }
        });
        // configurar searchviewPesquisa

        //Listener para caixa de texto
        searchViewPesquisaProduto.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                String textoDigitado = newText.toLowerCase(); // buscar o anuncio independente que seja com letra Maiscula ou minuscula

                buscarAnuncios(textoDigitado);

                return true;
            }
        });


        //evento de clique
        recyclerAnunciosPublicos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerAnunciosPublicos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                // criar condição para verificar se usuario esta logado
                                if(autenticacao.getCurrentUser() != null){ //logado

                                    //condicao para verificar se o usuario configurou seus dados(perfil)
                                    if(usuario.getUrlImagem() != null ){ //teste

                                        List<Anuncio> listaAnunciosAtualizado = adapterAnuncios.getAnuncios();

                                        Anuncio anuncioSelecionado = listaAnunciosAtualizado.get(position); // seleciona anuncio que foi buscado de forma correta seja na pesquisa ou não
                                        Intent i = new Intent(AnunciosActivity.this, DetalhesProdutoActivity.class);
                                        i.putExtra("anuncioSelecionado", anuncioSelecionado);
                                        startActivity(i);

                                    }else{
                                        exibirMensagem("Configure seu Perfil, antes de ver produtos");
                                        abrirConfiguracoes();
                                    }



                                }else{ //deslogado
                                   exibirMensagem("Você precisa está logado para fazer um pedido!");

                                }



                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

       /* if(idUsuarioLogado != null){
            recuperarDadosUsuario();
        } */

    }

                // botao Regiao
    public void filtrarPorEstado(View view){

          // abilitar filtros se o usuario estiver logado
        if(autenticacao.getCurrentUser() != null) { //logado

            AlertDialog.Builder dialogEstado = new AlertDialog.Builder(this);
            dialogEstado.setTitle("Selecione a região desejado");


            // configurar spinner de estado
            View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
            // configuracoes iniciais
            Spinner spinnerEstado = viewSpinner.findViewById(R.id.spinnerFiltro);

            // spinner estado
            String[] estados = getResources().getStringArray(R.array.regiao);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item,
                    estados
            );

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerEstado.setAdapter(adapter);
            dialogEstado.setView(viewSpinner);


            dialogEstado.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    filtroEstado = spinnerEstado.getSelectedItem().toString();
                    recuperarAnunciosPorEstados();
                    filtrandoPorEstado = true;

                }
            });

            dialogEstado.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = dialogEstado.create();
            dialog.show();
        }



    }

    // botao categoria
    public void filtrarPorCategoria(View view){


        // abilitar filtros se o usuario estiver logado
        if(autenticacao.getCurrentUser() != null) { //logado


            if(filtrandoPorEstado == true){

                AlertDialog.Builder dialogCategoria = new AlertDialog.Builder(this);
                dialogCategoria.setTitle("Selecione a categoria desejado");


                // configurar spinner de categoria
                View viewSpinner = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
                // configuracoes iniciais
                Spinner spinnerCategoria = viewSpinner.findViewById(R.id.spinnerFiltro);

                // spinner categoria
                String[] categoria = getResources().getStringArray(R.array.categorias);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        this, android.R.layout.simple_spinner_item,
                        categoria
                );

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategoria.setAdapter(adapter);
                dialogCategoria.setView(viewSpinner);


                dialogCategoria.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        filtroCategoria = spinnerCategoria.getSelectedItem().toString();
                        recuperarAnunciosPorCategoria();

                    }
                });

                dialogCategoria.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                AlertDialog dialog = dialogCategoria.create();
                dialog.show();

            }else{
                exibirMensagem("Escolha primeiro uma Região");
            }


        }



    }

    public void limparFiltro(View view){
        // abilitar filtros se o usuario estiver logado
        if(autenticacao.getCurrentUser() != null) { //logado
            abrirSlide();
        }

    }

    public void recuperarAnunciosPorEstados(){
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("anuncios")
                .child(filtroEstado); // recupera o texto(sigla do estado) selecionado pelo usuario


        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anuncios Por Estado!")
                .setCancelable(false)
                .build();
        dialog.show();

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listaAnuncios.clear();

                for(DataSnapshot categorias: snapshot.getChildren()){ // pecorre cada categoria de um estado
                    for(DataSnapshot anuncios: categorias.getChildren()){ // percorre anuncios(id) de cada categoria
                        Anuncio anuncio = anuncios.getValue(Anuncio.class);
                        listaAnuncios.add(anuncio);

                    }
                }

                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void recuperarAnunciosPorCategoria(){
        anunciosPublicosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("anuncios")
                .child(filtroEstado)
                .child(filtroCategoria); // recupera o texto(categoria) selecionado pelo usuario


        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anuncios Por Categoria!")
                .setCancelable(false)
                .build();
        dialog.show();

        anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listaAnuncios.clear();

                for(DataSnapshot anuncios: snapshot.getChildren()){ // pecorre cada categoria de um estado
                    Anuncio anuncio = anuncios.getValue(Anuncio.class);
                    listaAnuncios.add(anuncio);

                }

                Collections.reverse(listaAnuncios);
                adapterAnuncios.notifyDataSetChanged();
                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    public void recuperarAnunciosPublicos(){

      /*  dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Recuperando Anuncios!")
                .setCancelable(false)
                .build();
        dialog.show(); */

           anunciosPublicosRef.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {

                   listaAnuncios.clear();

                   if(snapshot.getValue() != null){ // se tiver anuncios

                       for(DataSnapshot estados: snapshot.getChildren()){ // percorre cada no de estados e seus filhos(categoria e anuncios)
                           for(DataSnapshot categorias: estados.getChildren()){ // pecorre cada categoria de um estado
                               for(DataSnapshot anuncios: categorias.getChildren()){ // percorre anuncios(id) de cada categoria
                                   Anuncio anuncio = anuncios.getValue(Anuncio.class);
                                   listaAnuncios.add(anuncio);

                               }
                           }
                       }
                       Collections.reverse(listaAnuncios); // exibicao reversa dos anuncios
                       adapterAnuncios.notifyDataSetChanged();
                     //  dialog.dismiss();

                   }else if(snapshot.getValue() == null){ // senao tiver anuncios
                       exibirMensagem("Você não tem anuncios!");


                   }





               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });
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

                if(snapshot.exists()){

                        usuario = snapshot.getValue(Usuario.class);

                    recuperarAnunciosPublicos();
                    dialog.dismiss();
                }else{
                    abrirConfiguracoes();
                    exibirMensagem("Configure seu perfil");
                    dialog.dismiss();

                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // metodo responsavel por criar a BottonNavigation
    private void configuraBotaoNavegacao(){
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.botaoNavegacao);

        //habilitar navegacao
        habilitarNavegacao(bottomNavigationViewEx);

        // configurar item selecionado inicialmente
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(0); // seleciona o primeiro menu(Inicio)
        menuItem.setChecked(true);


    }
    // tratar evento de clique no bottom navigation view
    private void habilitarNavegacao(BottomNavigationViewEx viewEx){

        viewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


                switch (item.getItemId()){
                    case R.id.ic_inicio:
                        //entra na pagina de cadastro
                        startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));
                        break;
                    case R.id.ic_empresas:
                        //empresas
                        //entra na pagina de empresas CadastradasS
                        startActivity(new Intent(getApplicationContext(), LojasActivity.class));


                        break;
                    case R.id.ic_postagem:
                        //entra na pagina de meus anuncio
                        startActivity(new Intent(getApplicationContext(), MeusAnunciosActivity.class));
                        exibirMensagem("Adicionar Anuncios!");
                        break;
                    case R.id.ic_perfil:
                        //entra na pagina de perfil configuracoes
                        startActivity(new Intent(getApplicationContext(), ConfiguracoesUsuarioActivity.class));
                        break;
                }

                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // configura menu de pesquisa
        MenuItem item = menu.findItem(R.id.menuPesquisa);
        searchViewPesquisaProduto.setMenuItem(item);

        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // condicao para verificar se o usuario estiver logado
        //se estiver, entao esconder o menu de 3 pontinhos
        // senao manter o menu de 3 pontinhos e esconder o menu Cadastrar/Entrar

        if(autenticacao.getCurrentUser() == null){ //usuario deslogado
            menu.setGroupVisible(R.id.group_deslogado, true);
        }else{ //usuario logado
            menu.setGroupVisible(R.id.group_logado, true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_cadastro:
                //entra na pagina de cadastro
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
            case R.id.menu_sair:

                AlertDialog.Builder builder = new AlertDialog.Builder(AnunciosActivity.this);
                builder.setTitle("Sair");
                builder.setMessage("Tem certeza que deseja sair?");

                builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            autenticacao.signOut();
                            finish();

                        }catch (Exception  e){
                            e.printStackTrace();
                        }

                        invalidateOptionsMenu(); // invalidar os menus de 3 pontinhos ao deslogar usuario
                        finish();
                        abrirLogin();
                    }
                });
                builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();


                break;


            case R.id.menu_conversas:

                startActivity(new Intent(getApplicationContext(), ConversasActivity.class));

                break;

            case R.id.menu_sobre:
                startActivity(new Intent(getApplicationContext(), SobreActivity.class));
                break;



        }

        return super.onOptionsItemSelected(item);


    }


    public void buscarAnuncios(String texto){
        //Log.d("pesquisa",  texto );


        List<Anuncio> listaAnunciosBusca = new ArrayList<>();

        for ( Anuncio anuncio : listaAnuncios ){

            if(anuncio.getTitulo() != null){

                String nome = anuncio.getTitulo().toLowerCase(); // buscar o anuncio independente que seja com letra Maiscula ou minuscula

                if( nome.contains( texto )  ){

                    listaAnunciosBusca.add( anuncio );

                }

            }


        }

        adapterAnuncios = new AdapterAnuncios(listaAnunciosBusca, this);
        recyclerAnunciosPublicos.setAdapter(adapterAnuncios);
        adapterAnuncios.notifyDataSetChanged();

        //int total = listaAnunciosBusca.size();
        //exibirMensagem(" total anuncios: " + total);

    }


    private void inicializarComponentes(){
         buttonRegiao = findViewById(R.id.buttonRegiao);
         buttonCategoria = findViewById(R.id.buttonCategoria);
         recyclerAnunciosPublicos = findViewById(R.id.recyclerAnunciosPublicos);

         searchViewPesquisaProduto = findViewById(R.id.materialSearchPrincipal);


    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    private void abrirConfiguracoes(){
        startActivity(new Intent(AnunciosActivity.this, ConfiguracoesUsuarioActivity.class));
    }
    private void abrirLogin(){
        startActivity(new Intent(AnunciosActivity.this, LoginActivity.class));
    }





    private void abrirSlide(){
        finish();
        startActivity(new Intent(AnunciosActivity.this, SplashActivity.class));

    }


    /// finaliza no botão voltar do celular

    @Override
    public void finish() {
        finishAffinity();
        super.finish();
    }

}