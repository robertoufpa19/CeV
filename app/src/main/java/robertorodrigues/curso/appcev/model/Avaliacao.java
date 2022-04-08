package robertorodrigues.curso.appcev.model;


import com.google.firebase.database.DatabaseReference;

import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;

public class Avaliacao {

    String idUsuario;
    String idAnuncio;
    String titulo ;
    String qtdEstrelas;



    public  void salvarAvaliacao(){


        String idUsuario = ConfiguracaoFirebase.getIdUsuario();

        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("avaliacoes");

        anuncioRef.child(idUsuario)
                .child(getIdAnuncio())
                .setValue(this);

    }


    public Avaliacao() {

        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("avaliacoes");
        setIdAnuncio(anuncioRef.push().getKey());
    }

    public String getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(String idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getQtdEstrelas() {
        return qtdEstrelas;
    }

    public void setQtdEstrelas(String qtdEstrelas) {
        this.qtdEstrelas = qtdEstrelas;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }
}
