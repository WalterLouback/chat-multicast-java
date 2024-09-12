import java.net.*;
import java.io.*;

public class ChatApp {

    private String userName;
    private InetAddress groupIp;
    private MulticastSocket mSocket;
    private int port = 6789;
    private Thread receiveThread;

    public ChatApp(String userName, String groupAddress) throws IOException {
        this.userName = userName;
        this.groupIp = InetAddress.getByName(groupAddress);
        this.mSocket = new MulticastSocket(port);
    }

    public static void main(String[] args) {
        String usuario;
        String sala;
        if (args.length == 0) {
            usuario = "Usuário A";
            sala = "230.0.0.1";
        } else {
            usuario = args[0];
            sala = args[1];
        }

        try {
            ChatApp chat = new ChatApp(usuario, sala);
            chat.enterRoom();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while (!(message = reader.readLine()).equals("sair")) {
                chat.sendMessage(usuario + ": " + message);
            }
            chat.leaveRoom();

        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }
    }

    public boolean enterRoom() {
        try {
            mSocket.joinGroup(groupIp);
            sendMessage("Usuário [" + userName + "] entrou na sala");
            startReceiving();
            return true;
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
            return false;
        }
    }

    public boolean leaveRoom() {
        try {
            sendMessage("Usuário [" + userName + "] saiu da sala");
            receiveThread.interrupt();
            mSocket.leaveGroup(groupIp);
            mSocket.close();
            return true;
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
            return false;
        }
    }

    public void sendMessage(String message) {
        try {
            byte[] messageBytes = message.getBytes();
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, groupIp, port);
            mSocket.send(packet);
        } catch (IOException e) {
            System.out.println("IO:" + e.getMessage());
        }
    }

    public void startReceiving() {
        receiveThread = new Thread(this::receiveMessages);
        receiveThread.start();
    }

    public void receiveMessages() {
        byte[] buffer = new byte[1000];
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                mSocket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println(received);
            } catch (IOException e) {
                System.out.println("IO:  " + e.getMessage());
            }
        }
    }

}
