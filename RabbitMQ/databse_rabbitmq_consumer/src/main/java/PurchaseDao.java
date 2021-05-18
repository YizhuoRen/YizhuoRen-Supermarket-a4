
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbcp2.BasicDataSource;

public class PurchaseDao {
  private static BasicDataSource dataSource;

  public PurchaseDao(){dataSource = DBCPDataSource.getDataSource();}

  public Purchase createNewPurchase(Purchase newPurchase) throws SQLException{
    Connection conn = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultKey = null;
    String insertQueryStatement = "INSERT INTO Purchase (StoreId, CustomId, PurchaseDate, ItemPurchased) " +
        "VALUES (?,?,?,?)";
    try {
      conn = this.dataSource.getConnection();
      preparedStatement = conn.prepareStatement(insertQueryStatement, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setInt(1, newPurchase.getStoreId());
      preparedStatement.setInt(2, newPurchase.getCustomId());
      preparedStatement.setDate(3, new java.sql.Date(newPurchase.getDate().getTime()));
      preparedStatement.setString(4, String.valueOf(newPurchase.getItemsPurchased()));
      // execute insert SQL statemen
      preparedStatement.executeUpdate();

      resultKey = preparedStatement.getGeneratedKeys();
      int restaurantId = -1;
      if(resultKey.next()) {
        restaurantId = resultKey.getInt(1);
      } else {
        throw new SQLException("Unable to retrieve auto-generated key.");
      }
      newPurchase.setPurchaseId(restaurantId);
    } catch (SQLException e) {
      e.printStackTrace();
      System.out.println(e.getSQLState());
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
        if (preparedStatement != null) {
          preparedStatement.close();
        }
      } catch (SQLException se) {
        se.printStackTrace();
        System.out.println(se.getSQLState());
      }
    }
    return newPurchase;
  }
}
