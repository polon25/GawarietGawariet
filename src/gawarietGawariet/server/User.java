package gawarietGawariet.server;

import java.util.ArrayList;

public class User {
	String login;
	String password;
	boolean online=false; //Czy użytkownik jest online?
	ArrayList<String> pals = new ArrayList<String>();	//Lista znajomych
	ArrayList<String> unreadMesg = new ArrayList<String>();	//Nieodebrane wiadomości
	
	public User(String login, String password) {
		this.login=login;
		this.password=password;
	}

}
