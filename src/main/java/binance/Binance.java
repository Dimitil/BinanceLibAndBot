package binance;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Binance {

    ObjectMapper mapper = new ObjectMapper();
    String apiKey;
    Encryptor encryptor = null;
    final static String baseUrl = "https://api.binance.com/";
    final static String timeUrl = baseUrl + "api/v1/time";
    final static String depth = baseUrl + "api/v1/depth";
    final static String tickerPrice  = baseUrl + "api/v3/ticker/price";
    final static String tickerBook = baseUrl + "api/v3/ticker/bookTicker";
    final static String accountInfo = baseUrl + "/api/v3/account";
//    final static String exchangeInfo = baseUrl + "api/v1/exchangeInfo";

    public void setAPIKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setSecretKey(String key) {
        encryptor = new Encryptor(key);
    }

    public long getServerTime() {
        long res = 0;
        try {
             URL timeUri = new URL(timeUrl);
             JsonNode rootNode = mapper.readValue(timeUri, JsonNode.class);
             res = rootNode.get("serverTime").asLong();
        } catch (IOException e) {
                e.printStackTrace();
        }
        return res;
    }

    public OrderBook getOrderBook(String pair, int count) {
        String resUri = depth + "?symbol=" + pair + "&" +"limit=" + count;
        OrderBook orderBook = new OrderBook();
        try {
            URL url = new URL(resUri);
            JsonNode rootNode = mapper.readTree(url);
            JsonNode bidsNode = rootNode.get("bids");
            for (int i = 0; i < count; i++) {
                JsonNode bidNode = bidsNode.get(i);
                double price = bidNode.get(0).asDouble();
                double qty = bidNode.get(1).asDouble();
                orderBook.addBid(new Bid(price, qty));
            }
            JsonNode asksNode = rootNode.get("asks");
            for (int i = 0; i < count; i++) {
                JsonNode askNode = asksNode.get(i);
                double price = askNode.get(0).asDouble();
                double qty = askNode.get(1).asDouble();
                orderBook.addAsk(new Ask(price, qty));
            }
        } catch (MalformedURLException e) {
            System.out.println("Error with URL in Binance::getOrderBook()"+ e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orderBook;
    }

    public double getLastPrice(String symbol) {
        double price = 0;
        String resUrl = tickerPrice + "?symbol=" + symbol;

        try {
            URL url = new URL(resUrl);
            JsonNode rootNode = mapper.readValue(url, JsonNode.class);
            price = rootNode.get("price").asDouble();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println("MalformedURL");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return price;
    }

    double[] getTickerBook(String symbol) {
        String resUrl = tickerBook + "?symbol=" + symbol;
        try {
            URL url = new URL(resUrl);
            JsonNode rootNode = mapper.readValue(url, JsonNode.class);
            double bidPrice = rootNode.get("bidPrice").asDouble();
            double askPrice = rootNode.get("askPrice").asDouble();
            return new double[]{ bidPrice, askPrice};
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getBestBid(String symbol) {
        return getTickerBook(symbol)[0];
    }

    public double getBestAsk(String symbol){
        return getTickerBook(symbol)[1];
    }

    String getAccountInfo()
    {
        return new String();
    }

}
//            'trades':           {'url': 'api/v1/trades', 'method': 'GET', 'private': False},
//            'historicalTrades': {'url': 'api/v1/historicalTrades', 'method': 'GET', 'private': False},
//            'aggTrades':        {'url': 'api/v1/aggTrades', 'method': 'GET', 'private': False},
//            'kines':           {'url': 'api/v1/kines', 'method': 'GET', 'private': False},
//            'ticker24hr':       {'url': 'api/v1/ticker/24hr', 'method': 'GET', 'private': False},


