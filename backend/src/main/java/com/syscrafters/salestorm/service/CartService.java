package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.dto.CartDTOs.AddToCartRequest;
import com.syscrafters.salestorm.dto.CartDTOs.CartItemResponse;
import com.syscrafters.salestorm.dto.CartDTOs.CartResponse;
import com.syscrafters.salestorm.entity.Cart;
import com.syscrafters.salestorm.entity.CartItem;
import com.syscrafters.salestorm.entity.Inventory;
import com.syscrafters.salestorm.entity.Product;
import com.syscrafters.salestorm.exception.BusinessException;
import com.syscrafters.salestorm.repository.CartItemRepository;
import com.syscrafters.salestorm.repository.CartRepository;
import com.syscrafters.salestorm.repository.InventoryRepository;
import com.syscrafters.salestorm.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       InventoryRepository inventoryRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("Product not found: " + request.getProductId()));

        Inventory inv = inventoryRepository.findByProductId(product.getId())
                .orElseThrow(() -> new BusinessException("Stock information not available"));

        Cart cart = cartRepository.findByCustomerId(request.getCustomerId())
                .orElseGet(() -> cartRepository.save(new Cart(request.getCustomerId())));

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        int currentQtyInCart = existingItem.map(CartItem::getQuantity).orElse(0);
        int targetQty = currentQtyInCart + request.getQuantity();

        // Enforce: Prevent quantity from exceeding available stock!
        if (targetQty > inv.getAvailableQuantity()) {
            throw new BusinessException("Cannot add " + request.getQuantity() + " item(s). Requested total (" +
                    targetQty + ") exceeds available stock (" + inv.getAvailableQuantity() + ").");
        }

        BigDecimal unitPrice = product.isFlashSaleActive() ? product.getSalePrice() : product.getOriginalPrice();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(targetQty);
            item.setUnitPrice(unitPrice);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product.getId(), product.getName(), request.getQuantity(), unitPrice);
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return getCart(request.getCustomerId());
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> new Cart(customerId));

        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setCustomerId(customerId);

        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int totalItems = 0;

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                int available = inventoryRepository.findByProductId(item.getProductId())
                        .map(Inventory::getAvailableQuantity).orElse(0);

                CartItemResponse itemResp = new CartItemResponse();
                itemResp.setItemId(item.getId());
                itemResp.setProductId(item.getProductId());
                itemResp.setProductName(item.getProductName());
                itemResp.setQuantity(item.getQuantity());
                itemResp.setUnitPrice(item.getUnitPrice());
                itemResp.setTotalPrice(item.getTotalPrice());
                itemResp.setAvailableStock(available);

                total = total.add(item.getTotalPrice());
                totalItems += item.getQuantity();
                itemResponses.add(itemResp);
            }
        }

        response.setItems(itemResponses);
        response.setTotalAmount(total);
        response.setTotalItems(totalItems);
        return response;
    }

    @Transactional
    public void clearCart(Long customerId) {
        cartRepository.findByCustomerId(customerId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }
}
