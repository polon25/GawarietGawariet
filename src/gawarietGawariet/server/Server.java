package gawarietGawariet.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Server {
	
	static ArrayList<User> users = new ArrayList<User>(); //Tablica zarejestrowanych użytkowników
	enum status{idle, login, palSelect, logout};
	
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
	        
	        DatagramPacket response = new DatagramPacket(
	        		byteResponse, byteResponse.length, address, port);
	        
	        //Osbługa zgłoszeń od klientów
	        if (currentStatus==status.login){	//Logowanie użytkownika
	        	if (mesgCounter==0){	//Pierwsza informacja - login
	        		tmpLogin=message;
	        		mesgCounter++;
	        	}
	        	else if (mesgCounter==1){	//Druga informacja - hasło
	        		tmpPassword=message;
	        		mesgCounter=0;
	        		boolean userExists=false;
	        		for (int i=0; i<users.size();i++){	//Szukaj użytkownika
	        			if(users.get(i).login.equals(tmpLogin) & users.get(i).password.equals(tmpPassword)){	//Istnieje
	        				users.get(i).online=true;
	        				users.get(i).lastAddress=address;
	        				users.get(i).lastPort=port;
	        				userExists=true;
	        				response = new DatagramPacket(
	        						"Logged".getBytes("utf8"), "Logged".getBytes("utf8").length, address, port);
	        				break;
	        			}
	        			else if (users.get(i).login.equals(tmpLogin)){	//Złe hasło
	        				userExists=true;
	        				response = new DatagramPacket(
	        						"WrongLog".getBytes("utf8"), "WrongLog".getBytes("utf8").length, address, port);
	        				break;
	        			}
	        		}
	        		if (!userExists){	//Rejestracja i logowanie
	        			users.add(new User(tmpLogin, tmpPassword));
	        			users.get(users.size()-1).online=true;
        				users.get(users.size()-1).lastAddress=address;
        				users.get(users.size()-1).lastPort=port;
	        			response = new DatagramPacket(
        						"Registered".getBytes("utf8"), "Registered".getBytes("utf8").length, address, port);
	        		}
	        		currentStatus=status.idle;
	        	}
	        }
	        else if (currentStatus==status.logout){	//Wyloguj użytkownika, z którego adresu przyszła informacja
	        	for (int i=0; i<users.size();i++){
        			if(users.get(i).lastAddress.equals(address)){
        				users.get(i).online=false;
        				users.get(i).currentPal=null;
        			}
        		}
	        	currentStatus=status.idle;
	        }
	        else if (currentStatus==status.palSelect){	//Wybór adresata
	        	for (int i=0; i<users.size();i++){
        			if(users.get(i).login.equals(message)){
        				for (int j=0; j<users.size();i++){
        					if(users.get(j).lastAddress.equals(address)){
                				users.get(j).currentPal=users.get(i);
                				response = new DatagramPacket(
    	        						"Connected".getBytes("utf8"), "Connected".getBytes("utf8").length, address, port);
                				break;
        					}
        					response = new DatagramPacket(
	        						"NoPal".getBytes("utf8"), "NoPal".getBytes("utf8").length, address, port);
        				}
        				break;
        			}
        		}
	        	currentStatus=status.idle;
	        }
	        
	        //Zmiana statusu - tutaj, gdyż to informacje te dostaje się przed powyższymi
	        
	        System.out.println(message);
	        if (message.equals("Login")){
	        	currentStatus=status.login;
	        }
	        else if (message.equals("PalSelect")){
	        	currentStatus=status.palSelect;
	        }
	        else if (message.equals("Login")){
	        	currentStatus=status.logout;
	        }
	        
	        datagramSocket.send(response);
	    }
	}
}
