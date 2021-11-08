import binance.*;


public class Main {
    public static void main(String ...str) {
            Binance binance = new Binance();
            binance.setAPIKey("XXX");
            binance.setSecretKey("XXX");
            System.out.println(binance.getBalance("BTC"));
//            binance.updateAccount();
//        System.out.println(binance.getAccount());

//        System.out.println(binance.getServerTime());
//        OrderBook ltcbtc = binance.getOrderBook("ETHBTC", 20);
//        out.println(ltcbtc.getAsk(0).getPrice());
//        System.out.println(ltcbtc.getAsk(0).getPrice());
//        System.out.println(binance.getLastPrice("ETHBTC"));
//        System.out.println(binance.getBestAsk("ETHBTC"));
    }
}

