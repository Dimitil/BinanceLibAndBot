package binance;


import binance.utils.Encryptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;

public class Binance implements BinanceAPI {

    private ObjectMapper mapper = new ObjectMapper();
    private Account acc = new Account();
    private String apiKey;
    private Encryptor encryptor = null;
    HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();

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
        finally {
            return orderBook;
        }
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

    public double[] getTickerBook(String symbol) {
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
        return getTickerBook(symbol)[0];
    }

    public double getBalance(String symbol) {
        updateAccount();
        return acc.getBalance(symbol);
    }

    private boolean updateAccount() {
        if (apiKey == null || apiKey.isEmpty()) return false;
        if (encryptor == null) return false;
        long curTimeStamp = new Date().getTime();
        if(curTimeStamp < (acc.getUpdateTime() + 10000)) return true;  //

        String resUrl = accountInfo + "?";
        String  body = "timestamp=" + curTimeStamp; // + "&recvWindow=45000";
        String sign = encryptor.getSHA256(body);
        resUrl += body;
        resUrl += "&signature=" + sign;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(resUrl))
                .timeout(Duration.ofSeconds(20))
                .header("X-MBX-APIKEY", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            acc = mapper.readValue(response.body(), Account.class);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }


}
//            'trades':           {'url': 'api/v1/trades', 'method': 'GET', 'private': False},
//            'historicalTrades': {'url': 'api/v1/historicalTrades', 'method': 'GET', 'private': False},
//            'aggTrades':        {'url': 'api/v1/aggTrades', 'method': 'GET', 'private': False},
//            'kines':           {'url': 'api/v1/kines', 'method': 'GET', 'private': False},
//            'ticker24hr':       {'url': 'api/v1/ticker/24hr', 'method': 'GET', 'private': False},


