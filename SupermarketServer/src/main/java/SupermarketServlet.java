
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;


@WebServlet("/purchase")
public class SupermarketServlet extends HttpServlet {
  protected PurchaseDao newPurchaseDao;
//    protected ItemPurchasedDao newItemPurchasedDao;

  @Override
  public void init() throws ServletException {
    newPurchaseDao = new PurchaseDao();
//      newItemPurchasedDao = new ItemPurchasedDao();
  }

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {
    doPost(request, response);
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
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  private boolean processPurchase(String[] urlPath, JSONObject jsonObject) throws ParseException {
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
//      JSONArray arr = jsonObject.getJSONArray("items");
    Purchase newPurchase = new Purchase(storeID, custID, sdf.parse(date), jsonObject);
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      Purchase purchase = newPurchaseDao.createNewPurchase(newPurchase);
//        for (int i =0; i < arr.length(); i++) {
//          String itemId = arr.getJSONObject(i).getString("ItemID");
//          Integer numberOfItems = arr.getJSONObject(i).getInt("numberOfItems:");
//          ItemPurchased itemPurchased = new ItemPurchased(itemId, numberOfItems, purchase.getPurchaseId());
//          newItemPurchasedDao.createItemPurchased(itemPurchased);
//        }
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return true;
  }
}
