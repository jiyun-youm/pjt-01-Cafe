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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/home")
public class CartController {

    @Autowired
    private CartService cartService;

    // Security 인증 객체로 로그인 여부 체크 (뷰 접근 제어용)
    private boolean isLoggedIn(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    // ✅ 실제 비즈니스 로직(장바구니)에 사용할 memberId는 세션에서 획득
    private String getMemberId(HttpSession session) {
        Object id = session.getAttribute("LOGIN_USER_ID");
        return (id != null) ? id.toString() : null;
    }

    @GetMapping("/cart")
    public String cartPage(Authentication auth, Model model, HttpSession session) {
        boolean loggedIn = isLoggedIn(auth);
        model.addAttribute("IS_LOGGED_IN", loggedIn);

        // 로그인 안 되어 있으면 홈으로
        if (!loggedIn) {
            return "redirect:/home/";
        }

        // ✅ 세션에서 memberId 가져오기 (member PK)
        String memberId = getMemberId(session);
        if (memberId == null) {
            // 세션에 memberId 없으면 강제로 로그아웃 처리 후 홈으로 보내도 됨
            return "redirect:/home/";
        }

        String currentStore = (String) session.getAttribute("storeName");
        model.addAttribute("storeName", currentStore);

        Map<String, Object> cartData;
        try {
            cartData = cartService.getCartList(memberId);

            if (cartData != null && cartData.get("cartItems") != null) {
                List<Map<String, Object>> cartItems =
                        (List<Map<String, Object>>) cartData.get("cartItems");
                List<Map<String, Object>> validItems = new ArrayList<>();

                for (Map<String, Object> item : cartItems) {
                    if (item.get("MENU_PRICE") != null &&
                            Integer.parseInt(item.get("MENU_PRICE").toString()) > 0) {
                        validItems.add(item);
                    }
                }
                cartData.put("cartItems", validItems);
            }

            if (cartData == null) {
                cartData = new HashMap<>();
                cartData.put("cartItems", new ArrayList<>());
                cartData.put("totalPrice", 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
        // 이 API는 기존대로 memberId를 직접 받음 (필요 시 세션 기반으로 변경 가능)
        return cartService.getCartList(memberId);
    }

    @PostMapping("/cart/items")
    @ResponseBody
    public int addCartItem(@RequestBody CartItemVO cartItemVO) {
        return cartService.addCartItem(cartItemVO);
    }

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

    @PostMapping("/cart/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestBody Map<String, Object> cartData,
                                         Authentication auth,
                                         HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 1차: Security 기준 로그인 체크
        if (!isLoggedIn(auth)) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        // ✅ 2차: 세션에 memberId(LOGIN_USER_ID)가 있는지 확인
        String memberId = getMemberId(session);
        if (memberId == null) {
            result.put("success", false);
            result.put("message", "세션 정보가 유효하지 않습니다. 다시 로그인 해주세요.");
            return result;
        }

        try {
            String menuId = (String) cartData.get("menuId");
            int quantity = Integer.parseInt(cartData.get("quantity").toString());
            String temp = (String) cartData.get("temp");
            boolean tumblerUse = Boolean.parseBoolean(cartData.get("tumblerUse").toString());
            int shotCount = Integer.parseInt(cartData.get("shotCount").toString());
            int vanillaSyrupCount = Integer.parseInt(cartData.get("vanillaSyrupCount").toString());
            int whippedCreamCount = Integer.parseInt(cartData.get("whippedCreamCount").toString());

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