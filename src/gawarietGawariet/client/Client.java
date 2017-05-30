package gawarietGawariet.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import javax.swing.JFrame;

import gawarietGawariet.server.Config;

public class Client {
	public Client() {}
	
	public String sendMesg(String message) throws Exception {
		InetAddress serverAddress = InetAddress.getByName("192.168.1.13");
        //System.out.println(serverAddress);

        DatagramSocket socket = new DatagramSocket(); //Otwarcie gniazda
        byte[] stringContents = message.getBytes("utf8"); //Pobranie strumienia bajtów z wiadomosci

        DatagramPacket sentPacket = new DatagramPacket(stringContents, stringContents.length);
        sentPacket.setAddress(serverAddress);
        sentPacket.setPort(Config.PORT);
        socket.send(sentPacket);

        DatagramPacket receivePacket = new DatagramPacket(new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
        socket.setSoTimeout(1000);
        receivePacket.getData();
        
        String serverMsg="";
        try{
            socket.receive(receivePacket);
			serverMsg=new String(receivePacket.getData(), 0, receivePacket.getLength(), "utf8");
			System.out.println(serverMsg);
        }catch (SocketTimeoutException ste){
            System.out.println("Serwer nie odpowiedział, więc albo dostał wiadomość albo nie...");
        }
        socket.close();
        return serverMsg;
	}

	public static void main(String[] args) throws Exception {
		JFrame window = new Interface();
		window.setVisible(true);
		window.setLocationRelativeTo(null);
	}
}
