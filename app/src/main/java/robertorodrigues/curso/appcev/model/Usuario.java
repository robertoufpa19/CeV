package robertorodrigues.curso.appcev.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import robertorodrigues.curso.appcev.helper.ConfiguracaoFirebase;
import robertorodrigues.curso.appcev.helper.UsuarioFirebase;

public class Usuario  implements Serializable {

    private String idUsuario;
    private String nome;
    private String senhaUsuario;
    private String cidade ;
    private String bairro ;
    private String rua ;
    private String numero ;
    private String telefone;
    private String urlImagem;
    private String tokenUsuario;
    private String tipo; // empresa ou usuario padrão


    private String email;

    public Usuario() {

    }

    public  void salvarUsuario(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference usuarioRef = firebaseRef.child("usuarios")
                .child(getIdUsuario());
        usuarioRef.setValue(this);

    }



    public void atualizar(){

        String identificadorUsuario = UsuarioFirebase.getIdUsuario();
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference usuariosRef = database.child("usuarios")
                .child(identificadorUsuario);
       Map<String, Object> valoresUsuario =  converterParaMap();
        usuariosRef.updateChildren(valoresUsuario);


    }


    public  void salvarEmpresa(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference usuarioRef = firebaseRef.child("empresas")
                .child(getIdUsuario());
        usuarioRef.setValue(this);
    }

    public void atualizarEmpresa(){

        String identificadorUsuario = UsuarioFirebase.getIdUsuario();
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference usuariosRef = database.child("empresas")
                .child(identificadorUsuario);
        Map<String, Object> valoresUsuario =  converterParaMap();
        usuariosRef.updateChildren(valoresUsuario);
    }

    @Exclude
    public Map<String, Object> converterParaMap(){
        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("nome", getNome());
        usuarioMap.put("urlImagem", getUrlImagem());
        usuarioMap.put("cidade", getCidade());
        usuarioMap.put("bairro", getBairro());
        usuarioMap.put("rua", getRua());
        usuarioMap.put("numero", getNumero());
        usuarioMap.put("telefone", getTelefone());
        usuarioMap.put("email", getEmail());
        usuarioMap.put("tokenUsuario", getTokenUsuario());

        return  usuarioMap;

    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public String getTokenUsuario() {
        return tokenUsuario;
    }

    public void setTokenUsuario(String tokenUsuario) {
        this.tokenUsuario = tokenUsuario;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    @Exclude
    public String getSenhaUsuario() {
        return senhaUsuario;
    }

    public void setSenhaUsuario(String senhaUsuario) {
        this.senhaUsuario = senhaUsuario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
