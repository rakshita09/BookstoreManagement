package com.th.service;

import java.util.ArrayList;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.th.dto.UserCartDTO;
import com.th.model.Book;
import com.th.model.User;
import com.th.model.UsersCart;
import com.th.repository.BookRepository;
import com.th.repository.UserCartRepository;
import com.th.repository.UserRepository;
import com.th.utility.ImageUtil;

/**
 * CartServiceImpl implements the business logic for adding the book to the cart
 * and ordering the book
 * 
 * @author bharati_koliwad, arifa_azam
 *
 */

@Service
@Transactional
public class CartServiceImpl implements CartService {

	@Autowired
	UserCartRepository userCartRepository;

	@Autowired
	BookRepository bookRepository;

	@Autowired
	UserRepository userRepository;

	/**
	 * addBookToCart (adds a book to a user's cart)
	 * 
	 * @param userCart consists of bookid, cartid, quantity and useremail
	 * @return (a html page)
	 */
	@Override
	public String addBookToCart(UsersCart userCart) {

		UsersCart userCartFetchId = userCartRepository.findByUseremailAndBookid(userCart.getUseremail(),
				userCart.getBookid());

		UsersCart cartExists = new UsersCart();
		cartExists.setBookid(userCart.getBookid());
		if (userCartFetchId != null)
			cartExists.setCartid(userCartFetchId.getCartid());
		cartExists.setQuantity(userCart.getQuantity());
		cartExists.setUseremail(userCart.getUseremail());
		System.out.println(userCart.getCartid());

		userCartRepository.save(cartExists);

		return "cartsuccess";
	}

	/**
	 * getAllOrders (gets all books ordered by a user)
	 * 
	 * @param useremail
	 * @param model     adds objetcs to be dispalyed to a user
	 * @return (a html page)
	 */
	@Override
	public String getAllOrders(String useremail, Model model) {

		List<UsersCart> cartList = userCartRepository.findByUseremail(useremail);
		List<Book> books = new ArrayList<>();

		if (!cartList.isEmpty()) {

			cartList.forEach(cartItem -> books.add(bookRepository.findById(cartItem.getBookid()).get()));

			List<UserCartDTO> userCartDTOList = new ArrayList<>();

			books.forEach(bookItem -> {

				UserCartDTO userCartDTO = new UserCartDTO();

				userCartDTO.setBook(bookItem);
				userCartDTO.setQuantity(userCartRepository.findById(getCartId(cartList, bookItem.getBookid(), useremail)

				).get().getQuantity());

				userCartDTO.setCartid(getCartId(cartList, bookItem.getBookid(), useremail));

				userCartDTOList.add(userCartDTO);

			});

			double totalPrice = 0;
			for (UserCartDTO userdto : userCartDTOList) {
				totalPrice += calculateTotalPrice(userdto.getQuantity(), userdto.getBook().getBookprice());
			}

			model.addAttribute("totalprice", totalPrice);

			User user = userRepository.findById(useremail).get();

			model.addAttribute("username", user.getName());
			model.addAttribute("cartList", userCartDTOList);
			model.addAttribute("imgUtil", new ImageUtil());

			return "cart";
		} else {
			return "cart";
		}

	}

	private Integer getCartId(List<UsersCart> cartList, int bookid, String useremail) {
		for (UsersCart userCart : cartList) {
			if (userCart.getBookid() == bookid && userCart.getUseremail().equals(useremail)) {
				return userCart.getCartid();
			}
		}
		return null;
	}

	/**
	 * deleteBookCart (delete a book from cart)
	 * 
	 * @param cartid
	 * @return String (a cart page)
	 */
	@Override
	public String deleteBookCart(int cartid) {
		userCartRepository.deleteById(cartid);
		System.out.println(cartid);
		return "delete";
	}

	/**
	 * calculateTotalPrice (calculates price using quantity and each book price)
	 * 
	 * @param quantity  (number of books in cart)
	 * @param bookprice (price of each book)
	 * @return quantity * bookprice
	 */
	public double calculateTotalPrice(int quantity, double bookprice) {
		return (quantity * bookprice);
	}

	/**
	 * cartToPaymentPage (move from cart to payment page)
	 * 
	 * @param totalPrice (cost of all books)
	 * @param name       (name of the user)
	 * @param model
	 * @return String (a html page)
	 */
	@Override
	public String cartToPaymentPage(double totalPrice, String name, Model model) {

		model.addAttribute("username", name);
		model.addAttribute("totalPrice", totalPrice);

		return "paymentPage";
	}

	/**
	 * paymentToGreeting (move from payment to greetings page)
	 * 
	 * @param name  (name of the user)
	 * @param model
	 * @return String (a html page)
	 */

	@Override
	public String paymentToGreeting(String name, Model model) {
		model.addAttribute("username", name);
		return "greetings";
	}
}
