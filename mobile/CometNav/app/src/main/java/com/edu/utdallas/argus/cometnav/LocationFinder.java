package com.edu.utdallas.argus.cometnav;

import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by Daniel on 4/8/2017.
 */

public class LocationFinder
{
    private static LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();


    /**
     * Given a set of positions and distances, return the maximum likelihood point estimate of where the user is.
     * @param positions Positions. For a 2d plane need at least 3, for 3d 4.
     * @param distances Each position should correspond to a position.
     * @return The RealVector containing the X and Y coordinates.
     */
    public static RealVector getLocationPoint(double[][] positions, double[] distances)
    {
        //double[][] positions = new double[][] { { 5.0, -6.0 }, { 13.0, -15.0 }, { 21.0, -3.0 }, { 12.4, -21.2 } };
        //double[] distances = new double[] { 8.06, 13.97, 23.32, 15.31 };

        NonLinearLeastSquaresSolver solver =
                new NonLinearLeastSquaresSolver
                        (new TrilaterationFunction(positions, distances), optimizer);
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

        // the answer
        //double[] centroid = optimum.getPoint().toArray();

        // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
        //RealVector standardDeviation = optimum.getSigma(0);
        //RealMatrix covarianceMatrix = optimum.getCovariances(0);
        return optimum.getPoint();
    }

    public static RealMatrix getLocationMatrix(double[][] positions, double[] distances)
    {
        //double[][] positions = new double[][] { { 5.0, -6.0 }, { 13.0, -15.0 }, { 21.0, -3.0 }, { 12.4, -21.2 } };
        //double[] distances = new double[] { 8.06, 13.97, 23.32, 15.31 };

        NonLinearLeastSquaresSolver solver =
                new NonLinearLeastSquaresSolver
                        (new TrilaterationFunction(positions, distances), optimizer);
        LeastSquaresOptimizer.Optimum optimum = solver.solve();

        // the answer
        //double[] centroid = optimum.getPoint().toArray();

        // error and geometry information; may throw SingularMatrixException depending the threshold argument provided
        //RealVector standardDeviation = optimum.getSigma(0);
        //RealMatrix covarianceMatrix = optimum.getCovariances(0);
        return optimum.getCovariances(0);
    }
}
