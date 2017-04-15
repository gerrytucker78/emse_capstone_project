package com.edu.utdallas.argus.cometnav;

import org.json.JSONArray;
import org.junit.Test;

import java.util.Map;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;

/**
 * Created by gtucker on 3/25/2017.
 */

public class DataServicesClientTest {

@Test
public void getEmergencies() throws Exception {
    EmergencyClient ec = new EmergencyClient();

    JSONArray origEmergencies = new JSONArray("[{\"emergency_id\":1,\"location_id\":1,\"emergency_type\":\"FIRE\",\"emergency_notes\":\"We don't need no water ...\",\"emergency_start\":\"2018-03-25T14:56:59.301Z\",\"emergency_last_update\":\"2018-03-25T15:56:00.000Z\",\"emergency_end\":null},{\"emergency_id\":2,\"location_id\":3,\"emergency_type\":\"WATER\",\"emergency_notes\":\"Looking for a fire ....\",\"emergency_start\":\"2018-03-28T21:56:59.301Z\",\"emergency_last_update\":\"2018-03-28T22:56:00.000Z\",\"emergency_end\":\"2018-03-28T22:56:00.000Z\"}]");
    ec.receiveEmergencies(origEmergencies);

    Map<Integer, Emergency> emergencies = ec.getEmergenciesMap();
    assertEquals("Emergencies",2,emergencies.size());
}

    @Test
    public void getAllLocations() throws Exception {
        //DataServices services = new DataServices();
        //services.getLocations();
        //assertEquals(4, 2 + 2);
    }

}
