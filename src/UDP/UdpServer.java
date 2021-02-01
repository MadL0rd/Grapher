package UDP;

import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class UdpServer {
    private final int portServer = 13133;
    private final int portClient = 13137;
    private final String internalMessage = "WWA726j6xAkMkvpb";
    private final int clientClearDelayMinutes = 15;

    private ArrayList<Client> clients = new ArrayList<Client>();
    private String systemStateLastMessage = "";

    public class Client {
        InetAddress ipAddress;
        LocalTime deathTime;

        Client(InetAddress ip) {
            ipAddress = ip;
            updateTime();
        }

        public void updateTime() {
            deathTime = LocalTime.now().plus(clientClearDelayMinutes, ChronoUnit.MINUTES);
            System.out.println("Time did updated for: " + ipAddress.toString() + "\tNew death time is: " + deathTime);
        }

        public boolean isDead() {
            return deathTime.compareTo(LocalTime.now()) == 0;
        }

        public void sendMessage(String messageText) throws Exception {
            if (messageText.isEmpty()) return;

            DatagramSocket socket = new DatagramSocket();
            byte[] messageBuffer = messageText.getBytes("UTF-8");
            DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length, ipAddress, portClient);
            socket.send(packet);

            System.out.println("Send msg\n " + messageText + "to " + ipAddress.toString());
        }
    }

    public static UdpServer shared = new UdpServer();

    public interface MessageReceiveHandler {
        void receiveMessage(String messageText, String sender);
    }
    public MessageReceiveHandler messageReceiveHandler;

    public void run() throws Exception {
        System.out.println("Server is enable");

        DatagramSocket receiveSocket = new DatagramSocket(portServer);
        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket receiveData = new DatagramPacket(buffer, buffer.length);
            receiveSocket.receive(receiveData);

            InetAddress clientAddress = receiveData.getAddress();
            addAddress(clientAddress);

            String messageText = new String(receiveData.getData());
            messageText = messageText.substring(0, receiveData.getLength());
            System.out.println("Receive msg: " + messageText);

            if (!messageText.equals(internalMessage)) {
                if (messageReceiveHandler != null) {
                    messageReceiveHandler.receiveMessage(messageText, clientAddress.toString());
                }
            } else {
                // Send current state to new client
                try {
                    Client client = new Client(clientAddress);
                    client.sendMessage(systemStateLastMessage);
                } catch (Exception e) {
                    System.out.println("Error msg send");
                    e.printStackTrace();
                }
            }
        }
    }
    public void sendGraphs(List<String> graphs) {
        String messageText = "";
        for (String graph: graphs) {
            messageText += graph + "\n";
        }
        systemStateLastMessage = messageText;
        System.out.println("State log: " + systemStateLastMessage);

        clients.removeIf(client -> client.isDead());
        for (Client client : clients) {
            try {
                client.sendMessage(messageText);
            } catch (Exception e) {
                System.out.println("Error msg send");
                e.printStackTrace();
            }
        }
    }

    private void addAddress(InetAddress clientAddress) {
        for (Client client : clients) {
            if (client.ipAddress.equals(clientAddress)) {
                client.updateTime();
                return;
            }
        }
        Client client = new Client(clientAddress);
        clients.add(client);
    }
}
