package UDP;

import javax.swing.*;
import java.awt.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UdpClient {

    private final int portServer = 13133;
    private final int portClient = 13137;
    private final String internalMessage = "WWA726j6xAkMkvpb";
    private String ipBroadcast = "127.0.0.1";

    public static UdpClient shared = new UdpClient();

    public interface MessageReceiveHandler {
        void receiveMessage(String messageText, String sender);
    }
    public MessageReceiveHandler messageReceiveHandler;

    private class ThreadReceiver implements Runnable {
        @Override
        public void run() {
            System.out.println("ThreadReceiver start");
            try {
                configureReceiver();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        private void configureReceiver() throws Exception {
            DatagramSocket receiveSocket = new DatagramSocket(portClient);
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket receiveData = new DatagramPacket(buffer, buffer.length);
                receiveSocket.receive(receiveData);

                InetAddress senderAddress = receiveData.getAddress();

                String messageText = new String(receiveData.getData());
                System.out.println("Receive: " + messageText);

                if (messageReceiveHandler != null) {
                    messageReceiveHandler.receiveMessage(messageText, senderAddress.toString());
                }
            }
        }
    }
    private class ThreadAliveMessageSander implements Runnable {

        final private int secondsToSleep = 15000;

        @Override
        public void run() {
            System.out.println("ThreadAliveMessageSander start");
            try {
                configure();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        private void configure() throws Exception {
            while (true) {
                try {
                    Thread.sleep(secondsToSleep);

                    DatagramSocket socket = new DatagramSocket();
                    InetAddress ipAddress = InetAddress.getByName(ipBroadcast);
                    byte[] messageBuffer = internalMessage.getBytes("UTF-8");
                    DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length, ipAddress, portServer);
                    socket.send(packet);
                } catch (Exception exception) {}
            }
        }
    }

    public void run() {
        System.out.println("Client is enable");

        Thread aliveMessageSander = new Thread(new ThreadAliveMessageSander());
        aliveMessageSander.start();

        Thread receiver = new Thread(new ThreadReceiver());
        receiver.start();
    }
    public void sendMessage(String messageText) throws Exception {
        if (messageText.isEmpty()) return;

        DatagramSocket socket = new DatagramSocket();
        InetAddress ipAddress = InetAddress.getByName(ipBroadcast);
        byte[] messageBuffer = messageText.getBytes("UTF-8");
        DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length, ipAddress, portServer);
        socket.send(packet);
    }
    public void setNewServerAddress(String ipAddress) {
        String previousIp = ipBroadcast;
        ipBroadcast = ipAddress;
        try {
            sendMessage(internalMessage);
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(null, "Can not send message to entered server!");
            ipBroadcast = previousIp;
        }
    }
    public String getServerAddress() {
        return ipBroadcast;
    }
}

