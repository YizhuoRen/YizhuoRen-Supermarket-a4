import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@NoArgsConstructor
@Getter
@Setter
@DynamoDBTable(tableName="Purchases")
public class Purchase {

  @DynamoDBHashKey(attributeName ="purchaseId")
  private String purchaseId;

  @DynamoDBAttribute(attributeName = "storeId")
  private Integer storeId;

  @DynamoDBAttribute(attributeName ="customId")
  private Integer customId;

  @DynamoDBAttribute(attributeName = "date")
  private Date date;

  @DynamoDBAttribute(attributeName = "itemsPurchased")
  private String itemsPurchased;



  public void setPurchaseId(String purchaseId) {
    this.purchaseId = purchaseId;
  }

  public void setStoreId(Integer storeId) {
    this.storeId = storeId;
  }

  public void setCustomId(Integer customId) {
    this.customId = customId;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public void setItemsPurchased(String itemsPurchased) { this.itemsPurchased = itemsPurchased; }

}
