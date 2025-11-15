package com.miniproject.cafe.Controller;

import com.miniproject.cafe.Service.CartService;
import com.miniproject.cafe.VO.CartItemVO;
import com.miniproject.cafe.VO.MemberVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public String cartPage(Authentication auth, Model model) {

        // 로그인 안 되어있으면 홈으로
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/home/";
        }

        // 로그인 된 유저 email(id)
        String memberId = auth.getName();

        Map<String, Object> cartData = cartService.getCartList(memberId);

        if (cartData == null) {
            cartData = new HashMap<>();
            cartData.put("cartItems", new ArrayList<>());
            cartData.put("totalPrice", 0);
        }

        model.addAttribute("cartItems", cartData.get("cartItems"));
        model.addAttribute("totalPrice", cartData.get("totalPrice"));
        model.addAttribute("memberId", memberId);

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

    // 장바구니에 추가
    @PostMapping("/cart/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestBody Map<String, Object> cartData,
                                         HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        MemberVO loginMember = (MemberVO) session.getAttribute("loginMember");
        if (loginMember == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        try {
            String memberId = loginMember.getId();
            String menuId = (String) cartData.get("menuId");
            int quantity = Integer.parseInt(cartData.get("quantity").toString());
            String temp = (String) cartData.get("temp");
            boolean tumblerUse = Boolean.parseBoolean(cartData.get("tumblerUse").toString());
            int shotCount = Integer.parseInt(cartData.get("shotCount").toString());
            int vanillaSyrupCount = Integer.parseInt(cartData.get("vanillaSyrupCount").toString());
            int whippedCreamCount = Integer.parseInt(cartData.get("whippedCreamCount").toString());

            // CartService의 addToCart 메서드 호출 (다음에 구현)
            int addResult = cartService.addToCart(memberId, menuId, quantity, temp,
                    tumblerUse, shotCount, vanillaSyrupCount, whippedCreamCount);

            if (addResult > 0) {
                result.put("success", true);
                result.put("message", "장바구니에 추가되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "장바구니 추가에 실패했습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "서버 오류가 발생했습니다.");
        }

        return result;
    }
}
