package com.bookstoreweb.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.bookstoreweb.dto.UserRating;
import com.bookstoreweb.rabbitmq.BookRatingInfo;


@FeignClient(value ="MyUserRatingMS",url="http://localhost:6500")
public interface UserRatingProxy {
	
	@GetMapping("/userRatings/{userId}")
	public List<UserRating> getUserRatingByUserId(@PathVariable String userId);
	
	@GetMapping("/bookRatings/{bookId}")
	public BookRatingInfo getBookRatingByBookId(@PathVariable Integer bookId);

}
