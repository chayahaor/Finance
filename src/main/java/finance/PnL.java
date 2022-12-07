package finance;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class PnL
{
    private final JFreeChart chart;

    public PnL()
    {
        chart = ChartFactory.createXYLineChart(
                "Profit and Loss",
                "Date",
                "Profit/Loss",
                createDataset(),
                PlotOrientation.VERTICAL,
                true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(100, 100));
    }

    public JFreeChart getChart()
    {
        return chart;
    }

    private XYDataset createDataset()
    {
        //TODO: replace with DB call
        final XYSeries xy1 = new XYSeries("Series 1");
        xy1.add(1.0, 1.0);
        xy1.add(2.0, -1.0);
        xy1.add(3.0, 3.0);

        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(xy1);
        return dataset;
    }

}
