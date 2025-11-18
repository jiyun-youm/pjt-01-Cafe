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

    @GetMapping("/cart")
    public String cartPage(Authentication auth, Model model, HttpSession session) {

        // 1. 로그인 체크
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/home/";
        }

        // 2. 세션에서 매장 정보 가져오기 (URL 파라미터 사용 X)
        String currentStore = (String) session.getAttribute("storeName");

        // 3. 방어 로직: 세션에 매장 정보가 없으면 메인으로 튕겨내기
        // (사용자가 매장 선택 없이 URL로 직접 접근하는 것 방지)
        if (currentStore == null || currentStore.trim().isEmpty()) {
            return "redirect:/home/";
        }
        System.out.println("🛒 [CartController] 세션값: [" + currentStore + "]");
        model.addAttribute("storeName", currentStore);

        // 5. 사용자 ID 가져오기
        String memberId = auth.getName();
        Map<String, Object> cartData;

        try {
            // 6. 장바구니 데이터 조회
            cartData = cartService.getCartList(memberId);

            // ✅ 빈 데이터 / 오류 데이터 필터링 로직
            if (cartData != null && cartData.get("cartItems") != null) {
                List<Map<String, Object>> cartItems = (List<Map<String, Object>>) cartData.get("cartItems");
                List<Map<String, Object>> validItems = new ArrayList<>();

                for (Map<String, Object> item : cartItems) {
                    // 가격 데이터 유효성 체크
                    if (item.get("MENU_PRICE") != null &&
                            Integer.parseInt(item.get("MENU_PRICE").toString()) > 0) {
                        validItems.add(item);
                    }
                }
                cartData.put("cartItems", validItems);
            }

            // 데이터가 아예 없을 경우 초기화
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

        // 7. 뷰(HTML)로 데이터 전달
        model.addAttribute("cartItems", cartData.get("cartItems"));
        model.addAttribute("totalPrice", cartData.get("totalPrice"));
        model.addAttribute("memberId", memberId);

        return "cart"; // user/cart.html 반환
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
                                         Authentication auth) {
        Map<String, Object> result = new HashMap<>();

        if (auth == null || !auth.isAuthenticated()) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        try {
            String memberId = auth.getName();
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
