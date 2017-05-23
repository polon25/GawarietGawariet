package gawarietGawariet.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import gawarietGawariet.server.Config;

public class Client {

	public static void main(String[] args) throws Exception {
		String message = "tekst";
        InetAddress serverAddress = InetAddress.getByName("localhost");
        System.out.println(serverAddress);

        DatagramSocket socket = new DatagramSocket(); //Otwarcie gniazda
        byte[] stringContents = message.getBytes("utf8"); //Pobranie strumienia bajt�w z wiadomosci

        DatagramPacket sentPacket = new DatagramPacket(stringContents, stringContents.length);
        sentPacket.setAddress(serverAddress);
        sentPacket.setPort(Config.PORT);
        socket.send(sentPacket);

        DatagramPacket reclievePacket = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
        socket.setSoTimeout(1010);

        try{
            socket.receive(reclievePacket);
            System.out.println("Serwer otrzyma� wiadomo��");
        }catch (SocketTimeoutException ste){
            System.out.println("Serwer nie odpowiedzia�, wi�c albo dosta� wiadomo�� albo nie...");
        }
        socket.close();
	}
}
