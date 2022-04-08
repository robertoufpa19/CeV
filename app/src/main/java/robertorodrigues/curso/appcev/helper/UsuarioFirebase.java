package robertorodrigues.curso.appcev.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import robertorodrigues.curso.appcev.activity.AnunciosActivity;
import robertorodrigues.curso.appcev.activity.EmpresaActivity;
import robertorodrigues.curso.appcev.model.Usuario;

public class UsuarioFirebase {


    public  static  String getIdUsuario(){
        FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return  autenticacao.getCurrentUser().getUid();
    }

    public static FirebaseUser getUsuarioAtual(){
        FirebaseAuth usuario = ConfiguracaoFirebase.getFirebaseAutenticacao();
        return  usuario.getCurrentUser();
    }

    public  static Usuario getDadosUsuarioLogado(){

        FirebaseUser firebaseUser = getUsuarioAtual();
        Usuario usuario = new Usuario();
        usuario.setEmail(firebaseUser.getEmail());
        usuario.setIdUsuario(getIdUsuario());
        usuario.setNome(firebaseUser.getDisplayName());
        usuario.setTokenUsuario(usuario.getTokenUsuario()); // falta recuperar token do usuario logado pra notificar no grupo


        if(firebaseUser.getPhotoUrl() == null){
            usuario.setUrlImagem("");
        }else{
            usuario.setUrlImagem(firebaseUser.getPhotoUrl().toString());

        }

        return  usuario;
    }


    // metodo para atualizar a nome do usuario
    public static boolean atualizarNomeUsuario(String nome){

        try {

            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nome)
                    .build();

            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        Log.d("Perfil", "Erro ao atualizar nome de Perfil! ");
                    }
                }
            });
            return  true;

        }catch (Exception e){
            e.printStackTrace();
            return  false;
        }


    }

    // metodo para atualizar a foto do usuario
    public static boolean atualizarFotoUsuario(Uri url){

        try {

            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(url)
                    .build();

            user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(!task.isSuccessful()){
                        Log.d("Perfil", "Erro ao atualizar foto de perfil! ");
                    }
                }
            });
            return  true;

        }catch (Exception e){
            e.printStackTrace();
            return  false;
        }


    }


    public  static  boolean atualizarTipoUsuario(String tipo){

        try {
            FirebaseUser user = getUsuarioAtual();
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(tipo)
                    .build();
            user.updateProfile(profile);
            return  true;

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }



    public static void redirecionaUsuarioLogado(final Activity activity){

        FirebaseUser user = getUsuarioAtual();
        if(user != null ){
            Log.d("resultado", "onDataChange: " + getIdUsuario());
            DatabaseReference usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase()
                    .child("usuarios")
                    .child( getIdUsuario() );
            usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("resultado", "onDataChange: " + dataSnapshot.toString() );
                    Usuario usuario = dataSnapshot.getValue( Usuario.class );

                    String tipoUsuario = usuario.getTipo();
                    if( tipoUsuario.equals("empresa") ){
                        Intent i = new Intent(activity, EmpresaActivity.class);
                        activity.startActivity(i);
                    }else {
                        Intent i = new Intent(activity, AnunciosActivity.class);
                        activity.startActivity(i);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{

        }

    }


}
