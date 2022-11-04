package dagger;

import main.Main;

import javax.inject.Singleton;

@Singleton
@Component(modules = {CurrencyExchangeModule.class})

public interface CurrencyExchangeComponent
{
   Main getCurrencyExchangeFrame();
}