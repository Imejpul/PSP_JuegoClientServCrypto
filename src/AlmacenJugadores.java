import java.util.ArrayList;
import java.util.List;

public class AlmacenJugadores {

    public List<DatosJugador> jugadores = new ArrayList<>();

    public synchronized void almacenarJugador(DatosJugador jugador) {
        jugadores.add(jugador);
        System.out.println("Jugador almacenado");
    }

    public synchronized boolean comprobarCredencialesJugador(DatosJugador jugador) {
        for (DatosJugador j : jugadores) {
            if (jugador.getNickName().equalsIgnoreCase(j.getNickName()) && jugador.getPwd().equals(j.getPwd())) {
                System.out.println("Jugador comprobado");
                return true;
            }
        }
        System.out.println("¡Jugador no encontrado en AlmacenJugadores!");
        return false;
    }


}
