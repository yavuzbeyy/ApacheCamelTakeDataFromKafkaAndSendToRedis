
package org.apache.camel.learn;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.redis.RedisConstants;

import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import redis.clients.jedis.Jedis;

public class MyRouteBuilder extends RouteBuilder {

	
	String fileKey;
	String filePath;
	
    @Override
    public void configure() throws Exception {
        from("kafka:192.168.20.104.dbo.UploadImages?brokers=localhost:59092")
            .process(exchange -> {
                String message = exchange.getIn().getBody(String.class);

                if(message != null) {
                // JSON mesajını işle
                JSONObject jsonMessage = (JSONObject) new JSONParser().parse(message);
                JSONObject payload = (JSONObject) jsonMessage.get("payload");

                // "after" ve "before" alanlarını kontrol et
                JSONObject after = (JSONObject) payload.getOrDefault("after", null);
                JSONObject before = (JSONObject) payload.getOrDefault("before", null);

                if (after != null && after.containsKey("FileKey")) {
                     fileKey = (String) after.get("FileKey");
                     filePath = (String) after.get("FilePath");
                    System.out.println("FileKey: " + fileKey + ",  FilePath: " + filePath);

                    // Redis'e ekleme işlemi
                                        
                    try{
                    Jedis jedis = new Jedis("localhost", 6379);
                    jedis.hset("filepaths", fileKey, filePath);
                    jedis.close();
                    
                    }catch (Exception e) {
                        System.out.println("Hata : " + e);
                    } 
                    
                    //Burayı biraz daha zorla
                  //  exchange.getContext().createProducerTemplate().sendBody("direct:addToRedis", null);
                   
                    

                } else if (before != null && before.containsKey("FileKey")) {
                	
                    String fileKey = (String) before.get("FileKey");
                    
                   try {
                    
                    Jedis jedis = new Jedis("localhost", 6379);
                    jedis.hdel("filepaths", fileKey);
                    jedis.close();
                    
                    System.out.println("Deleted FileKey: " + fileKey);
                    }catch(Exception e) 
                    {
                    	System.out.println("Hatalı İşlem " + e);
                    }  
                    
                    
                    // Redis'den silme işlemi
                /*    Map<String, Object> headers = new HashMap<>();
                    headers.put(RedisConstants.KEY, "filepaths");
                    headers.put(RedisConstants.FIELD, fileKey);
                    exchange.getIn().setHeaders(headers);
                    exchange.getContext().createProducerTemplate().sendBody("direct:deleteFromRedis", null);  */
                    
                    
                } else {
                    System.out.println("Invalid message format: 'after' or 'before' field is missing");
                }
            }});

        // Redis'e Ekleme İşlemi
        from("direct:addToRedis")
        .process(exchange -> {
           // Map<String, Object> headers = exchange.getIn().getHeaders();
        	Map<String, Object> headers = new HashMap<>();

            //headers.clear();
            // Yeni headerları ekle
           // headers.put("Content-Type", "text/html; charset=utf-8");
            headers.put(RedisConstants.COMMAND, "HSET");
            headers.put(RedisConstants.KEY,"filepaths");
            headers.put(RedisConstants.FIELD, fileKey);
            headers.put(RedisConstants.VALUE, filePath);
            
            System.out.println("Headers : " + headers);

            exchange.getIn().setHeaders(headers);
        })
        .to("spring-redis://localhost:6379");
        
        
        // Redis'den Silme İşlemi
       /*   from("direct:deleteFromRedis")
            .setHeader(RedisConstants.COMMAND, constant("HDEL"))
            .to("spring-redis://localhost:6379");  */
  
    }
}
