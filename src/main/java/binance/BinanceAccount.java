//this is a just data, check updateTime beforeUse
package binance;

import binance.utils.JsonDeserializerToAccount;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = JsonDeserializerToAccount.class)
public class BinanceAccount {
    private int makerCommission;
    private int takerCommission;
    private int buyerCommission;
    private int sellerCommission;
    private boolean canTrade;
    private boolean canWithdraw;
    private boolean canDeposit;
    private long updateTime;
    private String accountType;
    final private List<Balances> balances = new ArrayList<>();

    public int getMakerCommission() {
        return makerCommission;
    }

    public void setMakerCommission(int makerCommission) {
        this.makerCommission = makerCommission;
    }

    public int getTakerCommission() {
        return takerCommission;
    }

    public void setTakerCommission(int takerCommission) {
        this.takerCommission = takerCommission;
    }

    public int getBuyerCommission() {
        return buyerCommission;
    }

    public void setBuyerCommission(int buyerCommission) {
        this.buyerCommission = buyerCommission;
    }

    public int getSellerCommission() {
        return sellerCommission;
    }

    public void setSellerCommission(int sellerCommission) {
        this.sellerCommission = sellerCommission;
    }

    public boolean isCanTrade() {
        return canTrade;
    }

    public void setCanTrade(boolean canTrade) {
        this.canTrade = canTrade;
    }

    public boolean isCanWithdraw() {
        return canWithdraw;
    }

    public void setCanWithdraw(boolean canWithdraw) {
        this.canWithdraw = canWithdraw;
    }

    public boolean isCanDeposit() {
        return canDeposit;
    }

    public void setCanDeposit(boolean canDeposit) {
        this.canDeposit = canDeposit;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getBalance(String symbol) {
        if(symbol == null || symbol.isEmpty()) return -9999.9999;
        for ( Balances b: balances ) {
            if(b.asset.equals(symbol)) return b.free;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Account{" +
                "makerCommission=" + makerCommission +
                ", takerCommission=" + takerCommission +
                ", buyerCommission=" + buyerCommission +
                ", sellerCommission=" + sellerCommission +
                ", canTrade=" + canTrade +
                ", canWithdraw=" + canWithdraw +
                ", canDeposit=" + canDeposit +
                ", updateTime=" + updateTime +
                ", accountType='" + accountType + '\'' +
                ", balances=" + balances +
                '}';
    }
    public void addBalance(String as, double fr, double lck)
    {
        if(as == null || as.isEmpty()) return;
        for (Balances b : balances)
        {
            if(b.asset.equals(as)) {
                b.free = fr;
                b.locked = lck;
                return;
            }
        }
        Balances bal = new Balances(as, fr, lck);
        balances.add(bal);
    }

    class Balances{
        String asset;
        double free;
        double locked;

        Balances(String str, double fr, double lc) {
            asset = str;
            free = fr;
            locked = lc;
        }

        @Override
        public String toString() {
            return "Balances{" +
                    "asset='" + asset + '\'' +
                    ", free=" + free +
                    ", locked=" + locked +
                    '}';
        }
    }
}


