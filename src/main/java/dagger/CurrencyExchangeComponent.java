package dagger;

import helpers.CurrencyExchanger;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CurrencyExchangeModule.class})

public interface CurrencyExchangeComponent
{
    CurrencyExchanger getCurrencyExchanger();
}