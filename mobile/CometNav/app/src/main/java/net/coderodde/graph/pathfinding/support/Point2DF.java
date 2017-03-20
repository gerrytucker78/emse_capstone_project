package net.coderodde.graph.pathfinding.support;

import android.graphics.PointF;

public class Point2DF {

    public Point2DF()
    {
        this.setPoint(new PointF(0, 0));
    }

    public Point2DF(float x, float y)
    {
        this.setPoint(new PointF(x, y));
    }

    public float distance(Point2DF other)
    {
        return (float) Math.sqrt(Math.pow(this.point.x - other.getPoint().x, 2) + Math.pow(this.point.y - other.getPoint().y, 2));
    }

    private PointF getPoint() {
        return point;
    }

    private void setPoint(PointF point) {
        this.point = point;
    }

    private PointF point;

}
