package org.apache.camel.learn;

import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {

        // here is a sample which processes the input files
        // (leaving them in place - see the 'noop' flag)
        // then performs content based routing on the message using XPath
        /*from("file:src/data?noop=true")
            .choice()
                .when(xpath("/person/city = 'London'"))
                    .log("UK message")
                    .to("file:target/messages/uk")
                .otherwise()
                    .log("Other message")
                    .to("file:target/messages/others");
					
		 from("kafka:localhost:59092?topic=192.168.20.104.dbo.UploadImages")
            .log("Received message from Kafka: ${body}");
    }*/
	
	from("kafka:192.168.20.104.dbo.UploadImages?brokers=localhost:59092")
    .process(exchange -> {
        // Kafka'dan gelen mesajı işle
        String message = exchange.getIn().getBody(String.class);
        // İşlenen mesajı logla
        System.out.println("Received message from Kafka: " + message);
        // Mesajı işleme, veritabanına yazma, vb. işlemleri burada gerçekleştir
    });

	
	/*public void configure() {
       from("direct:start")
    .to("http://localhost:5062/api/Author/ListAll")
    .log("Received response from API: ${body}");*/

    }
}