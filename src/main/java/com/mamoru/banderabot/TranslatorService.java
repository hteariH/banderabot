package com.mamoru.banderabot;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TranslatorService {

    private static final Logger logger = LoggerFactory.getLogger(TranslatorService.class);

    protected String key;
    protected String location;

    public TranslatorService(@Value("${azure.translator.key}")String key,@Value("${azure.translator.region}") String location) {
        this.key = key;
        this.location = location;
    }

    // Instantiates the OkHttpClient.
    OkHttpClient client = new OkHttpClient();

    // This function performs a POST request.
    public String post(String text) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
                "[{\"Text\": \""+text+"\"}]");
        Request request = new Request.Builder()
                .url("https://api.cognitive.microsofttranslator.com/translate?api-version=3.0&to=uk")
                .post(body)
                .addHeader("Ocp-Apim-Subscription-Key", key)
                // location required if you're using a multi-service or regional (not global) resource.
                .addHeader("Ocp-Apim-Subscription-Region", location)
                .addHeader("Content-type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String translate(String text){
        try {
            String post = post(text);
            logger.info(post);
            JSONArray jsonArray = new JSONArray(post);
            JSONObject jsonObject =(JSONObject) jsonArray.get(0);
//            JSONObject jsonObject = new JSONObject(post);
            JSONArray translations = jsonObject.getJSONArray("translations");
            final String[] result = new String[1];
            translations.iterator().forEachRemaining(element -> {
                JSONObject translation = (JSONObject) element;
                if(translation.getString("to").equalsIgnoreCase("uk")) {
                     result[0] = translation.getString("text");
                }
            });
            return result[0];
        } catch (Exception e) {
            logger.error("Failed to translate message");
            logger.error(e.getMessage());
            logger.error("");
            return "Сталась помилка";
        }
    }

    // This function prettifies the json response.
//    public static String prettify(String json_text) {
//        JsonParser parser = new JsonParser();
//        JsonElement json = parser.parse(json_text);
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        return gson.toJson(json);
//    }
}
