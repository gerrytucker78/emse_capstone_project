package com.edu.utdallas.argus.cometnav;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import net.coderodde.graph.Demo;
import net.coderodde.graph.DirectedGraph;
import net.coderodde.graph.DirectedGraphWeightFunction;
import net.coderodde.graph.UndirectedGraph;
import net.coderodde.graph.pathfinding.AbstractPathfinder;
import net.coderodde.graph.pathfinding.DirectedGraphNodeCoordinates;
import net.coderodde.graph.pathfinding.DirectedGraphPath;
import net.coderodde.graph.pathfinding.HeuristicFunction;
import net.coderodde.graph.pathfinding.support.EuclideanHeuristicFunction;
import net.coderodde.graph.pathfinding.support.NBAStarPathfinder;
import net.coderodde.graph.pathfinding.support.Point2DF;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                startActivity(new Intent(MainActivity.this, FindARoomMapsActivity.class));


            }


        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //.Demo.runTest();
        testPathfinding();
    }

    public void testPathfinding()
    {
        UndirectedGraph graph = new UndirectedGraph();
        for (int id = 1; id <= 10; ++id)
            graph.addNode(id);

        List<Integer> graphNodeList = new ArrayList<>(graph.getNodeSet());

        graph.addArc(1, 2);
        graph.addArc(2, 3);
        graph.addArc(3, 4);
        graph.addArc(1, 4);
        graph.addArc(4, 5);

        DirectedGraphNodeCoordinates coordinates = new DirectedGraphNodeCoordinates();
        coordinates.put(1, new Point2DF(0,0));
        coordinates.put(2, new Point2DF(1, 0));
        coordinates.put(3, new Point2DF(3, 0));
        coordinates.put(4, new Point2DF(4, 0));
        coordinates.put(5, new Point2DF(0, 5));

        DirectedGraphWeightFunction weightFunction =
                new DirectedGraphWeightFunction();

        for (Integer childNodeId : graph.getChildrenOf(1))
            weightFunction.put(1, childNodeId, (float)(1.2 * coordinates.get(1).distance(coordinates.get(childNodeId))));
        //increase 1's weight with 4
        weightFunction.put(1, 4, 100);

        for (Integer childNodeId : graph.getChildrenOf(2))
            weightFunction.put(2, childNodeId, (float)(1.2 * coordinates.get(1).distance(coordinates.get(childNodeId))));
        for (Integer childNodeId : graph.getChildrenOf(3))
            weightFunction.put(3, childNodeId, (float)(1.2 * coordinates.get(1).distance(coordinates.get(childNodeId))));
        for (Integer childNodeId : graph.getChildrenOf(4))
            weightFunction.put(4, childNodeId, (float)(1.2 * coordinates.get(1).distance(coordinates.get(childNodeId))));
        for (Integer childNodeId : graph.getChildrenOf(5))
            weightFunction.put(5, childNodeId, (float)(1.2 * coordinates.get(1).distance(coordinates.get(childNodeId))));

        HeuristicFunction hf = new EuclideanHeuristicFunction(coordinates);

        AbstractPathfinder pathfinder = new NBAStarPathfinder(graph,
                weightFunction,
                hf);

        Log.d("AlgoDemo", pathfinder.search(1, 5).toString());
        //Log.d("AlgoDemo", pathfinder.search(4,1).toString());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_findRoom) {
            // Handle the Find Room action
            Intent intent = new Intent(this,FindARoomMapsActivity.class);
            this.startActivity(intent);
            return true;
        } else if (id == R.id.nav_emergency) {
            Toast.makeText(this, "Emergency Navigation Clicked", Toast.LENGTH_LONG).show();
            return true;

        } else if (id == R.id.nav_reportEmergency) {
            Toast.makeText(this, "Report Emergency Clicked", Toast.LENGTH_LONG).show();
            return true;

        } else if (id == R.id.nav_manage) {
            Toast.makeText(this, "Manage Account Clicked", Toast.LENGTH_LONG).show();
            return true;

        /*} else if (id == R.id.fab) {
            Intent intent = new Intent(this,FindARoomMapsActivity.class);
            this.startActivity(intent);
            return true;
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {*/

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
