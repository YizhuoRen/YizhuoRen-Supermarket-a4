import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class channelFactory extends BasePooledObjectFactory<Channel> {
  private final static String QUEUE_NAME = "a3_rabbit";

  @Override
  public Channel create() throws IOException {
    Channel channel = SupermarketServlet.connection.createChannel();
    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
    return channel;
  }

  /**
   * Use the default PooledObject implementation.
   */
  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<Channel>(channel);
  }

  /**
   * When an object is returned to the pool, clear the buffer.
   */
  @Override
  public void passivateObject(PooledObject<Channel> pooledObject)
      throws IOException, TimeoutException {
//    pooledObject.getObject().close();
  }

  // for all other methods, the no-op implementation
  // in BasePooledObjectFactory will suffice
}