package api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static main.Main.HOME_CURRENCY;

public class API {
    private final String urlBase;

    public API()
    {
        urlBase = "https://api.exchangerate.host/";
    }

    /**
     * Uses API to convert between two currencies at specific date and return the FX Rate
     *
     * @param from - the currency being converted
     * @param to   - the currency the "from" currency is being converted to
     * @param date - the date of the conversion
     * @return the FX rate for the conversion
     * @throws IOException - if connection to API fails
     */
    public String convert(String from, String to, String date) throws IOException
    {
        String convert = urlBase + "convert?from=" + from + "&to=" + to + "&date=" + date;
        URL url = new URL(convert);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonElement root = JsonParser.parseReader(
                new InputStreamReader((InputStream) request.getContent()));
        JsonObject object = root.getAsJsonObject();

        return object.get("result").getAsString();
    }

    /**
     * Uses API to convert between two currencies today and return the FX Rate
     *
     * @param from - the currency being converted
     * @param to   - the currency that the "from" currency is being converted to
     * @return - the FX rate for the conversion
     * @throws IOException - if connection to API fails
     */
    public double convert(String from, String to) throws IOException
    {
        String convert = urlBase + "convert?from=" + from + "&to=" + to;
        URL url = new URL(convert);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        JsonElement root = JsonParser.parseReader(
                new InputStreamReader((InputStream) request.getContent()));
        JsonObject object = root.getAsJsonObject();

        return Double.parseDouble(object.get("result").getAsString());
    }

    /**
     * Uses API to get all the currency symbols
     *
     * @return a JComboBox of all the currency symbols (minus HOME_CURRENCY)
     * @throws IOException - if connection to API fails
     */
    public JComboBox<String> getSymbolResults() throws IOException
    {
        JComboBox<String> currency = new JComboBox<>();
        String urlSymbol = urlBase + "symbols";
        URL url = new URL(urlSymbol);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonElement root = JsonParser.parseReader(
                new InputStreamReader((InputStream) request.getContent()));
        JsonObject jsonObject = root.getAsJsonObject();
        JsonElement element = jsonObject.get("symbols");
        jsonObject = element.getAsJsonObject();

        JsonObject eachCurrency;
        for (String key : jsonObject.keySet())
        {
            if (jsonObject.get(key) instanceof JsonObject)
            {
                eachCurrency = jsonObject.get(key).getAsJsonObject();
                for (String code : eachCurrency.keySet())
                {
                    //Each currency comes in with quotes, that get removed later
                    if (code.length() <= 6)
                    {
                        currency.addItem(String.valueOf(eachCurrency.get(code)).replace("\"", ""));
                    }
                }
            }
        }

        currency.setEditable(false);
        currency.removeItem(HOME_CURRENCY);

        return currency;

    }

}
