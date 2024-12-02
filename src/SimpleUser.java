import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

public class SimpleUser implements User {
    protected final int SERVICE_PORT_SERVER;
    protected final int SERVICE_PORT_CLIENT;
    protected DatagramSocket serverSocket;
    protected volatile boolean runningServer = true;
    protected volatile boolean runningClient = true;
    protected DatagramSocket clientSocket;
    protected InetAddress IPAddress;
    protected BufferedReader in;
    protected final int lenPackage = 8;
    protected final int amountCharInPackage = 5;
    protected HashMap<Integer, String> mapReceivedMessage;


    SimpleUser(int portServer, int portClient) {
        this.SERVICE_PORT_SERVER = portServer;
        this.SERVICE_PORT_CLIENT = portClient;
    }

    public void getM() {

        byte[] receivingDataBuffer = new byte[9];
        try {
            int maxRecPackages = 999; // 4995 символов при стандартных настройках
            mapReceivedMessage = new HashMap<>();
            DatagramPacket inputPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);

            while (mapReceivedMessage.keySet().stream().count() < maxRecPackages) {
                serverSocket.receive(inputPacket);
                String receivedData = (new String(inputPacket.getData())).substring(0, inputPacket.getLength());
                if (receivedData.length() != lenPackage || receivedData.charAt(7) == '`') {
                    maxRecPackages = Integer.parseInt(receivedData.substring(0, 3)) + 1;
                    mapReceivedMessage.put(
                            Integer.parseInt(receivedData.substring(0, 3)),
                            receivedData.substring(3, receivedData.length() - 1));
                } else {
                    mapReceivedMessage.put(
                            Integer.parseInt(receivedData.substring(0, 3)),
                            receivedData.substring(3));
                }
                //System.out.println(Integer.parseInt(receivedData.substring(0, 3))); -- вывод количества пакетов

            }
            System.out.println(String.join("", mapReceivedMessage.values()));


        } catch (IOException _) {}
    }

    public void sendM(String sentence) {
        if (sentence.contains("exit")) {
            try {
                close();
            } catch (IOException _) {}
            return;
        }
        byte[] sendingDataBuffer;
        int amountOfPackages;
        String currPackage;
        try {
            /* Определение количества пакетов */
            if (sentence.length() % amountCharInPackage == 0) {
                amountOfPackages = (sentence.length() / (amountCharInPackage));
            } else if (sentence.length() < (amountCharInPackage)) {
                amountOfPackages = 1;
            } else {
                amountOfPackages = (sentence.length() / (amountCharInPackage)) + 1;
            }
            /*  Определение и присваивание пакету номера */
            for (int i = 0; i < amountOfPackages; i++) {
                String b = String.valueOf(i);
                if (i < 10) {
                    b = "00" + i;
                } else if (i < 100) {
                    b = "0" + i;
                }
                if (sentence.length() <= amountCharInPackage) {
                    currPackage = b + sentence + "`";
                    sentence = "";
                } else {
                    currPackage = b + sentence.substring(0, amountCharInPackage);
                    sentence = sentence.substring(amountCharInPackage);
                }
                sendingDataBuffer = currPackage.getBytes();


                DatagramPacket sendingPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length, IPAddress, SERVICE_PORT_CLIENT);

                clientSocket.send(sendingPacket);
            }
        } catch (Exception _) {
        }
    }

    public void start() throws IOException {
        try {
            serverSocket = new DatagramSocket(SERVICE_PORT_SERVER);
            System.out.println("Waiting for a client to connect...");
            clientSocket = new DatagramSocket();
            IPAddress = InetAddress.getByName("localhost");

            in = new BufferedReader(new InputStreamReader(System.in));
            Thread threadSendM = new Thread(() -> {
                while (runningClient) {
                    String sentence;
                    try {
                        sentence = in.readLine();
                        sendM(sentence);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            });

            Thread threadGetM = new Thread(() -> {
                while (runningServer) {
                    getM();
                }
            });


            threadGetM.start();
            threadSendM.start();
            try {
                threadGetM.join();
                threadSendM.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } catch (SocketException _) {
        }
    }

    public void close() throws IOException {
        runningClient = false;
        runningServer = false;
        serverSocket.close();
        in.close();
        clientSocket.close();
        System.out.println("Потоки закрыты");

    }
}
