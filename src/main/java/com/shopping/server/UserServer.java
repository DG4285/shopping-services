package com.shopping.server;

import com.shopping.service.UserServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserServer {
    private static final Logger logger = Logger.getLogger(UserServer.class.getName());
    private Server server;

    public void startServer() {
        int port = 50051;
        try {
            server = ServerBuilder.forPort(port)
                    .addService(new UserServiceImpl())
                    .build()
                    .start();

            logger.info("Server started on port " + port);
            /**
             once the server started, killed the JVM: Server also need a clean shut down along with that JVM kill.
             that's why we add this hook
             */

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    logger.info("Clean Server shutdown in case JVM was shutdown");
                    try {
                        UserServer.this.stopServer();
                    } catch (InterruptedException e) {
                        logger.log(Level.SEVERE, "Server Shutdown Interrupted", e);
                    }
                }
            });
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server did not start", e);
        }
    }

    public void stopServer() throws InterruptedException {
        if (server != null) {
            server.awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * to keep running the server
     *
     * @throws InterruptedException
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        UserServer userServer = new UserServer();
        userServer.startServer();
        userServer.blockUntilShutdown();
    }
}
