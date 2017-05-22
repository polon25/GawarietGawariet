package gawarietGawariet;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
	public static void main(String[] args) throws Exception {//Przekopiowane ze strony Graczykowskiego
		//Otwarcie gniazda z okreslonym portem
	    DatagramSocket datagramSocket = new DatagramSocket(Config.PORT);
	
	    byte[] byteResponse = "OK".getBytes("utf8");
	
	    while (true){
	
	        DatagramPacket reclievedPacket
	                = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
	
	        datagramSocket.receive(reclievedPacket);
	
	        int length = reclievedPacket.getLength();
	        String message =
	                new String(reclievedPacket.getData(), 0, length, "utf8");
	
	        // Port i host kt�ry wys�a� nam zapytanie
	        InetAddress address = reclievedPacket.getAddress();
	        int port = reclievedPacket.getPort();
	
	        System.out.println(message);
	        Thread.sleep(1000); //To oczekiwanie nie jest potrzebne dla
	        // obs�ugi gniazda
	
	        DatagramPacket response
	                = new DatagramPacket(
	                    byteResponse, byteResponse.length, address, port);
	
	        datagramSocket.send(response);
	    }
	}
}