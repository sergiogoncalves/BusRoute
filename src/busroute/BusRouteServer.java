package busroute;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BusRouteServer {

    private static final int SERVER_PORT = 8088;
    private static HashMap< Integer, String> routes;

    public static void main(String[] args) {
        String fileName = args[0];
        File file = BusRoute.getFile(fileName);
        routes = BusRoute.loadRoutes(file);

        startServer();
    }

    private static void startServer() {
        try {
            ServerSocket ss = new ServerSocket(SERVER_PORT);
            System.out.println("Server running...");

            while (true) {
                Socket socket = ss.accept();
                new BusRoute(routes, socket);
            }
        } catch (IOException ex) {
            Logger.getLogger(BusRouteServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
