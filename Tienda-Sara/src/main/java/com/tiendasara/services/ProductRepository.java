package com.tiendasara.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tiendasara.models.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
}