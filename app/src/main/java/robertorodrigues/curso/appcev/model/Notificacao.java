package robertorodrigues.curso.appcev.model;

public class Notificacao {

    private String title; //titulo
    private String body; // corpo

    public Notificacao(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
