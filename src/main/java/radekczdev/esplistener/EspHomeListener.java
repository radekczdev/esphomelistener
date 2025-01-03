package radekczdev.esplistener;

import radekczdev.esplistener.protos.EspHomeProtos;
import radekczdev.esplistener.protos.EspHomeProtos.HelloRequest;
import radekczdev.esplistener.protos.EspHomeProtos.HelloResponse;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

import static java.util.logging.Level.*;
import static radekczdev.esplistener.protos.EspHomeProtos.MessageType.HELLO_REQUEST;

public class EspHomeListener {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final String serverIp;
    private final int serverPort;
    private final String password;

    public EspHomeListener(String serverIp, int serverPort, String password) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.password = password;
    }

    public static void main(String[] args) {
        String deviceIp = System.getenv("ESPDEVICEIP");
        int devicePort = Integer.parseInt(System.getenv("ESPDEVICEPORT"));
        String devicePassword = System.getenv("ESPDEVICEPASS"); // Replace with your actual password

        EspHomeListener client = new EspHomeListener(deviceIp, devicePort, devicePassword);
        client.connect();
    }

    public void sendMessage(EspHomeProtos.MessageType messageType, byte[] message, DataOutputStream out) throws IOException {
        final int ZERO_BYTE_ESP_NATIVE_API = 0;
        out.write(ZERO_BYTE_ESP_NATIVE_API);
        out.write(message.length);
        out.write(messageType.getNumber());
        out.write(message);
        out.flush();
    }

    public byte[] receiveMessage(DataInputStream in) throws IOException {
        int charRead;
        StringBuilder text = new StringBuilder();
        while ((charRead = in.read()) != 0) {
            byte byteRead = (byte) charRead;
            text.append((char) byteRead);
        }

        return text.toString().getBytes();
    }

    public void connect() {

        // convert to SocketChannel
        try (Socket socket = new Socket(serverIp, serverPort);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            logger.log(INFO, "Connected to the server at %s:%s".formatted(serverIp, serverPort));
            while (shouldContinue()) {
                final HelloRequest build1 = HelloRequest
                        .newBuilder()
                        .setClientInfo("testPc")
                        .setApiVersionMajor(4)
                        .setApiVersionMinor(29).build();
                sendMessage(HELLO_REQUEST, build1.toByteArray(), out);
                final HelloResponse helloResponse = HelloResponse.parseFrom(receiveMessage(in));
                logger.log(INFO, "Received response: %s".formatted(helloResponse));
//                final EspHomeProtos.ConnectRequest connectRequest = EspHomeProtos.ConnectRequest
//                        .newBuilder()
//                        .setPassword(password)
//                        .build();
//                sendMessage(EspHomeProtos.MessageType.CONNECT_REQUEST, connectRequest.toByteArray(), out);
//             final EspHomeProtos.ConnectResponse connectResponse = EspHomeProtos.ConnectResponse
//                 .parseFrom(receiveMessage(in));
//                logger.log(INFO, "Received response: " + connectResponse);

            }

        } catch (IOException e) {
            logger.log(SEVERE, "Error connecting to the server: %s".formatted(e.getMessage()));
        }
    }

    private boolean shouldContinue() throws IOException {
        logger.log(WARNING, "Do you want to continue? (y/n): ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = reader.readLine();
        return input.equalsIgnoreCase("y");
    }
}