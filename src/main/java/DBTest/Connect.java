package DBTest;

import java.sql.*;

public class Connect {

    public static void main(String[] args) {
        String dbName = "finance";
        int portNumber = 3306;
        try
        {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:" + portNumber + "/" + dbName, "root", "");
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("Select * from action");
            while (resultSet.next())
            {
                System.out.println(resultSet.getString(1) + " " + resultSet.getString(2));
            }

            // NOTE:
            // Strings MUST be enclosed in ''
            // DateTime MUST be in this format 'YYYY-MM-DD hh:mm:ss'
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
