package telran.net;

import java.io.*;
import java.net.*;

import static telran.net.TcpConfigurationProperties.*;

public class TcpClientServerSession implements Runnable {
	Socket socket;
	Protocol protocol;
	TcpServer tcpServer;

	public TcpClientServerSession(Socket socket, Protocol protocol, TcpServer tcpServer) {
		this.socket = socket;
		this.protocol = protocol;
		this.tcpServer = tcpServer;
	}

	@Override
	public void run() {
		try (BufferedReader receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintStream sender = new PrintStream(socket.getOutputStream())) {
			String line = null;
			boolean sessionRunning = true;
			socket.setSoTimeout(SOCKET_TIMEOUT);
			long idleTime = 0;
			while (tcpServer.running && sessionRunning) {
				try {
					line = receiver.readLine();
					if (line == null) {
						break;
					}
					String responseStr = protocol.getResponseWithJSON(line);
					sender.println(responseStr);
					idleTime = 0;
				} catch (SocketTimeoutException e) {
					idleTime += SOCKET_TIMEOUT;
					if (idleTime > SESSION_IDLE_TIMEOUT) {
						sessionRunning = false;
					}
				}
			}
			socket.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
