package gawarietGawariet.server;

import java.util.ArrayList;

public class User {
	String login;
	String password;
	ArrayList<String> unreadMesg = new ArrayList<String>();
	
	public User(String login, String password) {
		this.login=login;
		this.password=password;
	}

}
