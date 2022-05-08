package robertorodrigues.curso.appcev.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import robertorodrigues.curso.appcev.R;
import robertorodrigues.curso.appcev.activity.AnunciosActivity;
import robertorodrigues.curso.appcev.activity.ChatActivity;
import robertorodrigues.curso.appcev.activity.ConversasActivity;
import robertorodrigues.curso.appcev.fragment.ConversasFragment;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage notificacao) {
        if( notificacao.getNotification() != null ){

            String titulo = notificacao.getNotification().getTitle();
            String corpo = notificacao.getNotification().getBody();


            if(corpo.equals("Nova Mensagem de ")){
                enviarNotificacao(titulo, corpo);
            }

            if(corpo.equals("Mensagem de ")){
                enviarNotificacaoAbrirChat(titulo, corpo);
            }

            //Log.i("Notificacao", "recebida titulo: " + titulo + " corpo: " + corpo );

        }
    }
    private void enviarNotificacao(String titulo, String corpo){



        //Configuração para notificação
        String canal = getString(R.string.default_notification_channel_id);
        Uri uriSom = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );
        Intent intent = new Intent(this, ConversasActivity.class); /// atividade de conversas
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // auxiliar a não cair na atividade padrão do app
        // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //ver qual atividade está sendo aberta
        Log.i("atividade aberta ", "activity" + intent + " corpo: " + corpo );

        //Criar notificação
        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(this, canal)
                .setContentTitle( titulo )
                .setContentText( corpo )
                .setSmallIcon( R.drawable.ic_camera_black_24dp)
                .setSound( uriSom )
                .setAutoCancel( true )
                .setContentIntent( pendingIntent );

        //Recupera notificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Verifica versão do Android a partir do Oreo para configurar canal de notificação
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            NotificationChannel channel = new NotificationChannel(canal, "canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel( channel );
        }

        //Envia notificação
        notificationManager.notify(0, notificacao.build() );

    }


    private void enviarNotificacaoAbrirChat(String titulo, String corpo){

        //Configuração para notificação
        String canal = getString(R.string.default_notification_channel_id);
        Uri uriSom = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );
        Intent intent = new Intent(this, ChatActivity.class); /// atividade de pedidos
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // auxiliar a não cair na atividade padrão do app
        //Intent intent = new Intent(this, PedidosActivity.class); /// atividade de pedidos
        // PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Criar notificação
        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(this, canal)
                .setContentTitle( titulo )
                .setContentText( corpo )
                .setSmallIcon(R.drawable.ic_camera_black_24dp)
                .setSound( uriSom )
                .setAutoCancel( true )
                .setContentIntent( pendingIntent );

        //Recupera notificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Verifica versão do Android a partir do Oreo para configurar canal de notificação
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            NotificationChannel channel = new NotificationChannel(canal, "canal", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel( channel );
        }

        //Envia notificação
        notificationManager.notify(0, notificacao.build() );

    }

    @Override
    public void onNewToken(@NonNull String s) {
        Log.d("Token", " token atualizado: " + s);
        sendRegistrationToServer(s); // s = token
    }

    //Implemente este método para enviar token para seu servidor de aplicativo.
    public void sendRegistrationToServer(String token){


    }
}
