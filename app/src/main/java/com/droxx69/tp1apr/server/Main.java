package com.droxx69.tp1apr.server;

import com.droxx69.tp1apr.models.Client;
import com.droxx69.tp1apr.models.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main extends Thread {

    private int nbClient = 0;

    //Here we store all the clients so we can find them
    public static ArrayList<Client> clients_book = new ArrayList<>();

    public void run() {
        try {
            System.out.println("Server waiting for connections ... ");
            ServerSocket ss = new ServerSocket(8000);
            while (true) {
                Socket s = ss.accept();
                ++nbClient;
                Client newClient = new Client(nbClient, s);
                clients_book.add(newClient);
                new ServiceClient(newClient).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main().start();
    }

    class ServiceClient extends Thread {

        private int clientNumber;
        private Client client;

        private ObjectInputStream sInput;
        private ObjectOutputStream sOutput;
        private Socket socket;

        private Message msg;


        public ServiceClient(Client client) {
            this.client = client;
            socket = client.getSocket();
            clientNumber = client.getId();
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());

                client.setsInput(sInput);
                client.setsOutput(sOutput);

                sOutput.writeObject(new Message(clientNumber));

                System.out.println("Client Num " + clientNumber + " connected !");
            } catch (IOException e) {
                System.out.println("Error : " + e.getMessage());
            }
        }

        public void run() {
            while (true) {
                try {
                    msg = (Message) sInput.readObject();

                    int id_rec = msg.getId_Receiver();
                    Client receiver = Main.clients_book.get(id_rec - 1);
                    msg.setMyId(-1);

                    System.out.println(msg.getId_Sender() + "=>" + id_rec + " frame number : " + msg.getMsg_id() + " Msg : " + msg.getMessage() + " //received = " + msg.isReceived());

                    receiver.getsOutput().writeObject(msg);
                    msg = null;

                } catch (IOException e) {
                    if (msg == null || msg.getId_Receiver() == clientNumber) {
                        System.out.println("Client number " + clientNumber + " : disconnected !");
                        break;
                    } else {
                        try {
                            msg.setReceived(false);
                            msg.setReceiverDisconnected(true);
                            sOutput.writeObject(msg);
                            System.out.println("Stopped sending because receiver is offline");

                        } catch (IOException ex) {
                        }
                    }
                } catch (ClassNotFoundException e2) {
                }
            }


        }
    }
}
