import binance.*;


public class Main {
    public static void main(String ...str) {
            Binance binance = new Binance();
            binance.setAPIKey("XXX");
            binance.setSecretKey("XXX");
            binance.getBalance("BTC");
            System.out.println(binance.getAccount());
//            Account acc = new Account();
//            acc.addBalance("qwe", 12, 32.32);
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

