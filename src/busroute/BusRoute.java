package busroute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BusRoute implements Runnable {

    public static int BUS_ROUTES_UPPER_LIMIT = 100000;
    public static int STATION_NUMBER_UPPER_LIMIT = 1000000;
    public static int BUS_ROUTE_STATIONS_UPPER_LIMIT = 1000;

    private HashMap<Integer, String> routes;
    private final Socket socket;

    public BusRoute(HashMap<Integer, String> routes, Socket socket) {

        this.routes = routes;
        this.socket = socket;

        new Thread(this).start();
    }

    @Override
    public void run() {
        try {

            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            OutputStream os = socket.getOutputStream();
            PrintWriter out = new PrintWriter(os);
            String row = br.readLine();

            if (row != null) {

                String[] command = row.split(" ");

                if ("GET".equals(command[0])) {

                    String[] queryString = command[1].split("\\?");

                    if ("/api/direct".equals(queryString[0])) {
                        doGet(out, command[1].split("\\?")[1]);
                    } else {
                        doError(out);
                    }
                } else {
                    doError(out);
                }
                out.close();
            }
            os.close();
            br.close();
            isr.close();
            is.close();
            socket.close();

        } catch (IOException ex) {
            Logger.getLogger(BusRoute.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean existsDirectBusRoute(String depSid, String arrSid) {

        String _depSid = " " + depSid + " ";
        String _arrSid = " " + arrSid + " ";
        String _arrSidEnd = " " + arrSid;
        int indexOfSid;

        for (String route : routes.values()) {
            indexOfSid = route.indexOf(_depSid);
            if (indexOfSid == -1) {
                continue;
            }
            if ((route.indexOf(_arrSid, indexOfSid + 1) != -1)
                    || route.endsWith(_arrSidEnd)) {
                return true;
            }
        }

        return false;
    }

    public static File getFile(String fileName) {
        File file = new File(fileName);

        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Invalid file: " + fileName);
        }

        return file;
    }

    public static HashMap<Integer, String> loadRoutes(File file) {
        FileReader fr;
        HashMap<Integer, String> result = null;

        try {
            fr = new FileReader(file);

            BufferedReader br = new BufferedReader(fr);

            String row = br.readLine();
            int qtyRoutes = Integer.parseInt(row);
            result = new HashMap<>(qtyRoutes);

            if (qtyRoutes < 0 || qtyRoutes > BUS_ROUTES_UPPER_LIMIT) {
                throw new IllegalArgumentException("Number of bus routes out of bounds.");
            }

            while ((row = br.readLine()) != null) {
                addRoute(result, row);
            }

            fr.close();

            if (qtyRoutes != result.size()) {
                throw new IllegalArgumentException("The number of bus routes is incorrect.");
            }

            return result;

        } catch (FileNotFoundException ex) {
            Logger.getLogger(BusRouteServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BusRouteServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private static void addRoute(HashMap<Integer, String> routes, String row) {
        int index = row.indexOf(" ");
        Integer routeId = Integer.valueOf(row.substring(0, index));
        String stationIds = row.substring(index + 1);
        String[] stationIdsArr = stationIds.split(" ");
        Set<String> stationIdsSet = new HashSet<>(Arrays.asList(stationIdsArr));

        if (stationIdsArr.length > BUS_ROUTE_STATIONS_UPPER_LIMIT) {
            throw new IllegalArgumentException("There is a route with more than 1000 stations.");
        }

        if (stationIdsSet.size() != stationIdsArr.length) {
            throw new IllegalArgumentException("There is a bus route with duplicated station: " + routeId);
        }

        for (String stationId : stationIdsSet) {
            int stationIdInt = Integer.parseInt(stationId);
            if (stationIdInt < 0 || stationIdInt >= STATION_NUMBER_UPPER_LIMIT) {
                throw new IllegalArgumentException("Invalid bus station ID: " + stationId);
            }
        }
        routes.put(routeId, " " + stationIds);
    }

    public void doGet(PrintWriter out, String queryString) {
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/javascript");
        out.println("");
        out.println("{");

        Map<String, String> params = getParams(queryString);
        String depSid = params.get("dep_sid");
        String arrSid = params.get("arr_sid");
        out.println("    \"dep_sid\": " + depSid + ",");
        out.println("    \"arr_sid\": " + arrSid + ",");
        out.println("    \"direct_bus_route\": " + existsDirectBusRoute(depSid, arrSid));

        out.println("}");
        out.flush();
    }

    public void doError(PrintWriter out) {
        out.println("HTTP/1.1 404 OK");
        out.println("Content-Type: text/html");
        out.println("");
        out.flush();
    }

    private Map<String, String> getParams(String queryString) {
        Map<String, String> result = new HashMap<>();
        String[] params = queryString.split("&");

        for (String param : params) {
            String[] keyValue = param.split("=");
            result.put(keyValue[0], keyValue[1]);
        }

        return result;
    }

}
