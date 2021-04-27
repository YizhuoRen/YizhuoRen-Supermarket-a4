package part2;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

class Consumer implements Runnable {
  private final BlockingQueue queue;
  //private FileWriter csvWriter;

  Consumer(BlockingQueue q) {
    this.queue = q;
  }

  public void run() {
    try {
      while (true) {
        consume(queue.take());
      }
    } catch (InterruptedException | IOException ex) {
      System.err.println(ex);
    }
  }

  void consume(Object record) throws IOException {
    PurchaseClientPart2.records.add((List<String>)record);
  }
}



