package part2;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;


class Producer implements Runnable {

  private final BlockingQueue queue;
  private final int phaseID;
  int count1 = 0;

  Producer(BlockingQueue q, int id) {
    queue = q;
    this.phaseID = id;
  }

  public void run() {
    for (int j = 0; j < PurchaseClientPart2.numPurchases * PurchaseClientPart2.openingHours; j++) {
      try {
        queue.put(produce());
      } catch (InterruptedException ex) {
        System.err.println(ex);
      } catch (ApiException e) {
        e.printStackTrace();
      }
    }
    PurchaseClientPart2.completedAll.countDown();
  }

  public List<String> produce() throws ApiException {
    List<String> singleRecord = new ArrayList<>();
    PurchaseApi apiInstance1 = new PurchaseApi(PurchaseClientPart2.shop);
      ApiResponse<Void> res = null;
      long start = 0;
      long end;
      long latency = 0;
      int storeID = (int) Thread.currentThread().getId();
      Integer custID = generateCustomIdRandomly(storeID);
      Purchase body = generateItemsPurchasedRandomly();
      BackOffStrategyService backoff = new BackOffStrategyService();
      boolean flag = false;
      while (backoff.shouldRetry()) {
        try {
          start = System.currentTimeMillis();
          res = apiInstance1
              .newPurchaseWithHttpInfo(body, storeID, custID, PurchaseClientPart2.date);
          end = System.currentTimeMillis();
          latency = end - start;
        }
         catch (ApiException e) {
          System.out.println("Retrying");
          System.err.println(e.getCode());
          System.err.println(e.getResponseBody());
          backoff.errorOccured();
          continue;
        }
        if (500 <= res.getStatusCode() && res.getStatusCode() < 510) {
          System.out.println("Retrying");
          backoff.errorOccured();
        } else {
          backoff.doNotRetry();
          flag = true;
          break;
        }
      }
      if (res.getStatusCode() == 201 || res.getStatusCode() == 200) {
        PurchaseClientPart2
            .increaseNumSuccessfulRequests();   //increase the number of successful requests
        System.out.println(res.getStatusCode()); //print out the response code
      } else {
        PurchaseClientPart2
            .increaseNumUnsuccessfulRequests();  //increase the number of unsuccessful requests
        System.err.println(res.getStatusCode());
      }
      singleRecord = (Arrays.asList(Long.toString(start), "POST", Long.toString(latency),
          String.valueOf(res.getStatusCode())));
      count1++;
      if (this.phaseID == 1) {
        if (count1 == PurchaseClientPart2.numPurchases * PurchaseClientPart2.phaseHoursOne
            && PurchaseClientPart2.firstTime) {
          PurchaseClientPart2.firstTime = false;
          PurchaseClientPart2.completed.countDown();
        }
        if (count1 == PurchaseClientPart2.numPurchases * PurchaseClientPart2.phaseHoursTwo
            && PurchaseClientPart2.secondTime) {
          PurchaseClientPart2.secondTime = false;
          PurchaseClientPart2.completed2.countDown();
        }
      }
      if (this.phaseID == 2) {
        if (count1 == PurchaseClientPart2.numPurchases * PurchaseClientPart2.phaseHoursTwo
            && PurchaseClientPart2.secondTime) {
          PurchaseClientPart2.secondTime = false;
          PurchaseClientPart2.completed2.countDown();
        }
      }
    return singleRecord;
  }


  public int generateCustomIdRandomly(int storeID) {
    int num = 1000;
    Random random = new Random();
    int min = storeID * num;
    int max = storeID * num + PurchaseClientPart2.numCustomerPerStore;
    return random.nextInt(max - min) + min;
  }

  public Purchase generateItemsPurchasedRandomly() {
    int amountEachItem = 1;
    Purchase purchase = new Purchase();
    for (int i = 0; i < PurchaseClientPart2.numItemsEachPurchase; i++) {
      Random random = new Random();
      int min = 1;
      int max = PurchaseClientPart2.maximumItemID + 1;
      int itemID = random.nextInt(max - min) + min;  //randomly select itemID
      PurchaseItems purchaseItems = new PurchaseItems();
      purchaseItems.itemID(String.valueOf(itemID));
      purchaseItems.numberOfItems(amountEachItem);
      purchase.addItemsItem(purchaseItems);
    }
    return purchase;
  }
}



