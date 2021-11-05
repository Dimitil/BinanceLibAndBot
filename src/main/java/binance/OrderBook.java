package binance;


import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class OrderBook {
    SortedSet<Bid> bids = new TreeSet<>();
    SortedSet<Ask> asks = new TreeSet<>();

    public void addAsk(Ask ask) {
        asks.add(ask);
    }
    public void addBid(Bid bid){
        bids.add(bid);
    }

    public int size(){
        return bids.size();
    }

    public Bid getBid(int ind)
    {
        Iterator<Bid> iterator = bids.iterator();
        Bid res = bids.first();
        for(int i = 0; i < ind; i++) {
            res = iterator.next();
        }
        return res;
    }

    public Ask getAsk(int ind)
    {
        Iterator<Ask> iterator = asks.iterator();
        Ask res = asks.first();
        for(int i = 0; i < ind; i++) {
            res = iterator.next();
        }
        return res;
    }
}
