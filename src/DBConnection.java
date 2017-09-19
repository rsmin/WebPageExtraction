import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    private final String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private final String dbURL = "jdbc:sqlserver://221.181.73.44:1433;DatabaseName=Chemist";
    private final String userName = "xiaodong";
    private final String password = "Tsc12345";
    String delimiter = "。";
    Connection dbConn;

    public DBConnection() {
        try {
            Class.forName(driverName);
            dbConn = DriverManager.getConnection(dbURL, userName, password);
            Log.getInstance().writeLog("successed connection to DB");
        } catch (Exception e) {
            Log.getInstance().writeLog("fail to connect DB: " + e.getMessage());
        }
    }

    //value:   product_id,product_name,product_url,price,save,RRP,category
    public Boolean insert(String value) {
        try {
            if (dbConn.isClosed())
                dbConn = DriverManager.getConnection(dbURL, userName, password);
        } catch (Exception e) {
            Log.getInstance().writeLog("DB is closed unexpected: " + e.getMessage());
        }

        String[] values = value.split(delimiter);
        String query = "";
        if (values.length != 7 || values[0] == "null") {
            Log.getInstance().writeLog("incorrect record: " + value);
            return false;
        }
        try {
            query = "insert into dbo.raw_cache values('" + values[0] + "','"
                    + values[1].replace("'", "''") + "','"
                    + values[2].replace("'", "''") + "',"
                    + new BigDecimal(values[3]) + ","
                    + new BigDecimal(values[4]) + ","
                    + new BigDecimal(values[5]) + ",'"
                    + values[6].replace("'", "''") + "')";
            dbConn.createStatement().execute(query);
            return true;
        } catch (Exception e) {
            Log.getInstance().writeLog("cannot insert query: " + e.getMessage());
            Log.getInstance().writeLog("error query: " + query);
            return false;
        }
    }

    public void close() {
        try {
            dbConn.close();
        } catch (Exception e) {
            Log.getInstance().writeLog(e.getMessage());
        }
    }
}
