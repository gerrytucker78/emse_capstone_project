package net.coderodde.graph.pathfinding.support;

import android.graphics.PointF;

public class Point2DF {

    public Point2DF()
    {
        setPoint(new PointF(0, 0));
    }

    public Point2DF(float x, float y)
    {
        setPoint(new PointF(x, y));
    }

    public float distance(Point2DF other)
    {
        return (float) Math.sqrt(Math.pow(point.x - other.getPoint().x, 2) + Math.pow(point.y - other.getPoint().y, 2));
    }

    public float getX()
    {
        return point.x;
    }

    public void setX(float x) { point.set(x, point.y); }

    public float getY()
    {
        return point.y;
    }

    public void setY(float y) { point.set(point.x, y); }

    private PointF getPoint() {
        return point;
    }

    private void setPoint(PointF point) {
        this.point = point;
    }

    private PointF point;

}
