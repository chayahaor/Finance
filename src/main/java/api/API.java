package api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class API {
    private final String url_base;

    public API() {
        url_base = "https://api.exchangerate.host/";
    }

    public String convert(String from, String to, String date) throws IOException {
        String convert = url_base + "convert?from=" + from + "&to=" + to + "&date=" + date;
        URL url = new URL(convert);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
        JsonObject object = root.getAsJsonObject();

        return object.get("result").getAsString();
    }

    public String convert(String from, String to) throws IOException {
        String convert = url_base + "convert?from=" + from + "&to=" + to;
        URL url = new URL(convert);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
        JsonObject object = root.getAsJsonObject();

        return object.get("result").getAsString();
    }

    public void getSymbolResults() throws IOException {
        String urlSymbol = "https://api.exchangerate.host/symbols";
        URL url = new URL(urlSymbol);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
        JsonObject jsonObject = root.getAsJsonObject();
        JsonElement element = jsonObject.get("symbols");
        jsonObject = element.getAsJsonObject();

        new JsonObject();
        JsonObject eachCurrency;
        for (String key : jsonObject.keySet())
        {
            if (jsonObject.get(key) instanceof JsonObject)
            {
                eachCurrency = jsonObject.get(key).getAsJsonObject();
                for (String code : eachCurrency.keySet())
                {
                    if (code.length() <= 6)
                    {
                        System.out.println(eachCurrency.get(code));
                    }
                }
            }
        }


    }

    public static void main(String[] args) throws IOException {
        String from = "USD";
        String to = "ILS";
        String date = "2022-12-01";

        API api = new API();
        String req_result = api.convert(from, to);
        String req_result1 = api.convert(from, to, date);
        System.out.println(req_result);
        System.out.println(req_result1);
        api.getSymbolResults(); //TODO: change from returning void (just printing) to be an array)

    }
}
