package com.fms.sakar.sakir.service.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.account.request.OrderRequest;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class BinanceAccountService {

    @Inject
    BinanceApiRestClient binanceApiRestClient;

    public Account getAccount() {
        return binanceApiRestClient.getAccount();
    }

    public List<AssetBalance> getBalances() {
        Account account = getAccount();
        List<AssetBalance> balances = account.getBalances();

        return balances.stream().filter(b-> Double.parseDouble(b.getFree()) > 0).collect(Collectors.toList());
    }

    public List<Trade> getTrades(String symbol) {
        return binanceApiRestClient.getMyTrades(symbol);
    }

    public Trade getLastTrade(String symbol) {
        return getTrades(symbol).stream().max(Comparator.comparing(Trade::getTime)).orElse(null);
    }

    public List<Order> getOpenOrders(String symbol) {
        return binanceApiRestClient.getOpenOrders(new OrderRequest(symbol));
    }

    public String getFreeBalance(String symbol) {
        return getBalances().stream().filter(ab->symbol.equals(ab.getAsset())).map(AssetBalance::getFree).findFirst().orElse(null);
    }
}
