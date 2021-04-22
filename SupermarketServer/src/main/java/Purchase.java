import java.util.Date;
import org.json.JSONObject;

public class Purchase {
  private Integer PurchaseId;
  private Integer StoreId;
  private Integer CustomId;
  private Date date;
  private JSONObject itemsPurchased;



  public Purchase(Integer storeId, Integer customId, Date date, JSONObject itemsPurchased) {
    StoreId = storeId;
    CustomId = customId;
    this.date = date;
    this.itemsPurchased = itemsPurchased;
  }



  public Integer getPurchaseId() {
    return PurchaseId;
  }

  public Integer getStoreId() {
    return StoreId;
  }

  public Integer getCustomId() {
    return CustomId;
  }

  public Date getDate() { return this.date; }

  public JSONObject getItemsPurchased() { return this.itemsPurchased;}



  public void setPurchaseId(Integer purchaseId) {
    PurchaseId = purchaseId;
  }

  public void setStoreId(Integer storeId) {
    StoreId = storeId;
  }

  public void setCustomId(Integer customId) {
    CustomId = customId;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public void setItemsPurchased(JSONObject itemsPurchased) { this.itemsPurchased = itemsPurchased; }

}
