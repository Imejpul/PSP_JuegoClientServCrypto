import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Servidor {

    //gestionar entrada de jugadores en hilo
    public static void main(String[] args) throws IOException {
        ServerSocket s;
        Socket c;
        AlmacenJugadores almacenJugadores;

        s = new ServerSocket(5000);
        almacenJugadores = new AlmacenJugadores();
        System.out.println("Servidor Juego iniciado");
        while (true) {
            c = s.accept(); //esperando cliente
            Hilo hilo = new Hilo(c, almacenJugadores);
            Random r = new Random();
            int low = 1;
            int high = 10;
            int name = r.nextInt(high - low) + low;
            hilo.setName("Jugador " + name);
            hilo.start();
        }
    }
}
