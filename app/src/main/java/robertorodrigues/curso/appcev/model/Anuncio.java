package robertorodrigues.curso.appcev.model;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.List;

import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;

public class Anuncio implements Serializable {

    String idAnuncio;
    String idUsuario; // vendedor
    String estado ;
    String categoria;
    String titulo ;
    String valor ;
    String telefone ;
    String descricao ;
    String avaliacao ;
    String nomeVendedor;
    String fotoVendedor;
    String tokenVendedor;
    private Usuario usuarioExibicao;
    private List<String> fotos;

    public Anuncio() {
        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("meus_anuncios");
        setIdAnuncio(anuncioRef.push().getKey());

    }
    public  Anuncio(String idUsuario){

    }

    // criar metodo salvarAvaliacao

    public  void salvarAnuncio(){

        String idUsuario = ConfiguracaoFirebase.getIdUsuario();

        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("meus_anuncios");

        anuncioRef.child(idUsuario)
                .child(getIdAnuncio())
                .setValue(this);
       salvarAnuncioPublico();
    }

    public  void salvarAnuncioPublico(){


        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("anuncios");

        anuncioRef.child(getEstado())
                .child(getCategoria())
                .child(getIdAnuncio())
                .setValue(this);

    }

    public void removerAnuncio(){
        String idUsuario = ConfiguracaoFirebase.getIdUsuario();

        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("meus_anuncios")
                .child(idUsuario)
                .child(getIdAnuncio());
        anuncioRef.removeValue();
        removerMeuAnuncioPublico();
    }

    public void removerMeuAnuncioPublico(){

        DatabaseReference anuncioRef = ConfiguracaoFirebase.getFirebaseDatabase()
                .child("anuncios")
                .child(getEstado())
                .child(getCategoria())
                .child(getIdAnuncio());
        anuncioRef.removeValue();
    }




    public String getIdAnuncio() {
        return idAnuncio;
    }

    public void setIdAnuncio(String idAnuncio) {
        this.idAnuncio = idAnuncio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }

    public String getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(String avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getNomeVendedor() {
        return nomeVendedor;
    }

    public void setNomeVendedor(String nomeVendedor) {
        this.nomeVendedor = nomeVendedor;
    }

    public String getFotoVendedor() {
        return fotoVendedor;
    }

    public void setFotoVendedor(String fotoVendedor) {
        this.fotoVendedor = fotoVendedor;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Usuario getUsuarioExibicao() {
        return usuarioExibicao;
    }

    public void setUsuarioExibicao(Usuario usuarioExibicao) {
        this.usuarioExibicao = usuarioExibicao;
    }

    public String getTokenVendedor() {
        return tokenVendedor;
    }

    public void setTokenVendedor(String tokenVendedor) {
        this.tokenVendedor = tokenVendedor;
    }
}
