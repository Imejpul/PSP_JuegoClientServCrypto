public class Quest {

    private String pregunta;
    private int respuesta;

    Quest(String pregunta, int respuesta) {
        this.pregunta = pregunta;
        this.respuesta = respuesta;
    }

    public String getPregunta() {
        return pregunta;
    }

    public int getRespuesta() {
        return respuesta;
    }


}
