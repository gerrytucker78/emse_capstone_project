package com.edu.utdallas.argus.cometnav;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by gtucker on 3/25/2017.
 */

public class DataServicesClientTest {



    @Test
    public void getAllLocations() throws Exception {
        DataServicesClient client = new DataServicesClient();
        client.getLocations();
        assertEquals(4, 2 + 2);
    }

}
