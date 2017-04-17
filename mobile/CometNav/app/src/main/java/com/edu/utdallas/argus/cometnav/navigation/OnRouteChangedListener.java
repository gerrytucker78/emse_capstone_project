package com.edu.utdallas.argus.cometnav.navigation;

/**
 * Created by Daniel on 4/15/2017.
 */

public interface OnRouteChangedListener {

    /**
     * Callback for when the navigation route changes
     * @param routeArcs An array of the route arcs
     *                  e.g.2, 3, 4, 5 means the route is nodes 2-3, 3-4, and
     *                  4-5
     */
    void onRouteChange(int[] routeArcs);
}
