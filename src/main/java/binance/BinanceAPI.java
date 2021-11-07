package binance;

public interface BinanceAPI {
    String baseUrl = "https://api.binance.com/";
    String timeUrl = baseUrl + "api/v1/time";
    String depth = baseUrl + "api/v1/depth";
    String tickerPrice  = baseUrl + "api/v3/ticker/price";
    String tickerBook = baseUrl + "api/v3/ticker/bookTicker";
    String accountInfo = baseUrl + "api/v3/account";


    double UNDEF_BALANCE_VALUE = -9999.9999;
//    String exchangeInfo = baseUrl + "api/v1/exchangeInfo";


    long getServerTime();
    OrderBook getOrderBook(String pair, int count);
    double getLastPrice(String symbol);
    double[] getTickerBook(String symbol);
    double getBestBid(String symbol);
    double getBestAsk(String symbol);
    double getBalance(String symbol);

}
