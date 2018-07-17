package com.fyp.lun.rehabpartner;

import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    /* Variables */
    private LinearLayout chartLyt;
    private Button btnKneeExt;
    private String[] array;
    int haveFileFlag = 1;
    String finalString = "";
    ArrayList<Double> minAngle = new ArrayList<Double>();
    ArrayList<Double> maxAngle = new ArrayList<Double>();
    ArrayList<String> date = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Layout Initialization */
        chartLyt = (LinearLayout) findViewById(R.id.chart);
        btnKneeExt = (Button) findViewById(R.id.kneeExtBtn);

        /* Load metadata */
        Load();

        // If metadata exists
        if(haveFileFlag == 1) {
            int flag = 0, minIndex = 0, maxIndex = 0, dateIndex = 0;
            // Extract all data and store into a single long string
            for (int i = 0; i < array.length; i++) {
                finalString += array[i] + System.getProperty("line.separator");
            }
            // Eliminate all the commas from .csv file and store in data list
            List<String> data = Arrays.asList(finalString.split("\\s*,\\s*"));

            for (int j = 0; j < (data.size() / 8); j++) {
                // first extraction from file
                if (flag == 0) {
                    minIndex = 5;
                    maxIndex = 6;
                    date.add(data.get(dateIndex));
                    minAngle.add(Double.parseDouble(data.get(minIndex)));
                    maxAngle.add(Double.parseDouble(data.get(maxIndex)));
                    flag = 1;
                }
                // 2nd extraction onwards will give us the index of min, max and date to be a counter of 8
                else
                {
                    minIndex += 8;
                    maxIndex += 8;
                    dateIndex += 8;
                    date.add(data.get(dateIndex));
                    minAngle.add(Double.parseDouble(data.get(minIndex)));
                    maxAngle.add(Double.parseDouble(data.get(maxIndex)));
                }
            }
        }


        /* Creating the graph */
        createTempGraph();

        /* Button Listener */
        btnKneeExt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, SelectionPage.class));
            }
        });
    }

    // Load metadata
    public void Load()
    {
        // Getting file location of application
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File dir = contextWrapper.getExternalFilesDir("MyFileStorage");
        File file = new File(dir, "MetaData.txt");

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            // indicates no file found
            haveFileFlag = 0;
        }

        // if file exists, store data into array
        if(haveFileFlag == 1)
        {
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String test;
            int count=0;
            try {
                while ((test=br.readLine()) != null) {
                    count++;
                }
            }

            catch (IOException e) {
                e.printStackTrace();
            }

            try {
                fis.getChannel().position(0);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            array = new String[count];
            String line;
            int i = 0;
            try
            {
                while((line=br.readLine())!=null)
                {
                    array[i] = line;
                    i++;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void createTempGraph()
    {
        /* Variables initialization */
        String [] chartLegend = new String[] {"Min Angle", "Max Angle"};
        List<double[]> values = new ArrayList<double[]>();
        List<double[]> x = new ArrayList<double[]>();

        // If metadata is present
        if(haveFileFlag == 1)
        {
            double[] tempMinBuffer = new double[minAngle.size()];
            double[] tempMaxBuffer = new double[maxAngle.size()];
            double[] tempDateBuffer = new double[date.size()];

            for(int i = 0; i < minAngle.size(); i++)
            {
                tempDateBuffer[i] = (1+i);
                tempMinBuffer[i] = minAngle.get(i);
                tempMaxBuffer[i] = maxAngle.get(i);
            }

            // adding X values twice so that both min and max angles have same X values.
            x.add(tempDateBuffer);
            x.add(tempDateBuffer);
            values.add(tempMinBuffer);
            values.add(tempMaxBuffer);
        }

        // setting up chart details
        int[] colors = new int[] { Color.YELLOW, Color.RED};
        PointStyle[] styles = new PointStyle[] { PointStyle.CIRCLE, PointStyle.CIRCLE};
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(2);
        setRenderer(renderer, colors, styles);


        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
            r.setLineWidth(3f);
        }

        renderer.setApplyBackgroundColor(true);
        renderer.setBackgroundColor(Color.BLACK);

        renderer.setAxesColor(Color.LTGRAY);
        renderer.setPointSize(10);
        renderer.setChartTitle("Progress Chart by Days");
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Paint.Align.CENTER);
        renderer.setYLabelsAlign(Paint.Align.CENTER);


        XYMultipleSeriesDataset dataset = buildDataset(chartLegend, x, values);
        GraphicalView chartView = ChartFactory.getLineChartView(getBaseContext(), dataset, renderer);
        chartLyt.addView(chartView,0);

    }

    // Setting the chart details
    protected void setRenderer(XYMultipleSeriesRenderer renderer, int[]colors, PointStyle[] styles)
    {
        renderer.setAxisTitleTextSize(20);
        renderer.setChartTitleTextSize(50);
        renderer.setLabelsTextSize(30);
        renderer.setLegendTextSize(25);
        renderer.setPointSize(5f);
        renderer.setMargins(new int[] {20,30,15,20});
        int length = colors.length;
        for(int i =0; i < length; i++)
        {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setColor(colors[i]);
            r.setPointStyle(styles[i]);
            renderer.addSeriesRenderer(r);
        }
    }

    protected XYMultipleSeriesDataset buildDataset(String[] titles, List<double[]> xValues, List<double[]> yValues)
    {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        addXYSeries(dataset, titles, xValues, yValues, 0);
        return dataset;
    }

    // Function to add X and Y axis data
    protected void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<double[]> xValues, List<double[]> yValues, int scale)
    {
        int length = titles.length;
        for(int i = 0; i < length; i++)
        {
            XYSeries series = new XYSeries(titles[i], scale);

            // if there is data from MetaData
            if(!xValues.isEmpty())
            {
                double[] xV = xValues.get(i);
                double[] yV = yValues.get(i);
                int seriesLength = xV.length;
                for(int k = 0; k < seriesLength; k++)
                {
                    series.add(xV[k], yV[k]);
                }
                dataset.addSeries(series);
            }
            else
            {
                // No metadata set all values to 0
                double[] xV = {0};
                double[] yV = {0};
                int seriesLength = xV.length;
                for(int k = 0; k < seriesLength; k++)
                {
                    series.add(xV[k], yV[k]);
                }
                dataset.addSeries(series);
            }


        }
    }
}
