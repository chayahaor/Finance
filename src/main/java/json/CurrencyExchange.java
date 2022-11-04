package json;

import java.util.Map;

public class CurrencyExchange
{
    String date;
    double result;
    Info info;
    Map<String, Symbol> symbols;

    public double getRate()
    {
        return info.rate;
    }

    public String getDate()
    {
        return date;
    }

    public double getResult()
    {
        return result;
    }

    public Map<String, Symbol> getSymbols()
    {
        return symbols;
    }
}