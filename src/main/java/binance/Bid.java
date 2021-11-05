package binance;

public class Bid implements Comparable{
    double price;
    double qty;

    public Bid(double price, double qty) {
        this.price = price;
        this.qty = qty;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public double getQty() {
        return qty;
    }
    public void setQty(double qty) {
        this.qty = qty;
    }

    @Override
    public int compareTo(Object o) {
        Bid b = (Bid) o;
        double res = this.getPrice() - b.getPrice();
        if(res>0) {
            return -1;
        }
        else if(res<0) {
            return 1;
        }
        return 0;
    }
}