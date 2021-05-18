
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.SerializationUtils;

public class Recv {
  protected static PurchaseDao newPurchaseDao;
  private final static String QUEUE_NAME = "a3_rabbit";
  private static final ExecutorService executor = Executors.newFixedThreadPool(10);

  public static void main(String[] argv) throws Exception {
    newPurchaseDao = new PurchaseDao();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUsername("test");
    factory.setPassword("test");
    factory.setHost("18.208.186.81");
    Connection connection = factory.newConnection();

    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
      Purchase newPurchase = SerializationUtils.deserialize(delivery.getBody());
      try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Purchase purchase = newPurchaseDao.createNewPurchase(newPurchase);
      } catch (SQLException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    };

    Runnable thread = new Runnable() {
      @Override
      public void run() {
        Channel channel = null;
        try {
          channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
          });
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    };
    for (int i = 0; i < 80; i++) {
      thread.run();
    }
  }
}

