package robertorodrigues.curso.appcev.helper;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

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





}
