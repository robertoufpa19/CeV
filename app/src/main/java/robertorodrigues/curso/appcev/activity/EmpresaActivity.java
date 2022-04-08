package robertorodrigues.curso.appcev.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import dmax.dialog.SpotsDialog;
import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;
import robertorodrigues.curso.appcev.helper.UsuarioFirebase;
import robertorodrigues.curso.appcev.model.Usuario;

public class EmpresaActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;

    private Usuario empresa;
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private String idEmpresaLogado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empresa);

        // configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Nome da Empresa");
        setSupportActionBar(toolbar);

        empresa = UsuarioFirebase.getDadosUsuarioLogado();

        // configurar nome do usuario na toolbra
        getSupportActionBar().setTitle(empresa.getNome());

        //configuracoes iniciais
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();

        // criar condição para verificar se usuario esta logado
        if(autenticacao.getCurrentUser() != null) { //logado
            idEmpresaLogado = UsuarioFirebase.getIdUsuario();
              recuperarDadosEmpresa();

            configuraBotaoNavegacao();

            }else{
            exibirMensagem("Você precisa está logado para utilizar o App");
            abrirLogin();
            // nao recupera os anuncios se nao estiver logado
            //  recuperarAnunciosPublicos();
        }




    }


    private void recuperarDadosEmpresa(){
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference usuarioRef = firebaseRef
                .child("empresas")
                .child(idEmpresaLogado);
        // recupera dados uma unica vez
        usuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    empresa = snapshot.getValue(Usuario.class);
                }

                if(empresa.getUrlImagem() != ""){

                }else{
                    exibirMensagem("Configure seu Perfil");
                    abrirConfiguracoes();
                }

                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    // metodo responsavel por criar a BottonNavigation
    private void configuraBotaoNavegacao(){
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.botaoNavegacaoEmpresa);

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
                    case R.id.ic_inicio_empresa:
                        //entra na pagina de cadastro
                        startActivity(new Intent(getApplicationContext(), EmpresaActivity.class));
                        break;
                    case R.id.ic_pedidos_cliente:

                     //   startActivity(new Intent(getApplicationContext(), LojasActivity.class));

                        break;
                    case R.id.ic_adicionar_produto:
                        //entra na pagina de meus anuncio
                        startActivity(new Intent(getApplicationContext(), MeusAnunciosActivity.class));
                        exibirMensagem("Adicionar Anuncios!");
                        break;
                    case R.id.ic_empresa_perfil:
                        //entra na pagina de perfil configuracoes
                        startActivity(new Intent(getApplicationContext(), ConfiguracoesEmpresaActivity.class));
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

                AlertDialog.Builder builder = new AlertDialog.Builder(EmpresaActivity.this);
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

    private void abrirLogin(){
        startActivity(new Intent(EmpresaActivity.this, LoginActivity.class));
    }

    private void abrirHome(){
        startActivity(new Intent(EmpresaActivity.this, EmpresaActivity.class));
    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

    private void abrirConfiguracoes(){
        startActivity(new Intent(EmpresaActivity.this, ConfiguracoesUsuarioActivity.class));
    }

}