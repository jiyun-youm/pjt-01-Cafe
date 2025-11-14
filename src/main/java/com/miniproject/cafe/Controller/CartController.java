package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.CartService;
import com.miniproject.cafe.VO.CartItemVO;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/home")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {
        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");

        if (loginMember == null) {
            return "redirect:/";
        }

        String memberId = loginMember.getId();
        Map<String, Object> cartData = cartService.getCartList(memberId);

        if (cartData == null) {
            cartData = new HashMap<>();
            cartData.put("cartItems", new ArrayList<>());
            cartData.put("totalPrice", 0);
        }

        model.addAttribute("cartItems", cartData.get("cartItems"));
        model.addAttribute("totalPrice", cartData.get("totalPrice"));

        return "cart";
    }

    @GetMapping("/cart/list/{memberId}")
    @ResponseBody
    public Map<String, Object> getCartList(@PathVariable String memberId) {
        return cartService.getCartList(memberId);
    }

    @PostMapping("/cart/items")
    @ResponseBody
    public int addCartItem(@RequestBody CartItemVO cartItemVO) {
        return cartService.addCartItem(cartItemVO);
    }

    // 아이템 개별 삭제
    @DeleteMapping("/cart/items/{cartItemId}")
    @ResponseBody
    public ResponseEntity<String> deleteCartItem(@PathVariable long cartItemId) {
        try {
            int result = cartService.deleteCartItem(cartItemId);
            if (result == 1) {
                return ResponseEntity.ok().body("delete success");
            }
            return ResponseEntity.badRequest().body("delete fail");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("delete error");
        }
    }

    // 수량 변경
    @PatchMapping("/cart/items/{cartItemId}")
    @ResponseBody
    public ResponseEntity<String> changeQuantityCartItem(@PathVariable long cartItemId,
                                                         @RequestParam int quantity) {
        int result = cartService.changeQuantityCartItem(cartItemId, quantity);
        if (result == 1) {
            return ResponseEntity.ok().body("change success");
        }
        return ResponseEntity.badRequest().body("change fail");
    }
}
