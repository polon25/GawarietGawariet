package gawarietGawariet.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Interface extends JFrame {
	private static final long serialVersionUID = 1L;
	
	JTextField loginField = new JTextField("Login");
	JTextField passwordField = new JTextField("Hasło");
	
	String stringPals[]={};
	final JComboBox<String> pals = new JComboBox<String>(stringPals);
	
	public Interface() {
		setSize(400,600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Gawariet-Gawariet");
		setLayout(new BorderLayout());
		
		JPanel loginPanel = new JPanel();	//Panel z danymi logowania oraz wyborem/dodaniem znajomego
		JPanel chatPanel = new JPanel();	//Panel z chatem
		JPanel writePanel = new JPanel();	//Panel z polem do wpisywania tekstu
		
		add(BorderLayout.NORTH, loginPanel);
		add(BorderLayout.CENTER, chatPanel);
		add(BorderLayout.SOUTH, writePanel);
		
		loginPanel.setLayout(new GridLayout(2,5));
		
		loginPanel.add(loginField);
		loginPanel.add(passwordField);
		loginPanel.add(new JLabel());
		loginPanel.add(new JLabel("Znajomi"));
		loginPanel.add(pals);
	}

}
