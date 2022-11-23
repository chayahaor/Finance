package json;

import java.util.Map;

public class CurrencyExchange
{
    public class Info
    {
        double rate;
    }

    public class Symbol
    {
        String description;
        String code;

        public Symbol(String description, String code)
        {
            this.description = description;
            this.code = code;
        }

        public String getDescription()
        {
            return description;
        }

        public String getCode()
        {
            return code;
        }
    }

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