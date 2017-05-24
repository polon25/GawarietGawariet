package gawarietGawariet.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Server {
	
	static ArrayList<User> users = new ArrayList<User>(); //Tablica zarejestrowanych użytkowników
	enum status{idle, login, palSelect};
	
	public static void main(String[] args) throws Exception {//Przekopiowane ze strony Graczykowskiego
		//Otwarcie gniazda z okreslonym portem
	    DatagramSocket datagramSocket = new DatagramSocket(Config.PORT);
	
	    byte[] byteResponse = "OK".getBytes("utf8");
	    
	    int mesgCounter=0;	//Licznik otrzymanych wiadomości dla specjalnych zastosowań
	    status currentStatus=status.idle;
	    String tmpLogin="";
	    String tmpPassword="";
	    
	    System.out.println("Server is running");
	
	    while (true){
	
	        DatagramPacket reclievedPacket
	                = new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
	
	        datagramSocket.receive(reclievedPacket);
	
	        int length = reclievedPacket.getLength();
	        String message =
	                new String(reclievedPacket.getData(), 0, length, "utf8");
	        
	        // Port i host który wysłał nam zapytanie
	        InetAddress address = reclievedPacket.getAddress();
	        int port = reclievedPacket.getPort();
	
	        System.out.println(message);
	        if (message.equals("Login")){
	        	currentStatus=status.login;
	        }
	        
	        DatagramPacket response = new DatagramPacket(
	        		byteResponse, byteResponse.length, address, port);
	        
	        if (currentStatus==status.login){
	        	if (mesgCounter==0){
	        		tmpLogin=message;
	        		mesgCounter++;
	        	}
	        	else if (mesgCounter==1){
	        		tmpPassword=message;
	        		mesgCounter=0;
	        		boolean userExists=false;
	        		for (int i=0; i<users.size();i++){
	        			if(users.get(i).login.equals(tmpLogin) & users.get(i).password.equals(tmpPassword)){
	        				users.get(i).online=true;
	        				users.get(i).lastAddress=address;
	        				userExists=true;
	        				response = new DatagramPacket(
	        						"Logged".getBytes("utf8"), "Logged".getBytes("utf8").length, address, port);
	        				break;
	        			}
	        			else if (users.get(i).login.equals(tmpLogin)){
	        				userExists=true;
	        				response = new DatagramPacket(
	        						"WrongLog".getBytes("utf8"), "WrongLog".getBytes("utf8").length, address, port);
	        				break;
	        			}
	        		}
	        		if (!userExists){
	        			users.add(new User(tmpLogin, tmpPassword));
	        			response = new DatagramPacket(
        						"Registered".getBytes("utf8"), "Registered".getBytes("utf8").length, address, port);
	        		}
	        	}
	        }
	        
	        datagramSocket.send(response);
	    }
	}
}
