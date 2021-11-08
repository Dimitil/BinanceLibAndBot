package binance;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.Iterator;

public class JsonDeserializerToAccount extends StdDeserializer<Account> {

    public JsonDeserializerToAccount() {
        this(null);
    }

    public JsonDeserializerToAccount(Class<?> vc) {
        super(vc);
    }

    @Override
    public Account deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Account acc = new Account();
        JsonNode node = p.getCodec().readTree(p);
        int intTmp = node.get("makerCommission").asInt();
        acc.setMakerCommission(intTmp);
        intTmp = node.get("takerCommission").asInt();
        acc.setTakerCommission(intTmp);
        intTmp = node.get("buyerCommission").asInt();
        acc.setBuyerCommission(intTmp);
        intTmp = node.get("sellerCommission").asInt();
        acc.setSellerCommission(intTmp);
        boolean boolTmp = node.get("canTrade").asBoolean();
        acc.setCanTrade(boolTmp);
        boolTmp = node.get("canWithdraw").asBoolean();
        acc.setCanWithdraw(boolTmp);
        boolTmp = node.get("canDeposit").asBoolean();
        acc.setCanDeposit(boolTmp);
        long longTmp = node.get("updateTime").asLong();
        acc.setUpdateTime(longTmp);
        String str = node.get("accountType").asText();
        acc.setAccountType(str);

        JsonNode balNode = node.get("balances");
        for (Iterator<JsonNode> iterator = balNode.iterator(); iterator.hasNext(); ) {
            balNode = iterator.next();
            str = balNode.get("asset").asText();
            double fr = balNode.get("free").asDouble();
            double lc = balNode.get("locked").asDouble();
            acc.addBalance(str, fr, lc);
        }
        return acc;
    }
}
