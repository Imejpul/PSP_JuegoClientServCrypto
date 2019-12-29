import java.util.Objects;

public class DatosJugador {

    private String nombre;
    private String apellido;
    private int edad;
    private String nickName;
    private String pwd;

    DatosJugador(String nombre, String apellido, int edad, String nickName, String pwd) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.nickName = nickName;
        this.pwd = pwd;
    }

    DatosJugador() {
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DatosJugador)) return false;
        DatosJugador that = (DatosJugador) o;
        return getNickName().equals(that.getNickName()) &&
                getPwd().equals(that.getPwd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNickName(), getPwd());
    }
}
