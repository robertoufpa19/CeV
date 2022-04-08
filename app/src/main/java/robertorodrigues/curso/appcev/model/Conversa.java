package robertorodrigues.curso.appcev.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;

import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;

public class Conversa implements Serializable {

    private String idRemetente;
    private String idDestinatario;;
    private String ultimaMensagem;

    private Usuario usuarioExibicao;

    private Anuncio usuarioExibicaoAnuncio;


    private String novaMensagem;



    public Conversa() {

    }


    public void salvar(){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference conversaRef = database.child("conversas");

        conversaRef.child( this.getIdRemetente() )
                .child( this.getIdDestinatario() )
                .setValue( this );

    }

    public String getIdRemetente() {
        return idRemetente;
    }

    public void setIdRemetente(String idRemetente) {
        this.idRemetente = idRemetente;
    }

    public String getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(String idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public Usuario getUsuarioExibicao() {
        return usuarioExibicao;
    }

    public void setUsuarioExibicao(Usuario usuarioExibicao) {
        this.usuarioExibicao = usuarioExibicao;
    }

    public Anuncio getUsuarioExibicaoAnuncio() {
        return usuarioExibicaoAnuncio;
    }

    public void setUsuarioExibicaoAnuncio(Anuncio usuarioExibicaoAnuncio) {
        this.usuarioExibicaoAnuncio = usuarioExibicaoAnuncio;
    }

    public String getNovaMensagem() {
        return novaMensagem;
    }

    public void setNovaMensagem(String novaMensagem) {
        this.novaMensagem = novaMensagem;
    }
}
