import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Hilo extends Thread {

    private List<Quest> juego = new ArrayList<>();
    private int puntuacion;
    private Socket c;
    private AlmacenJugadores almacen;

    private KeyPairGenerator keygen;
    private PrivateKey privada;
    private PublicKey publica;

    Hilo(Socket c, AlmacenJugadores almacenJugadores) {
        this.c = c;
        generarPreguntasJuego();
        puntuacion = 0;
        almacen = almacenJugadores;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(c.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(c.getInputStream());
            generarClavesCifrado();

            switch (menuJuego(oos, ois)) {
                case 1: //registrar jugador si no registrado
                    oos.writeObject(publica);
                    jugadorNoRegistradoMenu(oos, ois, almacen, privada);
                    oos.writeObject("¡Registro ok!");
                    enviarReglasJuego(oos, publica, privada);
                    preguntaRespuesta(oos, ois, privada);
                    System.out.println("exit -> " + this.getName());
                    break;
                case 2:
                    oos.writeObject(publica);
                    if (jugadorRegistradoMenu(oos, ois, almacen, privada)) {
                        //si login ok:
                        oos.writeObject("¡Login ok!");
                        enviarReglasJuego(oos, publica, privada);
                        preguntaRespuesta(oos, ois, privada);
                        System.out.println("exit -> " + this.getName());
                    } else {
                        oos.writeObject("¡Error login!");
                    }
                    break;
                case 3:
                    oos.writeObject("¡Saliendo..!");
                    System.out.println("exit -> " + this.getName());
                    break;
                default:
                    oos.writeObject("¡Error opcion! + \n Saliendo..");
                    System.out.println("exit -> " + this.getName());
                    break;
            }

            oos.close();
            ois.close();
            c.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generarClavesCifrado() {
        try {
            keygen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        System.out.println("Generando par de claves");
        KeyPair par = keygen.generateKeyPair();
        privada = par.getPrivate();
        publica = par.getPublic();
    }

    private void preguntaRespuesta(ObjectOutputStream oos, ObjectInputStream ois, PrivateKey privada) {
        try {
            for (int i = 0; i < juego.size(); i++) {
                String respuesta;

                //pregunta (out)
                String pregunta = juego.get(i).getPregunta();
                oos.writeObject(pregunta);

                String respAlmacenada = String.valueOf(juego.get(i).getRespuesta());

                //respuesta (in)
                respuesta = descifrarMensaje((byte[]) ois.readObject(), privada);
                //respuesta = ois.readObject().toString();

                //gestion puntuacion
                if (respuesta.equalsIgnoreCase(respAlmacenada)) {
                    puntuacion += 10;
                    oos.writeObject("+10 pts \n");
                } else if (!respuesta.equalsIgnoreCase(respAlmacenada) && !respuesta.equalsIgnoreCase("fin")) {
                    puntuacion -= 5;
                    oos.writeObject("-5 pts \n");
                } else if (respuesta.equalsIgnoreCase("fin")) {
                    oos.writeObject("Puntuacion Final: " + puntuacion + "\n");
                    Thread.sleep(3000);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void generarPreguntasJuego() {
        for (int i = 0; i < 50; i++) {
            Random r = new Random();
            int low = 1;
            int high = 100;
            int multiplicando1 = r.nextInt(high - low) + low;
            int multiplicando2 = r.nextInt(high - low) + low;
            String pregunta = "¿Cuánto es " + multiplicando1 + " por " + multiplicando2 + "?: ";
            int respuesta = multiplicando1 * multiplicando2;

            Quest q = new Quest(pregunta, respuesta);
            juego.add(q);
        }
    }

    private static void jugadorNoRegistradoMenu(ObjectOutputStream oos, ObjectInputStream ois, AlmacenJugadores almacen, PrivateKey privada) {
        try {
            String nombre;
            String apellido;
            int edad;
            String nickname;
            String pwd;

            String mensajeSaliente;

            mensajeSaliente = " - Introduzca datos registro - \n" + "¿Nombre?: ";
            oos.writeObject(mensajeSaliente);
            nombre = (String) ois.readObject();

            mensajeSaliente = "¿Apellido?: ";
            oos.writeObject(mensajeSaliente);
            apellido = (String) ois.readObject();

            mensajeSaliente = "¿edad?: ";
            oos.writeObject(mensajeSaliente);
            edad = Integer.parseInt(ois.readObject().toString());

            mensajeSaliente = "¿Nickname?: ";
            oos.writeObject(mensajeSaliente);
            nickname = descifrarMensaje((byte[]) ois.readObject(), privada);
            //nickname = ois.readObject().toString();

            mensajeSaliente = "¿Password?: ";
            oos.writeObject(mensajeSaliente);
            pwd = descifrarMensaje((byte[]) ois.readObject(), privada);
            //pwd = ois.readObject().toString();

            DatosJugador datosJugador = new DatosJugador(nombre, apellido, edad, nickname, pwd);
            almacen.almacenarJugador(datosJugador);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static boolean jugadorRegistradoMenu(ObjectOutputStream oos, ObjectInputStream ois, AlmacenJugadores almacenJugadores, PrivateKey privada) {
        try {
            String nickname;
            String pwd;

            String mensajeSaliente;

            mensajeSaliente = " - Introduzca datos login - \n" + "¿Nickname?: ";
            oos.writeObject(mensajeSaliente);
            nickname = descifrarMensaje((byte[]) ois.readObject(), privada);
            //nickname = ois.readObject().toString();

            mensajeSaliente = "¿Password?: ";
            oos.writeObject(mensajeSaliente);
            pwd = descifrarMensaje((byte[]) ois.readObject(), privada);
            //pwd = ois.readObject().toString();

            DatosJugador datosJugador = new DatosJugador();
            datosJugador.setNickName(nickname);
            datosJugador.setPwd(pwd);

            if (almacenJugadores.comprobarCredencialesJugador(datosJugador)) {
                return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static int menuJuego(ObjectOutputStream oos, ObjectInputStream ois) {
        try {
            String mensajeSaliente = "- Bienvenido/a -\n"
                    + ".  1 Jugador no registrado. \n"
                    + ".  2 Jugador registrado.  \n"
                    + ".  3 SALIR.\n"
                    + "............................................................\n" +
                    "¿Opcion?: ";
            oos.writeObject(mensajeSaliente);
            return Integer.parseInt(ois.readObject().toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void enviarReglasJuego(ObjectOutputStream oos, PublicKey publica, PrivateKey privada) {
        String mensajeSaliente = "- Reglas Juego -\n"
                + ".  1 Pregunta acertada +10pts. \n"
                + ".  2 Pregunta errada -5pts.  \n"
                + ".  3 Puntuacion inicial 0 pts..\n"
                + ".  4 Puntuacion final mostrada al escribir 'fin' como respuesta\n"
                + "............................................................\n";
        firmarMensaje(oos, mensajeSaliente, publica, privada);
        /*try {
            oos.writeObject(mensajeSaliente);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private static void firmarMensaje(ObjectOutputStream oos, String mensaje, PublicKey publica, PrivateKey privada) {
        try {
            // Creamos la firma digital
            oos.writeObject(mensaje);
            //  FIRMA CON CLAVE PRIVADA EL MENSAJE
            //AL  OBJETO Signature SE LE SUMINISTRAN LOS DATOS A FIRMAR
            Signature dsa = Signature.getInstance("SHA1WITHRSA");
            dsa.initSign(privada);

            dsa.update(mensaje.getBytes());
            byte[] firma = dsa.sign(); //MENSAJE FIRMADO
            oos.writeObject(firma);

        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InvalidKeyException ex) {
            ex.printStackTrace();
        } catch (SignatureException ex) {
            ex.printStackTrace();
        }
    }

    private static String descifrarMensaje(byte[] mensajeCifrado, PrivateKey privada) {
        try {
            System.out.println("Descifrando..");

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privada);
            // DESCIFRADO
            byte[] desencriptado = cipher.doFinal(mensajeCifrado);
            String mensajeDescifrado = new String(desencriptado);
            System.out.println("Mensaje Descifrado: " + mensajeDescifrado);

            return mensajeDescifrado;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

        return "";
    }
}
