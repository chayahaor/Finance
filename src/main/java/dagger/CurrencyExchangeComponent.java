package dagger;

import helpers.CurrencyComboBox;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CurrencyExchangeModule.class})

public interface CurrencyExchangeComponent
{
    CurrencyComboBox getCurrencyExchange();
}