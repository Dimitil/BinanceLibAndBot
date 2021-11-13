package binance;

public interface BinanceAPI {
    String baseUrl = "https://api.binance.com/";
    String timeUrl = baseUrl + "api/v1/time";
    String depth = baseUrl + "api/v1/depth";
    String tickerPrice  = baseUrl + "api/v3/ticker/price";
    String tickerBook = baseUrl + "api/v3/ticker/bookTicker";
    String accountInfo = baseUrl + "api/v3/account";
    String orderUrl = baseUrl + "api/v3/order";


    double UNDEF_BALANCE_VALUE = -9999.9999;

    long getServerTime();
    OrderBook getOrderBook(String pair, int count);
    double getLastPrice(String symbol);
    double getBestBid(String symbol);
    double getBestAsk(String symbol);
    double getBalance(String symbol);
    String postSellOrder(String symbol, double qty, double price); // return OrderID
    String postBuyOrder(String symbol, double qty, double price); //too


}
