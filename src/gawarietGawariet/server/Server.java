package gawarietGawariet.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;


public class Server {

    static ArrayList<User> users = new ArrayList<User>(); //Tablica zarejestrowanych użytkowników


    ;

    public static void main(String[] args) throws Exception {//Przekopiowane ze strony Graczykowskiego
        //Otwarcie gniazda z okreslonym portem
        DatagramSocket datagramSocket = new DatagramSocket(Config.PORT);

        byte[] byteResponse = "OK".getBytes("utf8");

        int mesgCounter = 0;    //Licznik otrzymanych wiadomości dla specjalnych zastosowań
        Status currentStatus = Status.IDLE;
        String tmpLogin = "";
        String tmpPassword = "";

        System.out.println("Server is running");

        while (true) {

            DatagramPacket reclievedPacket
                    = new DatagramPacket(new byte[Config.BUFFER_SIZE], Config.BUFFER_SIZE);

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
            if (currentStatus == Status.LOGIN) {    //Logowanie użytkownika
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
                        users.add(new User(tmpLogin, tmpPassword));
                        users.get(users.size() - 1).online = true;
                        users.get(users.size() - 1).lastAddress = address;
                        users.get(users.size() - 1).lastPort = port;
                        response = new DatagramPacket(
                                "Registered".getBytes("utf8"), "Registered".getBytes("utf8").length, address, port);
                    }
                    currentStatus = Status.IDLE;
                }
            } else if (currentStatus == Status.LOGOUT) {    //Wyloguj użytkownika, z którego adresu przyszła informacja
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).lastAddress.equals(address)) {
                        users.get(i).online = false;
                        users.get(i).busy = false;
                        users.get(i).currentPal = null;
                    }
                }
                currentStatus = Status.IDLE;
            } else if (currentStatus == Status.PALSELECT) {    //Wybór adresata
                for (int i = 0; i < users.size(); i++) {    //Szukaj adresata
                    if (users.get(i).login.equals(message)) {
                        for (int j = 0; j < users.size(); i++) {    //Szukaj wysyłającego
                            if (users.get(j).lastAddress.equals(address)) {
                                if (users.get(i).busy) {    //Jeżeli użytkownik zajęty
                                    response = new DatagramPacket(
                                            "BusyPal".getBytes("utf8"), "BusyPal".getBytes("utf8").length, address, port);
                                } else {
                                    users.get(j).currentPal = users.get(i);
                                    users.get(i).currentPal = users.get(j);
                                    users.get(j).busy = true;
                                    users.get(i).busy = true;
                                    response = new DatagramPacket(
                                            "Connected".getBytes("utf8"), "Connected".getBytes("utf8").length, address, port);
                                }
                                break;
                            }
                            response = new DatagramPacket(
                                    "NoPal".getBytes("utf8"), "NoPal".getBytes("utf8").length, address, port);
                        }
                        break;
                    }
                }
                currentStatus = Status.IDLE;
            } else if (currentStatus == Status.SEND) {    //Wyślij wiadomość od użytkownika
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).lastAddress.equals(address)) {
                        message = users.get(i).login + ": " + message + "\n";
                        response = new DatagramPacket(
                                message.getBytes("utf8"), message.getBytes("utf8").length,
                                users.get(i).currentPal.lastAddress, users.get(i).currentPal.lastPort);
                        datagramSocket.send(response);
                        response = new DatagramPacket(
                                message.getBytes("utf8"),
                                message.getBytes("utf8").length, address, port
                        );
                        break;
                    }
                }
                currentStatus = Status.IDLE;
            }

            //Zmiana statusu - tutaj, gdyż to informacje te dostaje się przed powyższymi
            /**
             * Deprecated ..
             *
             *
             *

            System.out.println(message);
            if (message.equals("Login")) {
                currentStatus = login;
            } else if (message.equals("PalSelect")) {
                currentStatus = palSelect;
            } else if (message.equals("Login")) {
                currentStatus = logout;
            } else if (message.equals("Send")) {
                currentStatus = send;
            } else if (message.equals("PortReq")) { //Wyślij numer wykorzystywanego portu
                response = new DatagramPacket(
                        Integer.toString(port).getBytes("utf8"), Integer.toString(port).getBytes("utf8").length, address, port);
            }


             * A tu to samo co wyzej w jednej linijce
             */

            currentStatus = Status.getStatus(message);

            /**
             * O nie to jest wszsytko w static mainie ....aaaa
             */
            datagramSocket.send(response);
        }
    }
}
