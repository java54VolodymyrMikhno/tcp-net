package telran.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

public class TcpServer implements Runnable {
    Protocol protocol;
    int port;
    boolean running = true;
    ServerSocket serverSocket;
    List<TcpClientServerSession> sessionsList = new LinkedList<>();

    public TcpServer(Protocol protocol, int port) {
        this.protocol = protocol;
        this.port = port;
    }

    public void shutdown() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        for (TcpClientServerSession session : sessionsList) {
             session.shutdown();
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setSoTimeout(60000);

            System.out.println("Server is listening on port " + port);

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    TcpClientServerSession session = new TcpClientServerSession(socket, protocol);
                    sessionsList.add(session);
                    session.start();
                } catch (SocketTimeoutException e) {
                    if (!running) {
                        System.out.println("Server socket closed due to shutdown.");
                    } else {
                        System.out.println("Server socket accept timed out.");
                    }
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            if (running) {
                e.printStackTrace();
            } else {
                System.out.println("Server socket closed.");
            }
        }
    }

    public void removeSession(TcpClientServerSession session) {
        sessionsList.remove(session);
    }
}
