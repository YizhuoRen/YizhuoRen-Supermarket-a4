import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/purchase")
public class SupermarketServlet extends HttpServlet {
  protected DynamoDBMapper mapper;


  @Override
  public void init(){
    AWSCredentialsProvider creds = new AWSStaticCredentialsProvider(
        new BasicAWSCredentials("AKIAQYM3DRQQB4Y6R",
            "")
    );

    AmazonDynamoDB ddbClient = AmazonDynamoDBClientBuilder.standard()
        .withCredentials(creds)
        .withRegion("us-east-1")
        .build();

    mapper = new DynamoDBMapper(ddbClient);

  }


  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {
    doPost(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
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

      String purchasedItem = sb.toString();


    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing parameters");
      return;
    }

    String[] urlParts = urlPath.split("/");
    try {
      if (!processPurchase(urlParts, purchasedItem)) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.getWriter().write("not valid");
      } else {
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("It Works!");
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private boolean processPurchase(String[] urlPath, String purchasedItem) throws ParseException {
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
    Purchase newPurchase = new Purchase();

    newPurchase.setPurchaseId(getUUID32(custID));
    newPurchase.setStoreId(storeID);
    newPurchase.setCustomId(custID);
    newPurchase.setDate(sdf.parse(date));
    newPurchase.setItemsPurchased(purchasedItem);

    mapper.save(newPurchase);

    return true;
  }

  public static String getUUID32(int custID){
      String customId = String.valueOf(custID);
      return UUID.randomUUID().toString().replace("-", "").toLowerCase() + customId;
  }
}
