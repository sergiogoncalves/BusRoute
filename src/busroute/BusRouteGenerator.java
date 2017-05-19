package busroute;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class BusRouteGenerator {

    public static void main(String[] args) throws FileNotFoundException, IOException {
        File file = new File("C:/Users/Sergio/Documents/routes2.txt");
        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter osw = new OutputStreamWriter(fos);
        PrintWriter out = new PrintWriter(osw);
        out.println(BusRoute.BUS_ROUTES_UPPER_LIMIT);

        for (int i = 0; i < 50; i++) {

            Set< Integer> set = new HashSet<>(BusRoute.BUS_ROUTE_STATIONS_UPPER_LIMIT);

            while (set.size() < BusRoute.BUS_ROUTE_STATIONS_UPPER_LIMIT) {
                set.add((int) Math.floor(Math.random() * BusRoute.STATION_NUMBER_UPPER_LIMIT));
            }

            StringBuilder sb = new StringBuilder(String.valueOf(i));

            for (Integer stationId : set) {
                sb.append(" ");
                sb.append(stationId);
            }
            out.println(sb.toString());
        }
        out.close();
        osw.close();
        fos.close();
    }
}
