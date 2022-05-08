package robertorodrigues.curso.appcev.fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.activity.ChatActivity;
import robertorodrigues.curso.appcev.adapter.ConversasAdapter;
import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;
import robertorodrigues.curso.appcev.helper.RecyclerItemClickListener;
import robertorodrigues.curso.appcev.helper.UsuarioFirebase;
import robertorodrigues.curso.appcev.model.Anuncio;
import robertorodrigues.curso.appcev.model.Conversa;
import robertorodrigues.curso.appcev.model.Usuario;

/**
 * A simple {@link Fragment} subclass.

 */
public class ConversasFragment extends Fragment {

    private RecyclerView recyclerViewConversas; // pedidos
    private List<Conversa> listaConversas = new ArrayList<>(); //lista de pedidos
    //private List<Pedido> listaPedidos = new ArrayList<>();
    private ConversasAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;

    private String idUsuarioLogado;

    public ConversasFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        recyclerViewConversas = view.findViewById(R.id.recyclerListaConversas); // lista de conversas

        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //Configurar adapter
        adapter = new ConversasAdapter(listaConversas, getActivity());

        //Configurar recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewConversas.setLayoutManager( layoutManager );
        recyclerViewConversas.setHasFixedSize(true);
        recyclerViewConversas.setAdapter( adapter );

        //Configurar evento de clique nas conversas
        recyclerViewConversas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewConversas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                                List<Conversa> listaConversasAtualizada = adapter.getConversas();
                                Conversa conversaSelecionada = listaConversasAtualizada .get(position); // seleciona conversa que foi buscada de forma correta

                                // enviar informacoes de uma activity para outra
                               Usuario usuario = new Usuario();

                                String idUsuario = conversaSelecionada.getUsuarioExibicao().getIdUsuario();
                                String nomeUsuario = conversaSelecionada.getUsuarioExibicao().getNome();
                                String fotoUsuario = conversaSelecionada.getUsuarioExibicao().getUrlImagem();
                                String token = conversaSelecionada.getUsuarioExibicaoAnuncio().getUsuarioExibicao().getTokenUsuario();


                            /* Recebendo os valores no objeto para enviar para
                               a próxima tela através do método putExtra() */

                                usuario.setIdUsuario(idUsuario);
                                usuario.setNome(nomeUsuario);
                                usuario.setUrlImagem(fotoUsuario);
                                usuario.setTokenUsuario(token);

                                Intent i = new Intent(getActivity(), ChatActivity.class);
                              //  i.putExtra("chat",  conversaSelecionada.getUsuarioExibicao()); // usuario exibicao
                                i.putExtra("chat",  usuario); // usuario exibicao
                                startActivity( i );

                                // mensagem visualizada e remove a notificacão de nova mensagem
                                conversaSelecionada.setNovaMensagem("false");
                                conversaSelecionada.salvar();

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


        //Configura conversas ref
        String identificadorUsuario = UsuarioFirebase.getIdUsuario();
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        conversasRef = database.child("conversas")
                .child( identificadorUsuario );

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener( childEventListenerConversas);
    }

    public void recuperarConversas(){

        listaConversas.clear();

        childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                // Recuperar conversas
                Conversa conversa = dataSnapshot.getValue( Conversa.class );
                listaConversas.add( conversa );
                adapter.notifyDataSetChanged();


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


}