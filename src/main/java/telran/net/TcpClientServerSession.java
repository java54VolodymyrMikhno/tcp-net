package telran.net;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TcpClientServerSession extends Thread {
    private Socket socket;
    private Protocol protocol;
    private boolean running = true;
    private static final int IDLE_TIMEOUT = 60000;

    public TcpClientServerSession(Socket socket, Protocol protocol) {
        this.socket = socket;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             PrintWriter writer = new PrintWriter(outputStream, true)) {

            socket.setSoTimeout(IDLE_TIMEOUT);

            while (running && !socket.isClosed()) {
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        running = false;
                    } else {
                        String response = protocol.getResponseWithJSON(line);
                        writer.println(response);
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Client idle for more than 1 minute, closing connection.");
                    running = false;
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        e.printStackTrace();
                    }
                    running = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        close();
    }

    public void shutdown() {
        running = false;
        close();
    }

    private void close() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
