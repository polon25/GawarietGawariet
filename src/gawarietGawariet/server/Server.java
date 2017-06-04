package gawarietGawariet.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * 
 * @author Polonius
 *
 */

public class Server {

    static ArrayList<User> users = new ArrayList<User>(); //Tablica zarejestrowanych użytkowników
    enum status{idle, login, palSelect, logout, send, palsList};
    
    static int mesgCounter = 0;    //Licznik otrzymanych wiadomości dla specjalnych zastosowań
    static status currentStatus = status.idle;
    static String tmpLogin = "";
    static String tmpPassword = "";

    public static void main(String[] args) throws Exception {
        //Otwarcie gniazda z okreslonym portem
        DatagramSocket datagramSocket = new DatagramSocket(Config.PORT);
        byte[] byteResponse = "OK".getBytes("utf8");
        System.out.println("Server is running");
        
        //Baza danych użytkowników
        try{ 
			new DataBase(); //Create new h2
			downloadData();
		} catch (SQLException e){}//If there's already h2	
        
        while (true) {
            DatagramPacket reclievedPacket
                    = new DatagramPacket(new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
            datagramSocket.receive(reclievedPacket);
            int length = reclievedPacket.getLength();
            String message = new String(reclievedPacket.getData(), 0, length, "utf8");

            // Port i host który wysłał nam zapytanie
            InetAddress address = reclievedPacket.getAddress();
            int port = reclievedPacket.getPort();

            DatagramPacket response = new DatagramPacket(
                    byteResponse, byteResponse.length, address, port);

            //Osbługa zgłoszeń od klientów
            response=checkStatus(message, response, address, port);       
            //Zmiana statusu - tutaj, gdyż to informacje te dostaje się przed powyższymi
            System.out.println(message);
            response=setStatus(message, response, address, port);

            //currentStatus = Status.getStatus(message);
            datagramSocket.send(response);
        }
    }
    
    private static void downloadData() throws SQLException, UnknownHostException{
    	Connection conn = null;
		try{
			conn = DriverManager.getConnection("jdbc:h2:users", "sa", "");
			
			Statement statement = conn.createStatement();
			statement.execute("SELECT Login, Password, Address, Port FROM users");
			
			ResultSet rs = statement.getResultSet();
			ResultSetMetaData md = rs.getMetaData();
			
			while (rs.next()) {	//Pobierz dane użytkowników
				users.add(new User(MessageFormat.format("{0}", rs.getObject(1)),MessageFormat.format("{0}", rs.getObject(2))));
				users.get(users.size() - 1).online = true;
		        users.get(users.size() - 1).lastAddress = InetAddress.getByName(MessageFormat.format("{0}", rs.getObject(3)));
		        users.get(users.size() - 1).lastPort = (int) rs.getObject(4);
		        users.get(users.size() - 1).pals.add("NoPal");	
			}
		} finally {
			if (conn!=null)
				conn.close();
		}
    }
    
    static DatagramPacket setStatus(String message, DatagramPacket response, InetAddress address, int port) throws Exception{
    	//Zwraca response, bo inaczej nie działa (na razie)
    	if (message.equals("Login")) {
            currentStatus = status.login;
        } else if (message.equals("PalSelect")) {
            currentStatus = status.palSelect;
        } else if (message.equals("PalsList")) {
        	response=palsList(message, response, address, port);
        } else if (message.equals("Logout")) {
            currentStatus = status.logout;
        } else if (message.equals("Send")) {
            currentStatus = status.send;
        } else if (message.equals("PortReq")) { //Wyślij numer wykorzystywanego portu
            response = new DatagramPacket(
                    Integer.toString(port).getBytes("utf8"), Integer.toString(port).getBytes("utf8").length, address, port);
        }
        else if (message.equals("CheckMsg")) {
            response=checkMsg(message, response, address, port);
        } 
        else if (message.equals("ReqPalYes")) {
            response=ReqPal(message, response, address, port, true);
        } 
        else if (message.equals("ReqPalNo")) {
            response=ReqPal(message, response, address, port, false);
        } 
    	return response;
    }
    
    static DatagramPacket checkMsg(String message, DatagramPacket response, InetAddress address, int port) throws Exception{
    	for (int i = 0; i < users.size(); i++) {
            if (users.get(i).lastAddress.equals(address)) {
            	if (users.get(i).unreadMesg.size()>0){
            		message = users.get(i).unreadMesg.get(0);
            		users.get(i).unreadMesg.remove(0);
            	}
            	else
            		message = "NoMsg";
                response = new DatagramPacket(
                        message.getBytes("utf8"),
                        message.getBytes("utf8").length, address, port);
                break;
            }
        }
    	return response;
    }
    
    static DatagramPacket checkStatus(String message, DatagramPacket response, InetAddress address, int port) throws Exception{
    	if (currentStatus == status.login) {    //Logowanie użytkownika
    		response=login(message, response, address, port);
        } else if (currentStatus == status.logout) {    //Wyloguj użytkownika, z którego adresu przyszła informacja
        	response=logout(message, response, address, port);
            currentStatus = status.idle;
        } else if (currentStatus == status.palSelect) {    //Wybór adresata
        	response=palSelect(message, response, address, port);
            currentStatus = status.idle;
        } else if (currentStatus == status.send) {    //Wyślij wiadomość od użytkownika
        	response=send(message, response, address, port);
        	currentStatus = status.idle;
        }
    	return response;
    }
    
    static DatagramPacket login(String message, DatagramPacket response, InetAddress address, int port) throws Exception{
    	if (mesgCounter == 0) {    //Pierwsza informacja - LOGIN
            tmpLogin = message;
            mesgCounter++;
        } else if (mesgCounter == 1) {    //Druga informacja - hasło
            tmpPassword = message;
            mesgCounter = 0;
            boolean userExists = false;
            for (int i = 0; i < users.size(); i++) {    //Szukaj użytkownika
                if (users.get(i).login.equals(tmpLogin) & users.get(i).password.equals(tmpPassword)) {    //Istnieje
                    users.get(i).online = true;
                    users.get(i).busy = false;
                    users.get(i).lastAddress = address;
                    users.get(i).lastPort = port;
                    userExists = true;
                    response = new DatagramPacket(
                            "Logged".getBytes("utf8"), "Logged".getBytes("utf8").length, address, port);
                    break;
                } else if (users.get(i).login.equals(tmpLogin)) {    //Złe hasło
                    userExists = true;
                    response = new DatagramPacket(
                            "WrongLog".getBytes("utf8"), "WrongLog".getBytes("utf8").length, address, port);
                    break;
                }
            }
            if (!userExists) {    //Rejestracja i logowanie
                addUser(tmpLogin,tmpPassword,address,port);
                response = new DatagramPacket(
                        "Registered".getBytes("utf8"), "Registered".getBytes("utf8").length, address, port);
            }
            currentStatus = status.idle;
        }
        return response;
    }
    
    private static void addUser(String login, String password, InetAddress address, int port) throws SQLException{
    	users.add(new User(login, password));
        users.get(users.size() - 1).online = true;
        users.get(users.size() - 1).lastAddress = address;
        users.get(users.size() - 1).lastPort = port;
        users.get(users.size() - 1).pals.add("NoPal");
    	Connection conn = null;
		try{
			conn = DriverManager.getConnection("jdbc:h2:users", "sa", "");
			PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO users VALUES (?, ?, ?, ?);");
			preparedStatement.setObject(1, login);
			preparedStatement.setObject(2, password);
			preparedStatement.setObject(3, address.getHostAddress());
			preparedStatement.setObject(4, port);
			preparedStatement.execute();
		} finally {
			if (conn!=null)
				conn.close();
		}
    }
    
    static DatagramPacket logout(String message, DatagramPacket response, InetAddress address, int port) throws Exception{
    	for (int i = 0; i < users.size(); i++) {
            if (users.get(i).lastAddress.equals(address)) {
                users.get(i).online = false;
                users.get(i).busy = false;
                if(users.get(i).currentPal!=null){
                	users.get(i).currentPal.busy = false; //<- Zakomentować podczas testów na jednym urządzeniu
                	users.get(i).currentPal.unreadMesg.add("Zakończono połączenie");	
                }
                users.get(i).currentPal = null;
            }
        }
        return response;
    }
    
    static DatagramPacket palSelect(String message, DatagramPacket response, InetAddress address, int port) throws Exception{
    	for (int i = 0; i < users.size(); i++) {    //Szukaj adresata
            if (users.get(i).login.equals(message)) {
                for (int j = 0; j < users.size(); j++) {    //Szukaj wysyłającego
                    if (users.get(j).lastAddress.equals(address)) {
                        if (users.get(i).busy) {    //Jeżeli użytkownik zajęty
                            response = new DatagramPacket(
                                    "BusyPal".getBytes("utf8"), "BusyPal".getBytes("utf8").length, address, port);
                        } else {
                        	if (!users.get(j).pals.contains(users.get(i).login)){	//Jeżeli nie są znajomymi
                        		response=palRequest(message, response, address, port, users.get(j), users.get(i));
                        	}
                        	else{	//Jeżeli są znajomymi
                        		users.get(j).currentPal = users.get(i);
                        		users.get(i).currentPal = users.get(j);
                        		users.get(j).busy = true;
                        		users.get(i).busy = true;
                        		users.get(j).unreadMesg.add("Połączono z: "+users.get(i).login+"\n");
                        		users.get(i).unreadMesg.add("Połączono z: "+users.get(j).login+"\n");
                        		response = new DatagramPacket(
                        				"Connected".getBytes("utf8"), "Connected".getBytes("utf8").length, address, port);
                        	}
                        }
                        break;
                    }
                    response = new DatagramPacket(
                            "NoPal".getBytes("utf8"), "NoPal".getBytes("utf8").length, address, port);
                }
                break;
            }
    	}
        return response;
    }
    
    static DatagramPacket ReqPal(String message, DatagramPacket response, InetAddress address, int port, boolean yes) 
    		throws Exception{
    	if (yes){	//Odpowiedź na prośbę o znajomych
    		for (int i = 0; i < users.size(); i++) {	//Dodanie znajomych
                if (users.get(i).lastAddress.equals(address)) {
                	users.get(i).pals.add(0,users.get(i).currentPal.login);
                	users.get(i).currentPal.pals.add(0,users.get(i).login);
                	users.get(i).currentPal.unreadMesg.add("ReqPalYes");
                	users.get(i).currentPal.unreadMesg.add(users.get(i).login);
                	users.get(i).currentPal=null; 
                }
    		}
    	}
    	else{
    		for (int i = 0; i < users.size(); i++) {	//Dodanie znajomych
                if (users.get(i).lastAddress.equals(address)) {
                	users.get(i).currentPal.unreadMesg.add("ReqPalNo");
                	users.get(i).currentPal.unreadMesg.add(users.get(i).login);
                	users.get(i).currentPal=null; 
                }
    		}
    	}
    	return response;
    }
    
    static DatagramPacket palRequest(String message, DatagramPacket response, InetAddress address, int port,
    		User fromU, User toU)throws Exception{	//fromU, toU - nadawca i odbiorca
    	response = new DatagramPacket(
			"ReqPal".getBytes("utf8"), "ReqPal".getBytes("utf8").length, address, port);
    	toU.unreadMesg.add("ReqPal");
    	toU.unreadMesg.add(fromU.login);
    	toU.currentPal = fromU; //Wstępne ustawienie jednostronnej znajomości
    	return response;
    }
    
    static DatagramPacket palsList(String message, DatagramPacket response, InetAddress address, int port) throws Exception{
    	for (int j = 0; j < users.size(); j++) {    //Szukaj wysyłającego
            if (users.get(j).lastAddress.equals(address)) {
            	message=users.get(j).pals.get(mesgCounter);
            	if(!users.get(j).pals.get(mesgCounter).equals("NoPal"))
            		mesgCounter++;
            	else
            		mesgCounter=0;
            	response = new DatagramPacket(
                        message.getBytes("utf8"),
                        message.getBytes("utf8").length, address, port);
            	break;
            }
    	}
    	return response;
    }
    
    static DatagramPacket send(String message, DatagramPacket response, InetAddress address, int port) throws Exception{
    	for (int i = 0; i < users.size(); i++) {
            if (users.get(i).lastAddress.equals(address)) {
                message = users.get(i).login + ": " + message + "\n";
                users.get(i).currentPal.unreadMesg.add(message);
                response = new DatagramPacket(
                        message.getBytes("utf8"),
                        message.getBytes("utf8").length, address, port);
                break;
            }
        }
        return response;
    }
}
