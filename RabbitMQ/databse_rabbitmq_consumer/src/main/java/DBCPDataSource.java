
import org.apache.commons.dbcp2.BasicDataSource;

public class DBCPDataSource {
  private static BasicDataSource dataSource;

    private static final String user = "admin";
    private static final String password = "RYZ123456";
    private static final String hostName = "database-rds.ckze2rhpw4ok.us-east-1.rds.amazonaws.com";
    private static final int port= 3306;
    private static final String schema = "SuperMarket";
    private static final String timezone = "UTC";

  static {
    dataSource = new BasicDataSource();
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

      String url = String.format("jdbc:mysql://%s:%s/%s?allowPublicKeyRetrieval=true&"
          + "useSSL=false&useUnicode=true&"
          + "useJDBCCompliantTimezoneShift=true"
          + "&useLegacyDatetimeCode=false&serverTimezone=UTC", hostName, port, schema);
      dataSource.setUrl(url);
      dataSource.setUsername(user);
      dataSource.setPassword(password);
      dataSource.setInitialSize(10);
      dataSource.setMaxTotal(60);
  }

  public static BasicDataSource getDataSource() {
    return dataSource;
  }
}
