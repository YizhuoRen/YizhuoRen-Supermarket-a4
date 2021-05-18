import java.io.Serializable;
import java.util.Date;

public class Purchase implements Serializable {
  private Integer PurchaseId;
  private Integer StoreId;
  private Integer CustomId;
  private Date date;
  private String itemsPurchased;
  private static final long serialVersionUID = 6529685098267757690L;



  public Purchase(Integer storeId, Integer customId, Date date, String itemsPurchased) {
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

  public String getItemsPurchased() { return this.itemsPurchased;}



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

  public void setItemsPurchased(String itemsPurchased) { this.itemsPurchased = itemsPurchased; }


}
