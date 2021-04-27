package part2;

import static java.lang.System.exit;

import io.swagger.client.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;


public class PurchaseClientPart2 {
  public static final int openingHours = 9;
  public static int phaseThread;
  public static final int phaseHoursOne = 3;
  public static final int phaseHoursTwo = 5;
  public static int maxStores;
  public static int numPurchases;
  public static int numCustomerPerStore;
  public static int maximumItemID;
  public static int numItemsEachPurchase;
  public static String date;
  public static CountDownLatch completed;
  public static CountDownLatch completed2;
  public static CountDownLatch completedAll;
  public static boolean firstTime = true;
  public static boolean secondTime = true;
  public static final ApiClient shop = new ApiClient();
  public static int numSuccessfulRequests;
  public static int numUnsuccessfulRequests;
  static List<List<String>> records = Collections.synchronizedList(new ArrayList<>());
  public static LinkedBlockingDeque q = new LinkedBlockingDeque();
  public static int totalNumberOfRequests;

  public static synchronized void increaseNumSuccessfulRequests() {
    PurchaseClientPart2.numSuccessfulRequests++;
  }

  public static synchronized void increaseNumUnsuccessfulRequests() {
    PurchaseClientPart2.numUnsuccessfulRequests++;
  }

  private synchronized void phaseOne() {
    int num = 4;
    completed = new CountDownLatch(1);
    phaseThread = maxStores/num;
    for (int i = 0; i < phaseThread; i++) {
      Producer producer = new Producer(q, 1);
      new Thread(producer).start();
    }
  }

  public synchronized void phaseTwo() {
    int num = 4;
    completed2 = new CountDownLatch(1);
    phaseThread = maxStores/num;
    for (int i = 0; i < phaseThread; i++) {
      Producer producer = new Producer(q, 2);
      new Thread(producer).start();
    }
  }

  public synchronized void phaseThree() {
    int phaseID = 3;
    phaseThread = maxStores/2;
    for (int i = 0; i < phaseThread; i++) {
      Producer producer = new Producer(q, phaseID);
      new Thread(producer).start();
    }
  }

  public static void writingToCsv() throws IOException {
    try {
      FileWriter csvWriter = new FileWriter("records.csv");
      csvWriter.append("startTime");
      csvWriter.append(",");
      csvWriter.append("requestType");
      csvWriter.append(",");
      csvWriter.append("Latency");
      csvWriter.append(",");
      csvWriter.append("responseCode");
      csvWriter.append("\n");
      for (List<String> record : records) {
        csvWriter.append(String.join(",", record));
        csvWriter.append("\n");
      }
      csvWriter.flush();
      csvWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void calculateAndPrintOut() {
    int sum = 0;
    int meanResponseTime = 0;
    double pRate = 0.99;
    List<Integer> latencies = new ArrayList<>();

    for (List<String> record: records) {
      int latency = Integer.parseInt(record.get(2));
      latencies.add(latency);
      sum += latency;
    }
    int n = latencies.size();
    meanResponseTime = sum/n;
    int medianResponseTime;
    latencies.sort(null);
    if (n % 2 == 0) {
      int middleIndexOne = n/2;
      int middleIndexTwo = n/2 - 1;
      medianResponseTime = (latencies.get(middleIndexOne) + latencies.get(middleIndexTwo)) / 2;
    }
    else {
      medianResponseTime = latencies.get(n / 2);
    }
    int p99ResponseTime = latencies.get((int) Math.round((n * pRate)));
    int maxResponseTime = latencies.get(n-1);
    System.out.println("mean response time for POSTs (millisecs): " + meanResponseTime);
    System.out.println("median response time for POSTs (millisecs): " + medianResponseTime);
    System.out.println("p99 (99th percentile) response time for POSTs (millisecs): " + p99ResponseTime);
    System.out.println("max response time for POSTs (millisecs): " + maxResponseTime);
  }

  public static void main(String[] args) throws InterruptedException, IOException {
    int index3 = 3;
    int index4 = 4;
    int index5 = 5;
    int index6 = 6;
    int num = 1000;
    maxStores= Integer.parseInt(args[0]);
    numCustomerPerStore= Integer.parseInt(args[1]);
    maximumItemID = Integer.parseInt(args[2]);
    numPurchases = Integer.parseInt(args[index3]);
    numItemsEachPurchase = Integer.parseInt(args[index4]);
    String basePath = args[index6];
    shop.setBasePath(basePath);
    date = args[index5]; // String | date of purchase
    completed = new CountDownLatch(maxStores);
    PurchaseClientPart2 api = new PurchaseClientPart2();
    completedAll = new CountDownLatch(maxStores);
    shop.setReadTimeout(0);
    shop.setConnectTimeout(0);
    totalNumberOfRequests = maxStores*numPurchases*openingHours;

    Consumer c1 = new Consumer(q);
    new Thread(c1).start();
    long start = System.currentTimeMillis();
    api.phaseOne();
    completed.await();
//    System.out.println("notify get, start phase 2");
    api.phaseTwo();
    completed2.await();
//    System.out.println("notify get, start phase 3");
    api.phaseThree();
    completedAll.await();
    long end = System.currentTimeMillis();
    long difference = end - start;
//    System.out.println("notify get, all done!");

    System.out.println("total number of successful requests sent: " + numSuccessfulRequests);
    System.out.println("total number of unsuccessful requests: " + numUnsuccessfulRequests);
    System.out.println("the total run time (wall time): " + difference/num + "seconds");
    System.out.println("the throughput: " + numSuccessfulRequests/(difference/num));

    writingToCsv();
    calculateAndPrintOut();
    exit(0);
 }
}
