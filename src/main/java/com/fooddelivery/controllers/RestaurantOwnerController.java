package com.fooddelivery.controllers;

import com.fooddelivery.dto.request.RestaurantOwnerRequestDTO;
import com.fooddelivery.dto.response.RestaurantOwnerResponseDTO;
import com.fooddelivery.services.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class RestaurantOwnerController {

    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<RestaurantOwnerResponseDTO> registerOwner(@Valid @RequestBody RestaurantOwnerRequestDTO dto) {
        RestaurantOwnerResponseDTO created = restaurantService.registerOwner(dto);
        return ResponseEntity.created(URI.create("/api/owners/" + created.getId())).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantOwnerResponseDTO>> getAllOwners() {
        return ResponseEntity.ok(restaurantService.getAllOwners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantOwnerResponseDTO> getOwnerById(@PathVariable Integer id) {
        return ResponseEntity.ok(restaurantService.getOwnerById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<RestaurantOwnerResponseDTO> getOwnerByEmail(@PathVariable String email) {
        return ResponseEntity.ok(restaurantService.getOwnerByEmail(email));
    }
}