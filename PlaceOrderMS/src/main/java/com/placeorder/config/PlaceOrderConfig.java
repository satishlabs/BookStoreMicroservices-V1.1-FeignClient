package com.placeorder.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.google.common.base.Predicates;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
/*
PlaceOrderMS sends the message to BookSearchMS via Rabbit MQ Asynchronously
PlaceOrderMS Receives the message to BookStoreWeb via Rabbit MQ Asynchronously
So we need to use Rabbit MQ in PlaceOrderMS. 
 * */

@SpringBootApplication
public class PlaceOrderConfig {
	// PlaceOrderMS sends the message to BookSearchMS via Rabbit MQ Asynchronously
	public static final String INVENTORY_QUEUE = "MyInventory-Queue";
	public static final String INVENTORY_EXCHANGE = "MyInventory-Exchange";

	// PlaceOrderMS Receives the message to BookStoreWeb via Rabbit MQ
	// Asynchronously
	public static final String ORDER_QUEUE = "MyOrder-Queue";
	public static final String ORDER_EXCHANGE = "MyOrder-Exchange";

	private ApiInfo getMyApiInfo() {
		return new ApiInfo("PlaceOrderMS", "Place Order Microserices", "1.9", "Free to use for 10 times",
				new springfox.documentation.service.Contact("Satish Prasad", "https://www.coursecube.com",
						"sd@coursecube.com"),
				"API Under Free Licence", "https://www.coursecube.com");
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select().paths(PathSelectors.any())
				.apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework.boot"))).build()
				.apiInfo(getMyApiInfo());
	}

	// Inventory
	@Bean(name = "myInventorygQueue")
	Queue creatingInventoryQueue() {
		return QueueBuilder.durable(INVENTORY_QUEUE).build();
	}

	@Bean(name = "myInventoryExchange")
	Exchange createInventoryExchange() {
		return ExchangeBuilder.topicExchange(INVENTORY_EXCHANGE).build();
	}

	@Bean
	Binding inventoryBinding(Queue myInventorygQueue, TopicExchange myInventoryExchange) {
		return BindingBuilder.bind(myInventorygQueue).to(myInventoryExchange).with(INVENTORY_QUEUE);
	}

	@Bean(name = "myOrderQueue")
	Queue creatingOrderQueue() {
		return QueueBuilder.durable(ORDER_QUEUE).build();
	}

	@Bean(name = "myOrderExchange")
	Exchange createOrderExchange() {
		return ExchangeBuilder.topicExchange(ORDER_EXCHANGE).build();
	}

	@Bean
	Binding orderBinding(Queue myOrderQueue, TopicExchange myOrderExchange) {
		return BindingBuilder.bind(myOrderQueue).to(myOrderExchange).with(ORDER_QUEUE);
	}
}
