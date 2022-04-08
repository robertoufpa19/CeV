package robertorodrigues.curso.appcev.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.ImageListener;
import com.willy.ratingbar.BaseRatingBar;
import com.willy.ratingbar.ScaleRatingBar;

import java.util.List;

import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.model.Anuncio;
import robertorodrigues.curso.appcev.model.Avaliacao;
import robertorodrigues.curso.appcev.model.Usuario;

public class AvaliacaoActivity extends AppCompatActivity {


    private ScaleRatingBar barraClassificacaoEscala;
    private TextView textoResultado, nomeProduto;
    private ImageView fotoProduto;
    private Button buttonAvaliar;

    private Anuncio anuncioSelecionado;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avaliacao);


        addOuvinteBarraClassificacao();
        addOuvinteBotao();

        nomeProduto = findViewById(R.id.nomeProdutoAvaliacao);
        fotoProduto = findViewById(R.id.imageProdutoAvaliacao);


        // recuperar anuncio para a exibicao
        anuncioSelecionado = (Anuncio) getIntent().getSerializableExtra("anuncioSelecionado");
        nomeProduto.setText(anuncioSelecionado.getTitulo());

        // pegar a primeira imagem da lista
        List<String> urlFotos = anuncioSelecionado.getFotos();
        String urlCapa = urlFotos.get(0);// pegar a primeira imagem da lista
        Picasso.get().load(urlCapa).into(fotoProduto);


    }

    public void addOuvinteBarraClassificacao(){
        barraClassificacaoEscala = findViewById(R.id.simpleRatingBar);
        textoResultado = findViewById(R.id.textoResultado);

        barraClassificacaoEscala.setOnRatingChangeListener(new BaseRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChange(BaseRatingBar ratingBar, float avaliacao) {
                textoResultado.setText(String.valueOf(avaliacao));
            }
        });
    }


    public  void addOuvinteBotao(){
        barraClassificacaoEscala = findViewById(R.id.simpleRatingBar);
        buttonAvaliar = findViewById(R.id.buttonAvaliar);


        // se o botao for clicado, exiba o valor de avaliação
        buttonAvaliar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              /*  Toast.makeText(MainActivity.this,
                        String.valueOf(scaleRatingBar.getRating()),
                        Toast.LENGTH_SHORT).show();*/

                AlertDialog.Builder builder = new AlertDialog.Builder(AvaliacaoActivity.this)
                        .setTitle("Avaliação")
                        .setMessage(" " + barraClassificacaoEscala.getRating())
                        .setCancelable(false)
                        .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                // armazenar dados da avaliação no firebase
                                  Avaliacao avaliacao = new Avaliacao();
                                  avaliacao.setIdAnuncio(avaliacao.getIdAnuncio());
                                  avaliacao.setQtdEstrelas(" " + barraClassificacaoEscala.getRating() );

                                  // ja esta salvando a avaliação do anuncio
                                   anuncioSelecionado.setAvaliacao(" " + barraClassificacaoEscala.getRating() );
                                   anuncioSelecionado.salvarAnuncioPublico(); // criar o atributo avaliacao


                                   avaliacao.salvarAvaliacao();

                                finish(); // finalizar a activity
                                exibirMensagem("Avaliação Realizada Com Sucesso!");


                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });



    }

    private void exibirMensagem(String texto){
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }

}