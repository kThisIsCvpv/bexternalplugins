package com.kthisiscvpv.socket.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.kthisiscvpv.socket.client.RLClient;
import com.kthisiscvpv.util.logger.AbstractLogger;
import com.kthisiscvpv.util.logger.AbstractLogger.Level;

public class RLServerDoctor implements Runnable {

	// A RLClient is pronounced dead if it hasn't responded within DEADLOCK_TIME milliseconds.
	public static final long DEADLOCK_TIME = 60000L;

	private RLServer server;
	private AbstractLogger logger;

	public RLServerDoctor(RLServer server) {
		this.server = server;
		this.logger = server.getLogger();
	}

	@Override
	public void run() {
		while (true) {
			try {
				HashSet<RLClient> clients = server.getClients();
				List<RLClient> clientsClone;
				synchronized (clients) {
					clientsClone = new ArrayList<RLClient>(clients);
				}

				for (RLClient client : clientsClone) {
					long elapsedTime = System.currentTimeMillis() - client.getLastMessageEpoch();
					if (elapsedTime >= DEADLOCK_TIME) {
						logger.println(RLServerDoctor.class, Level.INFO, client.getIPAddress() + " has stopped responding after " + elapsedTime + "ms and will be terminated");
						client.terminate();
					}
				}

				Thread.sleep(1000L);
			} catch (Exception e) {
				logger.println(RLServerDoctor.class, Level.FATAL, "Unable to start the maintain the integrity checks");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
