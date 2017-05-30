package gawarietGawariet.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;

public class Interface extends JFrame implements FocusListener {
	private static final long serialVersionUID = 1L;
	
	JTextField loginField = new JTextField("Login");
	JTextField passwordField = new JTextField("Hasło");
	JTextField palsField = new JTextField("Dodaj znajomego");
	JButton loginButton = new JButton("Zaloguj");
	JButton connectButton = new JButton("Połącz");
	String stringPals[]={"Dodaj znajomego"};
	JComboBox<String> pals = new JComboBox<String>(stringPals);
	
	JTextArea chatField = new JTextArea();
	JTextField writeField = new JTextField("Napisz wiadomość");
	
	ScheduledExecutorService exec;
	
	boolean isBusy = false; // Czy następuje komunikacja z serwerem? - zapobiega "wmieszaniu" się danych wysyłanych
	
	public Interface() throws Exception {
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
		
		chatField.setEditable(false);
		
		loginButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	try {
					log2server();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		    }
		});
		connectButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	try {
					addPal();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		    }
		});
		writeField.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
			    try {
					send();
					writeField.setText("");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		    }
		});
		
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
            	try {
					new Client().sendMesg("Logout");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
            }
        });
		
		loginField.addFocusListener(this);
		passwordField.addFocusListener(this);
		palsField.addFocusListener(this);
		writeField.addFocusListener(this);
		
		pack();	//Po to by requestFocus zadziałał
		setSize(400,600);
		loginButton.requestFocusInWindow();	//Aby focus nie był na loginie na wstępie       
	}
	
	public void focusGained(FocusEvent fe) {
		if(fe.getSource()==loginField)
			loginField.setText("");
		else if(fe.getSource()==passwordField)
			passwordField.setText("");
		else if(fe.getSource()==palsField)
			palsField.setText("");
		else if(fe.getSource()==writeField)
			writeField.setText("");
	}
	public void focusLost(FocusEvent fe) {}	
	
	private class CheckMsg extends SwingWorker<Void, String>{	//Sprawdzanie, czy nie ma nowych wiadomości (działa w tle)
		public Void doInBackground() throws Exception{
			while(true){	//Sprawdzenie, czy na serwerze są jakieś wiadomości
				while(isBusy);
				isBusy=true;
				Client client = new Client();
				String response=client.sendMesg("CheckMsg");
				if (!response.equals("NoMsg")){
					System.out.println("Otrzymano wiadomość: "+response);
					chatField.setText(chatField.getText()+response);
				}
				isBusy=false;
				Thread.sleep(2000);
			}
		}
	}
	
	private void startListen(){//Nasłuchiwanie w tle
		CheckMsg check = new CheckMsg();
		new Timer(100, new ActionListener() {	
			 public void actionPerformed(ActionEvent e) {
				 check.execute();
				 validate();
			 }
		 }).start();
	}
	
	private void downloadPals() throws Exception{//Nasłuchiwanie w tle
		Client client = new Client();
		while(true){
			String pal=client.sendMesg("PalsList");
			if (pal.equals("NoPal"))
				break;
			this.pals.addItem(pal);
		}
	}
	
	private void log2server() throws Exception {
		while(isBusy);
		isBusy=true;
		Client client = new Client();
		client.sendMesg("Login");
		client.sendMesg(loginField.getText());
		String servResp=client.sendMesg(passwordField.getText());
		if(servResp.equals("Logged")){
			JOptionPane.showMessageDialog(
                    this, "Logowanie się powiodło",
                    "Logowanie",
                    JOptionPane.INFORMATION_MESSAGE);
			downloadPals();
			startListen();
		}
		else if(servResp.equals("Registered")){
			JOptionPane.showMessageDialog(
                    this, "Zarejestrowano się",
                    "Logowanie",
                    JOptionPane.INFORMATION_MESSAGE);
			startListen();
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
		isBusy=false;
	}
	
	private void addPal() throws Exception {
		while(isBusy);
		isBusy=true;
		Client client = new Client();
		client.sendMesg("PalSelect");
		String pal=" ";
		if (!pals.getSelectedItem().equals("Dodaj znajomego"))
			pal=(String) pals.getSelectedItem();
		else
			pal=palsField.getText();
		String servResp=client.sendMesg(pal);
		if(servResp.equals("Connected")){
			JOptionPane.showMessageDialog(
                    this, "Połączono się z użytkownikiem "+pal,
                    "Łączenie z użytkownikiem",
                    JOptionPane.INFORMATION_MESSAGE);
		}
		else if(servResp.equals("BusyPal")){
			JOptionPane.showMessageDialog(
                    this, "Użytkownik jest zajęty. Poczekaj jakiś czas.",
                    "Łączenie z użytkownikiem",
                    JOptionPane.WARNING_MESSAGE);
		}
		else if(servResp.equals("NoPal")){
			JOptionPane.showMessageDialog(
                    this, "Nie istnieje taki użytkownik",
                    "Łączenie z użytkownikiem",
                    JOptionPane.ERROR_MESSAGE);
		}
		else {
			JOptionPane.showMessageDialog(
                    this, "Wystąpił problem. Nie udało się połączyć.",
                    "Łączenie z użytkownikiem",
                    JOptionPane.ERROR_MESSAGE);
		}
		isBusy=false;
	}
	private void send() throws Exception {
		while(isBusy);
		isBusy=true;
		Client client = new Client();
		client.sendMesg("Send");
		String msg=client.sendMesg(writeField.getText());
		chatField.setText(chatField.getText()+msg);
		isBusy=false;
	}
}
