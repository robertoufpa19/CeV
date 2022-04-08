package robertorodrigues.curso.appcev.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ConfiguracaoFirebase {

    private static DatabaseReference referenciaFirebase;
    private static FirebaseAuth referenciaAutenticacao;
    private static StorageReference referenciaStorage;


     public static String getIdUsuario(){

         FirebaseAuth autenticacao = getFirebaseAutenticacao();
         return autenticacao.getCurrentUser().getUid();
     }

    // retorna a instancia do  FirebaseDatabase

    public  static DatabaseReference getFirebaseDatabase(){
        if(referenciaFirebase == null){
            referenciaFirebase = FirebaseDatabase.getInstance().getReference();
        }
        return  referenciaFirebase;
    }
    // retorna a instancia do  FirebaseAuth

    public  static FirebaseAuth getFirebaseAutenticacao(){
        if(referenciaAutenticacao == null){
            referenciaAutenticacao = FirebaseAuth.getInstance();
        }
        return  referenciaAutenticacao;
    }
    // retorna a instancia do  FirebaseStorage
    public  static StorageReference getFirebaseStorage(){

        if(referenciaStorage == null){
            referenciaStorage = FirebaseStorage.getInstance().getReference();
        }

        return  referenciaStorage;


    }

}

