package gawarietGawariet.server;

import java.net.InetAddress;
import java.util.ArrayList;

public class User {
	String login;
	String password;
	boolean online=false; //Czy użytkownik jest online?
	InetAddress lastAddress;	//Obecnie używane IP
	User currentAddresser=null;	//Użytkownik z którym obecnie pisze
	ArrayList<String> pals = new ArrayList<String>();	//Lista znajomych
	ArrayList<String> unreadMesg = new ArrayList<String>();	//Nieodebrane wiadomości
	
	public User(String login, String password) {
		this.login=login;
		this.password=password;
	}

}
