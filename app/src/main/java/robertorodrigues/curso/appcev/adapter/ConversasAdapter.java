package robertorodrigues.curso.appcev.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.databinding.FragmentConversasBinding;
import robertorodrigues.curso.appcev.fragment.ConversasFragment;
import robertorodrigues.curso.appcev.model.Anuncio;
import robertorodrigues.curso.appcev.model.Conversa;
import robertorodrigues.curso.appcev.model.Usuario;

public class ConversasAdapter extends RecyclerView.Adapter<ConversasAdapter.MyViewHolder> {

    private List<Conversa> conversas;
    private Context context;

    public ConversasAdapter(List<Conversa> lista, Context c) {
        this.conversas = lista;
        this.context = c;
    }

    public List<Conversa> getConversas(){
        return this.conversas;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contatos, parent, false );
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {



        Conversa conversa = conversas.get( position );


       // if(conversa.getClass().equals(ConversasFragment.class)){

           // Usuario usuario = conversa.getUsuarioExibicao();
            Anuncio anuncio = conversa.getUsuarioExibicaoAnuncio();
            String usuarioNome = conversa.getUsuarioExibicao().getNome();
            String usuarioFoto = conversa.getUsuarioExibicao().getUrlImagem();


            if(usuarioNome != null){
                holder.nome.setText( usuarioNome);
//                holder.tituloProduto.setText(anuncio.getTitulo()); // nao esta exibindo o titulo do anuncio
                holder.ultimaMensagem.setText( conversa.getUltimaMensagem() );
                //configura foto de usuarios na conversa
                if ( usuarioFoto != null ){
                    Uri uri = Uri.parse( usuarioFoto );
                    Picasso.get().load( uri ).into( holder.foto);
                }else {
                    holder.foto.setImageResource(R.drawable.perfil);
                }


                //  condição para verificar novas mensagens
                if(conversa.getNovaMensagem().equals("true")){
                    holder.novaMensagem.setVisibility( View.VISIBLE);

                }else{
                    holder.novaMensagem.setVisibility( View.GONE);

                }

            }

        /* }  else{

            holder.ultimaMensagem.setText( conversa.getUltimaMensagem() );

            Anuncio anuncio = conversa.getUsuarioExibicaoAnuncio();
            Usuario usuario = conversa.getUsuarioExibicao();

            // holder.tituloProduto.setText( anuncio.getTitulo());
            holder.nome.setText(usuario.getNome());
            holder.tituloProduto.setText(anuncio.getTitulo());

            if ( usuario.getUrlImagem()!= null ){
                //Carregar imagem
                Uri uri = Uri.parse(usuario.getUrlImagem());
                Picasso.get().load( uri ).into( holder.foto);

            }else {
                holder.foto.setImageResource(R.drawable.perfil);
            }
        }*/


    }

    @Override
    public int getItemCount() {
        return conversas.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView foto;
        TextView nome, ultimaMensagem, novaMensagem;

        public MyViewHolder(View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.imageViewFotoContatos);
            //tituloProduto = itemView.findViewById(R.id.textNomeProdutoChat);
            ultimaMensagem = itemView.findViewById(R.id.textEmailContato);
            nome = itemView.findViewById(R.id.textNomeVendedorChat);
            novaMensagem = itemView.findViewById(R.id.textQtdNovasMensagens);

        }
    }


}
