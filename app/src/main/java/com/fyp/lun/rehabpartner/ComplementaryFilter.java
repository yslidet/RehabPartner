package com.fyp.lun.rehabpartner;


/**
 * Created by Lun on 30/1/2017.
 */

public class ComplementaryFilter
{
    /* Setting up of constants */
    double tau = 0.1, dt = 1/10;
    double alpha = tau/(tau+dt);

    // Main function to call all sub functions to calculate angles using ComplementaryFilter
    public double[] process(double[] accel, double[] prevAccel, double[] magnet, double[] gyro,  double[] prevResults, double[]prevHPFResults)
    {
        double[] LPFResults = new double[3];
        double[] estResults = new double[3];
        double[] HPFResults = new double[3];
        double[] XYZResults = new double[3];
        double[] compiledResults = new double[9];
        int j = 0, k = 0;

        LPFResults = LPFCalculations(accel, prevAccel);
        estResults = estimateXYZ(LPFResults, magnet);
        HPFResults = HPFCalculations(gyro, prevResults, prevHPFResults);
        XYZResults = complement(HPFResults, estResults);

        // storing first 3 results of x,y,z CF angle
        for(int i = 0; i < XYZResults.length; i++){
            compiledResults[i] = XYZResults[i];
        }
        // storing next 3 results of high pass filter for next processing
        for(int i = 3; i < (HPFResults.length + 3); i++) {
            compiledResults[i] = HPFResults[j];
            j++;
        }
        // storing next 3 results of low pass filter for next processing
        for(int i = 6; i < (LPFResults.length + 3); i++)
        {
            compiledResults[i] = LPFResults[k];
            k++;
        }
        return compiledResults;
    }

    // to change android's data to be the same axis as XSEN's data
    public double[] android2XSEN(double dataX, double dataY, double dataZ) {
        double[] swap = new double[3];
        swap[0] = dataY;
        swap[1] = dataX;
        swap[2] = -dataZ;
        return swap;
    }

    // Normalizing and low-pass filtering of accelerometer's data
    private double[] LPFCalculations(double[] accel, double[] prevAccel) {
        double acc_norm, accX, accY, accZ;
        double[] results = new double[3];

        // Normalizing accelerometer's data
        //acc_norm = Math.sqrt(Math.pow(accel[0],2) + Math.pow(accel[1],2) + Math.pow(accel[2],2));
        acc_norm = Math.sqrt( (accel[0]*accel[0]) + (accel[1]*accel[1]) + (accel[2]*accel[2]));
        // To prevent divide by zero error
        if (accel[0] == 0.0) {
            accX = 0.0;
        }
        else
        {
            accX = accel[0] / acc_norm;
        }

        if (accel[1] == 0.0)
        {
            accY = 0.0;
        }
        else
        {
            accY = accel[1] / acc_norm;
        }

        if (accel[2] == 0.0)
        {
            accZ = 0.0;
        }
        else
        {
            accZ = accel[2] / acc_norm;
        }

        // Low-pass filtering
        results[0] = prevAccel[0] + alpha * (accX - prevAccel[0]);
        results[1] = prevAccel[1] + alpha * (accY - prevAccel[1]);
        results[2] = prevAccel[2] + alpha * (accZ - prevAccel[2]);

        return results;
    }

    // Normalizing and estimating of roll, pitch and yaw angle with magnetometer's data
    private double[] estimateXYZ(double[] LPFResults, double[] magnet) {
        double mag_norm, magX, magY, magZ;
        double[] estimateXYZ = new double[3]; // The estimate of Roll, Pitch and Yaw


        // Normalizing magnetometer's data
        //mag_norm = Math.sqrt(Math.pow(magnet[0],2) + Math.pow(magnet[1],2) + Math.pow(magnet[2],2));
        mag_norm = Math.sqrt((magnet[0]*magnet[0]) + (magnet[1]*magnet[1]) + (magnet[2]*magnet[2]));
        // To prevent divide by zero error
        if (magnet[0] == 0.0) {
             magX = 0.0;
        }
        else
        {
            magX = magnet[0] / mag_norm;
        }

        if (magnet[1] == 0.0)
        {
            magY = 0.0;
        }
        else
        {
            magY = magnet[1] / mag_norm;
        }

        if (magnet[2] == 0.0)
        {
            magZ = 0.0;
        }
        else
        {
            magZ = magnet[2] / mag_norm;
        }

        // Roll estimate
        //estimateXYZ[0] = Math.atan2(LPFResults[1], Math.sqrt(Math.pow(LPFResults[0],2) + Math.pow(LPFResults[2],2)));
        estimateXYZ[0] = Math.atan2(LPFResults[1], Math.sqrt((LPFResults[0]*LPFResults[0]) + (LPFResults[2]*LPFResults[2])));
        // Pitch estimate
        //estimateXYZ[1] = Math.atan2(-LPFResults[0], Math.sqrt(Math.pow(LPFResults[1],2) + Math.pow(LPFResults[2],2)));
        estimateXYZ[1] = Math.atan2(-LPFResults[0], Math.sqrt((LPFResults[1]*LPFResults[1]) + (LPFResults[2]*LPFResults[2])));
        // Yaw estimate
        estimateXYZ[2] = Math.atan2( (-magY * Math.cos(estimateXYZ[0]) + magZ * Math.sin(estimateXYZ[0])),
                magX * Math.cos(estimateXYZ[1]) + magZ * Math.sin(estimateXYZ[1]) * Math.sin(estimateXYZ[0]) + magZ * Math.sin(estimateXYZ[1]) * Math.cos(estimateXYZ[0]));

        return estimateXYZ;
    }

    // Integration and High-pass filtering of gyroscope's data
    private double[] HPFCalculations(double[] gyro, double[] prevResults, double[] prevHPFResults) {
        double[] HPFResults = new double[3];

        // Integrating gyroscope
        gyro[0] = prevResults[0] + gyro[0] * dt;
        gyro[1] = prevResults[1] + gyro[1] * dt;
        gyro[2] = prevResults[2] + gyro[2] * dt;

        //High-pass filter
        HPFResults[0] = alpha * prevHPFResults[0] + (1-alpha) * gyro[0];
        HPFResults[1] = alpha * prevHPFResults[1] + (1-alpha) * gyro[1];
        HPFResults[2] = alpha * prevHPFResults[2] + (1-alpha) * gyro[2];

        return HPFResults;
    }

    // Complement part of Complementary Filter
    private double[] complement(double[] HPFResults, double[] estimateXYZ) {
        double[] results = new double[3];

        results[0] = 0.19 * HPFResults[0] + 0.81 * estimateXYZ[0];
        results[1] = 0.19 * HPFResults[1] + 0.81 * estimateXYZ[1];
        results[2] = 0.81 * HPFResults[2] + 0.19 * estimateXYZ[2];

        return results;
    }

}
