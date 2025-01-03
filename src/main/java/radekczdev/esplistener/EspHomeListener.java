package radekczdev.esplistener;

import radekczdev.esplistener.protos.EspHomeProtos;
import radekczdev.esplistener.protos.EspHomeProtos.HelloRequest;
import radekczdev.esplistener.protos.EspHomeProtos.HelloResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import static java.util.logging.Level.*;
import static radekczdev.esplistener.protos.EspHomeProtos.MessageType.HELLO_REQUEST;

public class EspHomeListener {

    private static final Logger logger = Logger.getLogger(EspHomeListener.class.getName());

    public static void main(String[] args) {
        String deviceIp = System.getenv("ESPDEVICEHOSTNAME");
        int devicePort = Integer.parseInt(System.getenv("ESPDEVICEPORT"));
        String devicePassword = System.getenv("ESPDEVICEPASS"); // Replace with your actual password

        EspHomeConnection client = new EspHomeConnection(deviceIp, devicePort, devicePassword);
        connect(client);
    }

    public static void sendMessage(EspHomeProtos.MessageType messageType, byte[] message, SocketChannel socketChannel) throws IOException {
        final byte ZERO_BYTE_ESP_NATIVE_API = 0;
        ByteBuffer out = ByteBuffer
                .allocate(1024)
                .clear()
                .put(ZERO_BYTE_ESP_NATIVE_API)
                .put((byte) message.length)
                .put((byte) messageType.getNumber())
                .put(message)
                .flip();
        socketChannel.write(out);
    }

    public static String receiveMessage(final SocketChannel socketChannel) throws IOException {
        ByteBuffer in = ByteBuffer.allocate(512).clear();
        socketChannel.read(in);
        in.position(1); // Skipped, as custom NATIVE API ESPHOME protocol sends empty byte at the beginning

        StringBuilder text = new StringBuilder();

        int charRead;
        while ((charRead = (char) in.get()) != 0) {
            byte byteRead = (byte) charRead;
            text.append((char) byteRead);
        }

//        if (in.hasRemaining() && in.get() != 0) { // needs to be rethinked - what if message will be longer than 512 bytes? reacusion may not work
//            text.append(receiveMessage(socketChannel));
//        }

        return text.toString();
    }

    public static void connect(EspHomeConnection client) {
        try (SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(client.hostname(), client.port()))) {

            logger.log(INFO, "Connected to the server at %s:%s".formatted(client.hostname(), client.port()));
            do {
                final HelloRequest build1 = HelloRequest
                        .newBuilder()
                        .setClientInfo("testPc")
                        .setApiVersionMajor(4)
                        .setApiVersionMinor(29).build();
                sendMessage(HELLO_REQUEST, build1.toByteArray(), socketChannel);
                final HelloResponse helloResponse = HelloResponse.parseFrom(receiveMessage(socketChannel).getBytes());
                logger.log(INFO, "Received response: %s".formatted(helloResponse));
//                final EspHomeProtos.ConnectRequest connectRequest = EspHomeProtos.ConnectRequest
//                        .newBuilder()
//                        .setPassword(password)
//                        .build();
//                sendMessage(EspHomeProtos.MessageType.CONNECT_REQUEST, connectRequest.toByteArray(), out);
//             final EspHomeProtos.ConnectResponse connectResponse = EspHomeProtos.ConnectResponse
//                 .parseFrom(receiveMessage(in));
//                logger.log(INFO, "Received response: " + connectResponse);

            } while (shouldContinue());

        } catch (IOException e) {
            logger.log(SEVERE, "Error connecting to the server: %s".formatted(e.getMessage()));
        }
    }

    private static boolean shouldContinue() throws IOException {
        logger.log(WARNING, "Do you want to continue? (y/n): ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = reader.readLine();
        return input.equalsIgnoreCase("y");
    }
}