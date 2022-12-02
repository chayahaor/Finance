package api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

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

    public double convert(String from, String to) throws IOException {
        String convert = url_base + "convert?from=" + from + "&to=" + to;
        URL url = new URL(convert);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        JsonElement root = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
        JsonObject object = root.getAsJsonObject();

        return Double.parseDouble(object.get("result").getAsString());
    }

    public ArrayList<String> getSymbolResults() throws IOException {
        ArrayList<String> output = new ArrayList<>();
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
                        output.add(String.valueOf(eachCurrency.get(code)));
                    }
                }
            }
        }

        return output;

    }

}
