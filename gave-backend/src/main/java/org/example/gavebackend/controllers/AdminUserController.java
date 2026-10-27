package org.example.gavebackend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.gavebackend.repositories.appUserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final appUserRepository repo;

    @GetMapping
    public List<Map<String, Object>> list() {
        return repo.findAll().stream().map(u -> Map.<String, Object>of(
                "id", u.getId(), "email", u.getEmail(),
                "fullName", u.getFullName() == null ? "" : u.getFullName(),
                "priceAccess", u.isPriceAccess())).toList();
    }

    @PatchMapping("/{id}/price-access")
    public void set(@PathVariable Long id, @RequestParam boolean value) {
        var u = repo.findById(id).orElseThrow();
        u.setPriceAccess(value);
        repo.save(u);
    }
}