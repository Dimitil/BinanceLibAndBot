package binance;


import binance.utils.Encryptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;

import static java.lang.Thread.sleep;

public class Binance implements BinanceAPI {
    public final long DELTA_MILLS_SEC = 5000;
    public final int TIMEOUT_DURATION_SEC = 10;
    final private HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
    final private ObjectMapper mapper = new ObjectMapper();
    private BinanceAccount acc = new BinanceAccount();
    private String apiKey = "";
    private Encryptor encryptor = null;

    public void setAPIKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setSecretKey(String key) {
        encryptor = new Encryptor(key);
    }

    public long getServerTime() {
        long res = 0;
        try {
             String response = sendGet(timeUrl);
             JsonNode rootNode = mapper.readValue(response, JsonNode.class);
             res = rootNode.get("serverTime").asLong();
        } catch (IOException | InterruptedException e) {
                e.printStackTrace();
        }
        return res;
    }

    public OrderBook getOrderBook(String pair, int count) {
        String resUri = depth + "?symbol=" + pair + "&" +"limit=" + count;
        OrderBook orderBook = new OrderBook();
        try {
            String response = sendGet(resUri);
            JsonNode rootNode = mapper.readTree(response);
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
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return orderBook;
    }

    public double getLastPrice(String symbol) {
        double price = 0;
        String resUrl = tickerPrice + "?symbol=" + symbol;
        try {
            String response = sendGet(resUrl);
            JsonNode rootNode = mapper.readValue(response, JsonNode.class);
            price = rootNode.get("price").asDouble();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return price;
    }

    public double[] getTickerBook(String symbol) {
        String resUrl = tickerBook + "?symbol=" + symbol;
        try {
            String response = sendGet(resUrl);
            JsonNode rootNode = mapper.readValue(response, JsonNode.class);
            double bidPrice = rootNode.get("bidPrice").asDouble();
            double askPrice = rootNode.get("askPrice").asDouble();
            return new double[]{ bidPrice, askPrice};
        } catch (IOException | InterruptedException e) {
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
        if(updateAccount()) {
            return acc.getBalance(symbol);
        }
        return UNDEF_BALANCE_VALUE;
    }

    private boolean updateAccount() {
        if (apiKey == null || apiKey.isEmpty()) return false;
        if (encryptor == null) return false;
        long curTimeStamp = new Date().getTime();
        if(curTimeStamp < (acc.getUpdateTime() + DELTA_MILLS_SEC)) return true;
        String resUrl = accountInfo + "?";
        String  body = "timestamp=" + curTimeStamp; // + "&recvWindow=45000";
        resUrl += body;
        try {
            String response = sendSignedGet(resUrl);
            System.out.print(response);
            acc = mapper.readValue(response, BinanceAccount.class);
            return true;
        } catch (IOException |
                InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public BinanceAccount getAccount() {  //DELETE THIS
        updateAccount();
        return acc;
    }

    private String sendGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT_DURATION_SEC))
                .header("X-MBX-APIKEY", apiKey)
                .GET()
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (responceCodeHandler(response.statusCode())) {
                return response.body();
            } else {
                return "";
            }
    }
    private String sendSignedGet(String resUrlWithBody) throws IOException, InterruptedException {
        String sign = encryptor.getSHA256(resUrlWithBody);
        String resUrl = resUrlWithBody + "&signature=" + sign;
//        System.out.println(resUrl);
        return sendGet(resUrl);
    }
    private boolean responceCodeHandler(int code) {
        try {
            if (code == 429) {
                sleep(10000);
                return true;
            } else if (code == 200) {
                return true;
            }
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


