package telran.net;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static telran.net.TcpConfigurationProperties.*;

public class TcpServer implements Runnable {
	Protocol protocol;
	int port;
	boolean running = true;
	ExecutorService executor;

	public TcpServer(Protocol protocol, int port) {
		this.protocol = protocol;
		this.port = port;
		this.executor = Executors.newFixedThreadPool(getNumberOfThreads());
	}

	private int getNumberOfThreads() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.availableProcessors();
	}

	public void shutdown() {
		running = false;
		executor.shutdown();

	}

	@Override
	public void run() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {

			System.out.println("Server is listening on port " + port);
			serverSocket.setSoTimeout(SOCKET_TIMEOUT);
			while (running) {
				try {
					Socket socket = serverSocket.accept();
					executor.execute(new TcpClientServerSession(socket, protocol, this));

				} catch (SocketTimeoutException e) {

				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
