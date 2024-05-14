package org.apache.camel.learn;

import org.apache.camel.builder.RouteBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MyRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("kafka:192.168.20.104.dbo.UploadImages?brokers=localhost:59092")
            .process(exchange -> {
                String message = exchange.getIn().getBody(String.class);
               // System.out.println("Received message from Kafka: " + message);

                try {
                    if (message != null) { // message null değilse işlem yap
                        // JSON mesajını işle
                        JSONParser parser = new JSONParser();
                        JSONObject jsonMessage = (JSONObject) parser.parse(message);
                        JSONObject payload = (JSONObject) jsonMessage.get("payload");

                        // "after" ve "before" alanlarını kontrol et
                        JSONObject after = (JSONObject) payload.getOrDefault("after", null);
                        JSONObject before = (JSONObject) payload.getOrDefault("before", null);

                        if (after != null && after.containsKey("FileKey")) {
                            // Redise Ekleme İşlemi
                            String fileId = (String) after.get("FileKey");
                            String filePath = (String) after.get("FilePath");
                            System.out.println("FileKey: " + fileId + ",  FilePath: " + filePath);
                        } else if (before != null && before.containsKey("FileKey")) {
                            // Redisten Silme İşlemi
                            String fileKey = (String) before.get("FileKey");
                            System.out.println("Deleted FileKey: " + fileKey);
                        } else {
                            System.out.println("Invalid message format: 'after' or 'before' field is missing");
                        }
                    } else {
                        System.out.println("Received message is null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }
}
