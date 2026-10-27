package org.example.gavebackend.entities;

import lombok.RequiredArgsConstructor;

import org.example.gavebackend.dtos.ProductDTO;
import org.example.gavebackend.repositories.appUserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PriceVisibility {
    private final appUserRepository users;

    public boolean canSeePrices() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken) return false;
        if (a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN"))) return true;
        return users.findByEmail(a.getName()).map(appUser::isPriceAccess).orElse(false);
    }

    public ProductDTO apply(ProductDTO dto, boolean canSee) {
        if (!canSee) {
            dto.setPrice(null);
            dto.setDiscountPercent(null);
            dto.setDiscountThreshold(null);
        }
        return dto;
    }
}
