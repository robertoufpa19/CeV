package robertorodrigues.curso.appcev.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.model.Anuncio;
import robertorodrigues.curso.appcev.model.Avaliacao;

public class AdapterAnuncios extends RecyclerView.Adapter<AdapterAnuncios.MyViewHolder> {

   private List<Anuncio> anuncios;
   private Context context;


    public AdapterAnuncios(List<Anuncio> anuncios, Context context) {
        this.anuncios = anuncios;
        this.context = context;

    }

    public List<Anuncio> getAnuncios(){
        return this.anuncios;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_anuncio, parent, false);

        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

         Anuncio anuncio = anuncios.get(position);
         holder.titulo.setText(anuncio.getTitulo());
         holder.valor.setText(anuncio.getValor());
         holder.descricao.setText(anuncio.getDescricao());
         holder.nomeVendedor.setText(anuncio.getNomeVendedor());

         //verificar se existe avaliações
         if(anuncio.getAvaliacao() != null){
             holder.avaliacao.setText(anuncio.getAvaliacao());
         }else{
             holder.avaliacao.setText("0");
         }

         // pegar a primeira imagem da lista
        List<String> urlFotos = anuncio.getFotos();
        String urlCapa = urlFotos.get(0);// pegar a primeira imagem da lista
        Picasso.get().load(urlCapa).into(holder.foto);

        //configurar foto do vendedor
        String  urlFotoUsuario = anuncio.getFotoVendedor();
        Picasso.get().load(urlFotoUsuario).into(holder.fotoVendedor);

    }

    @Override
    public int getItemCount() {
        return anuncios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView titulo;
        TextView valor;
        ImageView foto;
        TextView descricao;
        TextView avaliacao;
        TextView nomeVendedor;
        CircleImageView fotoVendedor;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            titulo = itemView.findViewById(R.id.textTitulo);
            valor  = itemView.findViewById(R.id.textPreco);
            foto = itemView.findViewById(R.id.imageAnuncio);
            descricao = itemView.findViewById(R.id.textDescricao);
            avaliacao = itemView.findViewById(R.id.textAvaliacoes);
            nomeVendedor = itemView.findViewById(R.id.textNomeUsuarioAnuncio);
            fotoVendedor = itemView.findViewById(R.id.imagePerfilUsuarioAnuncio);

        }
    }

}
