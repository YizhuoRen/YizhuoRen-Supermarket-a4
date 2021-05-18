import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.json.JSONException;
import org.json.JSONObject;


@WebServlet(urlPatterns = {"/purchase", "/items"})
public class SupermarketServlet extends HttpServlet {
  private static final String RPC_QUEUE_NAME = "rpc_queue";
  private final static String QUEUE_NAME_MICROSERVICE = "a3_rabbit_queue2";
  private final static String QUEUE_NAME = "a3_rabbit";
  protected static Connection connection;
  private ObjectPool<Channel> pool;


  @Override
  public void init() throws ServletException {
    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setUsername("test");
      factory.setPassword("test");
      factory.setHost("");
      connection = factory.newConnection();
      GenericObjectPoolConfig conf = new GenericObjectPoolConfig();
      conf.setMaxTotal(800);
      conf.setMaxIdle(600);
      //create an channel pool
      pool = new GenericObjectPool<Channel>(new channelFactory(), conf);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String call(String message, Channel channel) throws IOException, InterruptedException {
    String corrId = UUID.randomUUID().toString();
    String replyQueueName = channel.queueDeclare().getQueue();
    BasicProperties props = (new Builder()).correlationId(corrId).replyTo(replyQueueName).build();
    channel.basicPublish("", RPC_QUEUE_NAME, props, message.getBytes("UTF-8"));
    BlockingQueue<String> response = new ArrayBlockingQueue(1);
    String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
      if (delivery.getProperties().getCorrelationId().equals(corrId)) {
        response.offer(new String(delivery.getBody(), "UTF-8"));
      }
    }, (consumerTag) -> {
    });
    String result = (String)response.take();
    channel.basicCancel(ctag);
    return result;
  }


  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {
    String urlPath = request.getPathInfo();
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }
    String[] urlParts = urlPath.split("/");
    try {
      if (!isValid(urlParts)) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("not valid");
      } else {
        response.setStatus(HttpServletResponse.SC_OK);
        //build RPC
        Channel channelRPC = null;
        channelRPC = pool.borrowObject();
        String sendMessage = urlParts[1] +" "+ urlParts[2];
        String responseMessage = call(sendMessage, channelRPC);
        response.getWriter().write(responseMessage);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean isValid(String[] urlPath) throws Exception {
    if (urlPath.length != 3) {
      return false;
    }
    if (!urlPath[1].equals("store")&&!urlPath[1].equals("top10")) {
      return false;
    }
    try{
      int Id = Integer.parseInt(urlPath[2]);
    }catch (NumberFormatException ex) {
      return false;
    }
    return true;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();
    StringBuilder sb = new StringBuilder();
    BufferedReader reader = req.getReader();
    try {
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append('\n');
      }
    } finally {
      reader.close();
    }
    JSONObject jsonObject = null;
    try {
      jsonObject = new JSONObject(sb.toString());
    } catch (JSONException e) {
      e.printStackTrace();
    }

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    try {
      if (!processPurchase(urlParts, jsonObject)) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write("not valid");
      } else {
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("It Works!");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean processPurchase(String[] urlPath, JSONObject jsonObject)
      throws Exception {
    int storeID;
    int custID;
    String date;
    if (urlPath.length != 6) {
      return false;
    }
    try{
      storeID = Integer.parseInt(urlPath[1]);
    }catch (NumberFormatException ex) {
      return false;
    }
    if (!urlPath[2].equals("customer")) {
      return false;
    }
    try{
      custID = Integer.parseInt(urlPath[3]);
    }catch (NumberFormatException ex) {
      return false;
    }

    if (!urlPath[4].equals("date")) {
      return false;
    }
    if (urlPath[5].matches("(([0-9]{4})[0-9]{2})([0-9]{2})")) {
      date = urlPath[5];
    }
    else {
      return false;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Purchase newPurchase = new Purchase(storeID, custID, sdf.parse(date), String.valueOf(jsonObject));
    byte[] data = SerializationUtils.serialize(newPurchase);
    Channel channel = null;
    try {
      channel = pool.borrowObject();
      channel.basicPublish("", QUEUE_NAME, null, "".getBytes());
      channel.basicPublish("", QUEUE_NAME_MICROSERVICE, null, data);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Unable to borrow channel from pool" + e.toString());
    } finally {
      try {
        if (null != channel)
          pool.returnObject(channel);
      } catch (Exception e) {
        // ignored
      }
    }
    return true;
  }
}
