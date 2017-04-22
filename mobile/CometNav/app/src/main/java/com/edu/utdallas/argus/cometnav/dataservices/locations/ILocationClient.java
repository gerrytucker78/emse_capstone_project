package com.edu.utdallas.argus.cometnav.dataservices.locations;

import java.util.List;

/**
 * Created by gtucker on 4/16/2017.
 */

public interface ILocationClient {
    public void receiveNavigableLocations(List<Location> locations);

    public void receiveBlockedAreas(List<Location> locations);

    public void receivePaths(List<Path> paths);

}
