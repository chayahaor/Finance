package finance;

import api.API;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class PnL {
    private JFreeChart chart;
    private Connection connection;
    private API api;

    public PnL(Connection connection)
    {
        this.connection = connection;
        api = new API();
    }

    public JFreeChart getChart() throws SQLException, IOException
    {
        updatePnL();
        chart = ChartFactory.createXYLineChart(
                "Profit and Loss",
                "Date",
                "Profit/Loss",
                createDataset(),
                PlotOrientation.VERTICAL,
                true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(100, 100));

        return chart;
    }

    private void updatePnL() throws SQLException, IOException
    {
        //TODO:
        // confirm dates are sent/received in the same manner
        // decide if we want to throw exceptions in methods or put in try catch
        // Make a party if this works because it did not take long :)

        Date mostRecent = new Date(); //TODO: replace with most recent date in PnL DB
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("Call spGetMainDataByCurrency();");
        Date today = new Date();
        for (Date dayLookingAt = mostRecent;
             dayLookingAt.before(today);
             dayLookingAt = new Date(dayLookingAt.getTime() + (1000 * 60 * 60 * 24)))
        {
            while (resultSet.next())
            {
                double totalPnL = 0;
                double pnl = 0;
                Date transactionDate = resultSet.getDate("ActionDate");
                String currency = resultSet.getString("Currency");
                Date maturityDate = resultSet.getDate("MaturityDate");
                double quantity = resultSet.getDouble("Quantity");
                double forwardPrice = resultSet.getDouble("ForwardPrice");
                if (transactionDate.equals(dayLookingAt))
                {
                    String day = dayLookingAt.toString();
                    pnl = forwardPrice - Double.parseDouble(api.convert(currency, "USD", day));
                } else
                {
                    String day = new Date(dayLookingAt.getTime()
                                          + (1000 * 60 * 60 * 24)).toString();
                    pnl = Double.parseDouble(api.convert(currency, "USD", day));
                }
                double transactionPnL = pnl * quantity;
                if (maturityDate.before(dayLookingAt))
                {
                    totalPnL += transactionPnL;
                } else
                {
                    double time = 1.0; //TODO: get time as a decimal of a year
                    double rfr = 1.0; //TODO: get rfr as annual decimal
                    transactionPnL = transactionPnL / 1 / time * rfr; //TODO: confirm equation with Dr. Katz
                    totalPnL += transactionPnL;
                }
                Statement stmtInsert = connection.createStatement();
                stmtInsert.executeQuery("Call spInsertIntoPnL(" + dayLookingAt + ", " + totalPnL + ");");
            }
        }

    }

    private XYDataset createDataset() throws SQLException
    {
        XYSeries pnlData = new XYSeries("Profit and Loss");
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("Call spGetPnL();");
        while (resultSet.next())
        {
            pnlData.add(resultSet.getDate(0).getTime(), resultSet.getDouble(1));
        }

        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(pnlData);
        return dataset;
    }

}
