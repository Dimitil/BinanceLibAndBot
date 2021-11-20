package binance;

import binance.utils.Encryptor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

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

    HashMap<String, Ticker> tickers = new HashMap<String, Ticker>();


    synchronized public void setAPIKey(String apiKey) {
        this.apiKey = apiKey;
    }

    synchronized public void setSecretKey(String key) {
        encryptor = new Encryptor(key);
    }

    synchronized public long getServerTime() {
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

    synchronized public OrderBook getOrderBook(String pair, int count) {
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

    synchronized public double getLastPrice(String symbol) {
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

    synchronized private void updateTickerBook(String symbol) {
        if(!tickers.containsKey(symbol)) {
            tickers.put(symbol, new Ticker());
        }
        Ticker t = tickers.get(symbol);
        long tickerUpdateTime = t.tickerUpdateTime;
        if(tickerUpdateTime + 5000 > new Date().getTime()) return;
        String resUrl = tickerBook + "?symbol=" + symbol;
        try {
            System.out.println("i do update");
            String response = sendGet(resUrl);
            JsonNode rootNode = mapper.readValue(response, JsonNode.class);
            t.bestBid = rootNode.get("bidPrice").asDouble();
            t.bestAsk = rootNode.get("askPrice").asDouble();
            t.tickerUpdateTime = new Date().getTime();
            System.out.println(t.tickerUpdateTime);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized public double getBestBid(String symbol) {
        updateTickerBook(symbol);
        return tickers.get(symbol).bestBid;
    }

    synchronized public double getBestAsk(String symbol){
        updateTickerBook(symbol);
        return tickers.get(symbol).bestAsk;
    }

    synchronized public double getBalance(String symbol) {
        if(updateAccount()) {
            return acc.getBalance(symbol);
        }
        return UNDEF_BALANCE_VALUE;
    }

    synchronized private boolean updateAccount() {
        if (apiKey == null || apiKey.isEmpty()) return false;
        if (encryptor == null) return false;
        long curTimeStamp = new Date().getTime();
        if(curTimeStamp < (acc.getUpdateTime() + DELTA_MILLS_SEC)) return true;
        String  body = "timestamp=" + curTimeStamp; // + "&recvWindow=45000";
        try {
            String response = sendSignedGet(accountInfo, body);
            acc = mapper.readValue(response, BinanceAccount.class);
            return true;
        } catch (IOException |
                InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    synchronized public String postSellOrder(String symbol, double qty, double price) {
        return sentPostOrder("SELL", symbol, qty, price);
    }

    synchronized public String postBuyOrder(String symbol, double qty, double price) {
        return sentPostOrder("BUY", symbol, qty, price);
    }

    @Override
    synchronized public boolean orderIsOpen(String symbol, long orderId) throws Exception {
        if(symbol == null ||
                symbol.isEmpty() ) throw new Exception("Invalid symbol or orderId");
        String body = "symbol=" + symbol + "&orderId=" + orderId
                + "&timestamp=" + new Date().getTime();
        String response = sendSignedGet(orderUrl , body);
            JsonNode jsonRoot = mapper.readValue(response, JsonNode.class);
            if(jsonRoot.isEmpty()) throw new Exception("Order not found");
            String status = jsonRoot.get("status").asText();
            return status.equals("NEW");
    }

    @Override
    synchronized public boolean deleteOpenOrder(String symbol, long orderId) throws Exception{
        if(symbol == null ||
                symbol.isEmpty() ) throw new Exception("Invalid symbol or orderId");
        String body = "symbol=" + symbol + "&orderId=" + orderId + "&timestamp="
                + new Date().getTime();
        String sign = encryptor.getSHA256(body);
        body += "&signature=" + sign;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(orderUrl + "?" + body))
                .DELETE()
                .header("X-MBX-APIKEY", apiKey)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if(!processCodeHandler(response.statusCode())) {
            throw new Exception ("bad response from delete order");
        }
        JsonNode jsonRoot = mapper.readValue(response.body(), JsonNode.class);
        String orderStatus = jsonRoot.get("status").asText();
        return orderStatus.equals("CANCELED");
    }

    synchronized private String sentPostOrder(String SELLorBUY, String symbol, double qty, double price) {
        if (qty <= 0) return "";
        if( price <= 0) return "";
        if(!SELLorBUY.equals("SELL") && !SELLorBUY.equals("BUY")) return "";
        String side = SELLorBUY;
        String body =  "symbol=" + symbol + "&side=" + side + "&type=LIMIT_MAKER" +
                "&timestamp=" + new Date().getTime() + "&quantity=" + qty +
                "&price=" + price + "&newOrderRespType=ACK";
        String sign = encryptor.getSHA256(body);
        body += "&signature=" + sign;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(orderUrl + "/test")) //DELETE TEST
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("X-MBX-APIKEY", apiKey)
                    .build();
            HttpResponse <String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (!processCodeHandler(response.statusCode())) {
                return "";
            }
            return processOrderResponse(response.body());
        } catch ( Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    synchronized private String sendGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(TIMEOUT_DURATION_SEC))
                .header("X-MBX-APIKEY", apiKey)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        try {
            if (processCodeHandler(response.statusCode())) {
                return response.body();
            }
        } catch ( Exception e) {
            e.printStackTrace();
            System.out.println(response.body());
        }
        return "";
    }

    synchronized private String sendSignedGet(String endPoint, String body) throws IOException, InterruptedException {
        String sign = encryptor.getSHA256(body);
        String resUrl = endPoint + "?" + body + "&signature=" + sign;
        return sendGet(resUrl);
    }

    private boolean processCodeHandler(int code)  {
        try {
            switch (code){
                case 429:
                    sleep(10000); //no need break statement
                case 200:
                    return true;
                case 400:
                    System.out.println("Signature for this request is not valid or order does not exist");
                    return false;
                case 401:
                    System.out.println("Api-key format invalid");
                    return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("Problem with sleep()");
            return true;
        }
        System.out.println("Unknown response code");
        System.out.println(code);
        System.exit(2);
        return false;
    }

    synchronized private String processOrderResponse(String response) {
        String orderId = "";
        try {
            JsonNode node = mapper.readValue(response, JsonNode.class);
            if(node.isEmpty()) return "";
            orderId = node.get("orderId").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return orderId;
    }

    private class Ticker {
        long tickerUpdateTime;
        double bestAsk;
        double bestBid;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Ticker ticker = (Ticker) o;
            return tickerUpdateTime == ticker.tickerUpdateTime &&
                    Double.compare(ticker.bestAsk, bestAsk) == 0 &&
                    Double.compare(ticker.bestBid, bestBid) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tickerUpdateTime, bestAsk, bestBid);
        }
    }
}
//            'trades':           {'url': 'api/v1/trades', 'method': 'GET', 'private': False},
//            'historicalTrades': {'url': 'api/v1/historicalTrades', 'method': 'GET', 'private': False},
//            'aggTrades':        {'url': 'api/v1/aggTrades', 'method': 'GET', 'private': False},
//            'kines':           {'url': 'api/v1/kines', 'method': 'GET', 'private': False},
//            'ticker24hr':       {'url': 'api/v1/ticker/24hr', 'method': 'GET', 'private': False},


