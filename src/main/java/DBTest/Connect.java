package DBTest;

import java.sql.*;

public class Connect {

    public static void main(String[] args) {
        String dbName = "finance";
        try
        {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/"+dbName, "root", "");
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("Select * from action");
            while (resultSet.next())
            {
                System.out.println(resultSet.getString(1) + " " + resultSet.getString(2));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
