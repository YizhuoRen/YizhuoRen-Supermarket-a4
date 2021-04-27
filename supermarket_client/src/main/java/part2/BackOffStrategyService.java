package part2;
import java.util.Random;


public class BackOffStrategyService {

  public static int defaultRetries=10;
  public static long defaultWaitTimeInMills=5000;
  private int numberOfRetries;

  private int numberOfTriesLeft;

  private long defaultTimeToWait;

  private long timeToWait;

  private final Random random = new Random();

  public BackOffStrategyService() {

    this(defaultRetries, defaultWaitTimeInMills);
  }

  public BackOffStrategyService(int numberOfRetries, long defaultTimeToWait){
    this.numberOfRetries = numberOfRetries;
    this.numberOfTriesLeft = numberOfRetries;
    this.defaultTimeToWait = defaultTimeToWait;
    this.timeToWait = defaultTimeToWait;
  }

  public boolean shouldRetry() {
    return numberOfTriesLeft > 0;
  }


  public void errorOccured() {

    numberOfTriesLeft -= 1;
    if (!shouldRetry()) {
      System.out.println("Retry failed!");
    }
    waitUntilNextTry();
    timeToWait += random.nextInt(1000);
  }
  private void waitUntilNextTry() {

    try {
      Thread.sleep(timeToWait);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  public long getTimeToWait() {
    return this.timeToWait;
  }
  public void doNotRetry() {
    numberOfTriesLeft = 0;
  }
  public void reset() {
    this.numberOfTriesLeft = numberOfRetries;
    this.timeToWait = defaultTimeToWait;
  }
}
