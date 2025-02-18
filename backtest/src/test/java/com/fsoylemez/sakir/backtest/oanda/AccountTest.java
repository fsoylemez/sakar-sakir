package com.fsoylemez.sakir.backtest.oanda;

import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.account.AccountSummary;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.Properties;

@Disabled
@Slf4j
@QuarkusTest
public class AccountTest {

    @Inject
    Properties properties;

    @Test
    void accessTest() {
        Context ctx = new ContextBuilder("https://api-fxpractice.oanda.com")
                .setToken(properties.getProperty("oanda.token"))
                .build();

        try {
            AccountSummary summary = ctx.account.summary(
                    new AccountID(properties.getProperty("oanda.accountid"))).getAccount();
            log.info(summary.toString());
        } catch (Exception e) {
           log.error(e.getMessage());
        }
    }
}
