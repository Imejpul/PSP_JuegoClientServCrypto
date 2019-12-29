import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.security.*;

public class Jugador {

    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("localhost", 5000);

        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        PublicKey clavePublicaServ = null;

        try {
            //login o registro
            //  1º menu (in)
            System.out.print(ois.readObject().toString());
            String op = br.readLine();
            //    2º opcion elegida (out)
            oos.writeObject(op);
            String succes = "";

            switch (op) {
                case "1":
                    //Clave publica servidor (in)
                    clavePublicaServ = (PublicKey) ois.readObject();

                    //nombre (in)
                    System.out.print(ois.readObject().toString());
                    //respuestaNombre(out)
                    oos.writeObject(br.readLine());

                    //apellido (in)
                    System.out.print(ois.readObject().toString());
                    //respuestaApellido(out)
                    oos.writeObject(br.readLine());

                    //edad (in)
                    System.out.print(ois.readObject().toString());
                    //respuestaEdad(out)
                    oos.writeObject(br.readLine());

                    //nick (in)
                    System.out.print(ois.readObject().toString());
                    //respuestaNick(out)
                    //oos.writeObject(br.readLine());
                    cifrarMensaje(oos, br.readLine(), clavePublicaServ);

                    //pwd (in)
                    System.out.print(ois.readObject().toString());
                    //respuestaPwd(out)
                    //oos.writeObject(br.readLine());
                    cifrarMensaje(oos, br.readLine(), clavePublicaServ);

                    //succes(in)
                    succes = ois.readObject().toString();
                    System.out.println(succes);
                    break;
                case "2":
                    //Clave publica servidor (in)
                    clavePublicaServ = (PublicKey) ois.readObject();

                    //nick (in)
                    System.out.print(ois.readObject().toString());
                    //respuestaNick(out)
                    //oos.writeObject(br.readLine());
                    cifrarMensaje(oos, br.readLine(), clavePublicaServ);

                    //pwd (in)
                    System.out.print(ois.readObject().toString());
                    //respuestaPwd(out)
                    //oos.writeObject(br.readLine());
                    cifrarMensaje(oos, br.readLine(), clavePublicaServ);

                    //succes(in)
                    succes = ois.readObject().toString();
                    System.out.println(succes);
                    break;
                case "3":
                    System.out.print(ois.readObject().toString());
                    break;
                default:
                    System.out.print(ois.readObject().toString());
                    break;
            }

            //recibe reglas desde el servidor y debe validar esas reglas (servidor envía firmadas)
            if (succes.equals("¡Registro ok!") || succes.equals("¡Login ok!")) {
                System.out.println("---------------------------");
                comprobarFirmaMensaje(ois, clavePublicaServ);
                //System.out.println(ois.readObject().toString());
                String respuesta;
                do {
                    //servidor envía preguntas -> jugador responde enviando cifradas las respuestas
                    //pregunta (in)
                    System.out.print(ois.readObject().toString());
                    //respuesta(out)
                    respuesta = br.readLine();
                    cifrarMensaje(oos, respuesta, clavePublicaServ);
                    //oos.writeObject(respuesta);
                    //puntuacion actual(in)
                    System.out.print(ois.readObject().toString());
                } while (!respuesta.equalsIgnoreCase("fin"));
                System.out.println("---------------------------");
            }

            oos.close();
            ois.close();
            socket.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void comprobarFirmaMensaje(ObjectInputStream ois, PublicKey clave) {
        try {

            String mensaje = ois.readObject().toString();

            System.out.println("Verificando firma..");

            //VERIFICACIÓN DE LA FIRMA

            //AL OBJETO signature se le suministran los datos a verificar
            Signature verificadsa = Signature.getInstance("SHA1WITHRSA");
            verificadsa.initVerify(clave);

            verificadsa.update(mensaje.getBytes());
            byte[] firma = (byte[]) ois.readObject();
            boolean check = verificadsa.verify(firma);

            //Compruebo la veracidad de la firma
            if (check){
                System.out.println("¡El mensaje es auténtico!");
                System.out.println("mensaje: " + mensaje);
            } else System.out.println("¡Intento de falsificación!");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
    }

    private static void cifrarMensaje(ObjectOutputStream oos, String mensaje, PublicKey clave) {
        try {
            if (clave != null) {
                System.out.println("Cifrando: " + mensaje);
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, clave);
                // CIFRADO
                byte[] textoPlano = mensaje.getBytes();
                byte[] textoCifrado = cipher.doFinal(textoPlano);

                oos.writeObject(textoCifrado);
                System.out.println("Mensaje cifrado enviado");
            } else {
                System.out.println("¡Error: Mensaje no cifrado!");
            }

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
