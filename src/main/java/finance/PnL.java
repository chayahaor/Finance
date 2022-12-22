package finance;

import api.API;
import org.jfree.chart.*;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class PnL {
    private JFreeChart chart;
    private Connection connection;
    private API api;

    public PnL(Connection connection) {
        this.connection = connection;
        api = new API();
    }

    public JFreeChart getChart() throws SQLException {
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

    private void updatePnL() throws SQLException {
        Date mostRecent = new Date(); //TODO: replace with most recent date in PnL DB
        Statement stmt = connection.createStatement();
        ResultSet resultSet = stmt.executeQuery("Call spGetMainDataByCurrency();");
        Date today = new Date();
        for (Date dayLookingAt = mostRecent; dayLookingAt < today; dayLookingAt++)
            while (resultSet.next())
            {
                double totalPnL = 0;
                double pnl = 0;
                Date transactionDate = resultSet.getDate(0); //TODO: confirm start with 0/1
                String currency = resultSet.getString(2); //TODO: confirm start with 0/1
                Date maturityDate = resultSet.getDate(3); //TODO: confirm start with 0/1
                double quantity = resultSet.getDouble(4); //TODO: confirm start with 0/1
                double forwardPrice = resultSet.getDouble(5); //TODO: confirm start with 0/1
                if (transactionDate.equals(dayLookingAt))
                {
                    pnl = forwardPrice - api.convert(currency, "USD", dayLookingAt);
                } else
                {
                    pnl = api.convert(currency, "USD", dayLookingAt, dayLookingAt - 1);
                }
                double transactionPnL = pnl * quantity;
                if (maturityDate < dayLookingAt)
                {
                    totalPnL += transactionPnL;
                } else
                {
                    transactionPnL = transactionPnL / 1 / time * rfr;
                    totalPnL += transactionPnL;
                }
                pnlTableInDB.add(totalPnL, dayLookingAt);
            }

    }

    private XYDataset createDataset() throws SQLException {
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
