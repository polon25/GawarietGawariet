package gawarietGawariet.client;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import gawarietGawariet.server.Config;

public class Interface extends JFrame implements FocusListener {
	private static final long serialVersionUID = 1L;
	
	JTextField loginField = new JTextField("Login");
	JTextField passwordField = new JTextField("Hasło");
	JTextField palsField = new JTextField("Dodaj znajomego");
	JButton loginButton = new JButton("Zaloguj");
	JButton connectButton = new JButton("Połącz");
	String stringPals[]={"Dodaj znajomego"};
	final JComboBox<String> pals = new JComboBox<String>(stringPals);
	
	JTextArea chatField = new JTextArea();
	JTextField writeField = new JTextField("Napisz wiadomość");
	
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
		
		pack();	//Po to by requestFocus zadziałał
		setSize(400,600);
		loginButton.requestFocusInWindow();	//Aby focus nie był na loginie na wstępie
		
	}
	
	private class CheckMsg extends SwingWorker<Void, Void>{	//Sprawdzanie, czy nie ma nowych wiadomości (działa w tle)
		int port;
		public CheckMsg(int port){
			this.port=port;
		}
		public Void doInBackground() throws Exception{
			DatagramSocket datagramSocket = new DatagramSocket(port);
			while(true){
				DatagramPacket reclievedPacket
                	= new DatagramPacket( new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);
				datagramSocket.receive(reclievedPacket);
				int length = reclievedPacket.getLength();
				String message =
						new String(reclievedPacket.getData(), 0, length, "utf8");
				chatField.setText(chatField.getText()+message);
			}
		}
	}
		
	public void focusGained(FocusEvent fe) {
		if(fe.getSource()==loginField || fe.getSource()==passwordField){
			if(fe.getSource()==loginField)
				loginField.setText("");
			else if(fe.getSource()==passwordField)
				passwordField.setText("");
			else if(fe.getSource()==palsField)
				palsField.setText("");
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
	
	void addPal() throws Exception {
		Client client = new Client();
		client.sendMesg("PalSelect");
		String servResp=client.sendMesg(palsField.getText());
		if(servResp.equals("Connected")){
			JOptionPane.showMessageDialog(
                    this, "Połączono się z użytkownikiem "+palsField.getText(),
                    "Łączenie z użytkownikiem",
                    JOptionPane.INFORMATION_MESSAGE);
			CheckMsg check = new CheckMsg(Integer.parseInt(client.sendMesg("PortReq")));
			check.execute();
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
	}
	void send() throws Exception {
		Client client = new Client();
		client.sendMesg("Send");
		String msg=client.sendMesg(writeField.getText());
		chatField.setText(chatField.getText()+msg);
	}
}
