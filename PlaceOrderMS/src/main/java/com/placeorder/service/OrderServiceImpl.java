package com.placeorder.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.placeorder.config.PlaceOrderConfig;
import com.placeorder.dao.BookInventoryDAO;
import com.placeorder.dao.OrderDAO;
import com.placeorder.dao.OrderItemDAO;
import com.placeorder.entity.BookInventory;
import com.placeorder.entity.MyOrder;
import com.placeorder.entity.MyOrderItem;
import com.placeorder.rabbitmq.BookInventoryInfo;
import com.placeorder.rabbitmq.Order;
import com.placeorder.rabbitmq.OrderInfo;
import com.placeorder.rabbitmq.OrderItem;

@Service
public class OrderServiceImpl implements OrderService {
	static Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

	@Autowired
	private OrderDAO orderDAO;

	@Autowired
	private OrderItemDAO orderItemDAO;

	@Autowired
	private BookInventoryDAO bookInventoryDAO;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@RabbitListener(queues = PlaceOrderConfig.ORDER_QUEUE)
	public void placeOrder(OrderInfo orderInfo) {
		log.info("--3. OrderServiceImpl---placeOrder()-----");
		// Place Order
		// Task1: Insert Order - 1
		Order order = orderInfo.getOrder();
		MyOrder myorder = new MyOrder(order.getOrderDate(), order.getUserId(), order.getTotalQty(),
				order.getTotalCost(), order.getStatus());
		myorder = orderDAO.save(myorder);
		int orderId = myorder.getOrderId();

		// Task2: Insert OrderItems - N** need to improve this
		List<OrderItem> itemsList = orderInfo.getItemsList();
		System.out.println("OrderServiceImpl -- itemList " + itemsList);
		for (OrderItem orderItem : itemsList) {
			orderItem.setOrderId(orderId);
			MyOrderItem myOrderItem = new MyOrderItem(orderId, orderItem.getBookId(), orderItem.getQty(),
					orderItem.getCost());
			orderItemDAO.save(myOrderItem);
		}

		// (Task4)
		// Update Local Inventory
		// Update Inventory of BookSearchMS
		// RestTemplate bookSearchRest=new RestTemplate();
		// String endpoint="http://localhost:8000/updateBookInventory";

		// Task3: Update Local Book Inventory - N
		// Task4: Update BookSearchMS BookInventory - N
		for (OrderItem myorderItem : itemsList) {
			Integer bookId = myorderItem.getBookId();
			BookInventory mybookInventory = bookInventoryDAO.findById(bookId).get();
			Integer currentStock = mybookInventory.getBooksAvailable();
			currentStock = currentStock - myorderItem.getQty();
			mybookInventory.setBooksAvailable(currentStock);

			// Local Inventory
			bookInventoryDAO.save(mybookInventory);

			// Inventory of BookSearchMS
			// bookSearchRest.put(endpoint, mybookInventory);
			BookInventoryInfo bookInventoryInfo = new BookInventoryInfo();
			bookInventoryInfo.setBookId(mybookInventory.getBookId());
			bookInventoryInfo.setBooksAvailable(mybookInventory.getBooksAvailable());
			rabbitTemplate.convertAndSend(PlaceOrderConfig.INVENTORY_QUEUE, bookInventoryInfo);

		}
	}

	@Override
	public List<MyOrder> getOrdersByUserId(String userId) {
		log.info("---OrderServiceImpl---getOrderByUserId()-----");
		List<MyOrder> orderList = orderDAO.getOrdersByUserId(userId);
		return orderList;
	}

	@Override
	public MyOrder getOrderByOrderId(Integer orderId) {
		log.info("---OrderServiceImpl---getOrderByOrderId()-----");
		MyOrder myorder = orderDAO.findById(orderId).get();
		return myorder;
	}

}
