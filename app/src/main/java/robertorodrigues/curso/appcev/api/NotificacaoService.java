package robertorodrigues.curso.appcev.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import robertorodrigues.curso.appcev.model.NotificacaoDados;

public interface NotificacaoService {

    @Headers({
            "Authorization:key= AAAABe3BKCw:APA91bHymI_hL87n9kasoS8LuA_IIMwwFLUM0QBT4JKMyxMloxNfdRf8k61N1j9FDYWerhU6xyoy9q56kthJlkSyjlSnCYtqLKoadlzcvhJyHIdEZFbMDmS6DVGq1Mthw9iyQQT49EuX",
            "Content-Type:application/json"
    })
    @POST("send")
    Call<NotificacaoDados> salvarNotificacao(@Body NotificacaoDados notificacaoDados);
}
