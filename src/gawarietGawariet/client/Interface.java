package gawarietGawariet.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Interface extends JFrame implements FocusListener {
	private static final long serialVersionUID = 1L;
	
	JTextField loginField = new JTextField("Login");
	JTextField passwordField = new JTextField("Hasło");
	JTextField palsField = new JTextField("Dodaj znajomego");
	JButton loginButton = new JButton("Zaloguj");
	JButton connectButton = new JButton("Połącz");
	String stringPals[]={"Dodaj znajomego"};
	final JComboBox<String> pals = new JComboBox<String>(stringPals);
	
	JTextField chatField = new JTextField();
	JTextField writeField = new JTextField("Napisz wiadomość");
	
	public Interface() {
		setSize(400,600);
		setTitle("Gawariet-Gawariet");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		
		JPanel loginPanel = new JPanel();	//Panel z danymi logowania oraz wyborem/dodaniem znajomego
		JPanel chatPanel = new JPanel();	//Panel z chatem
		JPanel writePanel = new JPanel();	//Panel z polem do wpisywania tekstu
		
		add(BorderLayout.NORTH, loginPanel);
		add(BorderLayout.CENTER, chatPanel);
		add(BorderLayout.SOUTH, writePanel);
		
		loginPanel.setLayout(new GridLayout(2,3));
		loginPanel.add(loginField);
		loginPanel.add(passwordField);
		loginPanel.add(loginButton);
		loginPanel.add(palsField);	//Użytkownik może bądź wpisać adresata
		loginPanel.add(pals);	//Bądź wybrać z listy
		loginPanel.add(connectButton);	//Wpisanie za pierwszym razem dodaje do listy znajomych
		
		chatPanel.setLayout(new BorderLayout());
		chatPanel.add(chatField);
		writePanel.setLayout(new BorderLayout());
		writePanel.add(writeField);
		
		loginButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	try {
					log2server();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		    }
		});
		
		loginField.addFocusListener(this);
		passwordField.addFocusListener(this);
		pack();	//Po to by requestFocus zadziałał
		setSize(400,600);
		loginButton.requestFocusInWindow();	//Aby focus nie był na loginie na wstępie
	}
		
	public void focusGained(FocusEvent fe) {
		if(fe.getSource()==loginField || fe.getSource()==passwordField){
			if(fe.getSource()==loginField)
				loginField.setText("");
			else
				passwordField.setText("");
		}
	}

	public void focusLost(FocusEvent fe) {}	
	
	void log2server() throws Exception {
		Client client = new Client();
		client.sendMesg("Login");
		client.sendMesg(loginField.getText());
		String servResp=client.sendMesg(passwordField.getText());
		if(servResp.equals("Logged")){
			JOptionPane.showMessageDialog(
                    this, "Logowanie się powiodło",
                    "Logowanie",
                    JOptionPane.INFORMATION_MESSAGE);
		}
		else if(servResp.equals("Registered")){
			JOptionPane.showMessageDialog(
                    this, "Zarejestrowano się",
                    "Logowanie",
                    JOptionPane.INFORMATION_MESSAGE);
		}
		else if(servResp.equals("WrongLog")){
			JOptionPane.showMessageDialog(
                    this, "Podano złe hasło",
                    "Logowanie",
                    JOptionPane.ERROR_MESSAGE);
		}
		else {
			JOptionPane.showMessageDialog(
                    this, "Wystąpił problem. Logowanie nie powiodło się.",
                    "Logowanie",
                    JOptionPane.ERROR_MESSAGE);
		}
	}
}
