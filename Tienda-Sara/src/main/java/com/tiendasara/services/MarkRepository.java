package com.tiendasara.services;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tiendasara.models.Mark;

@Repository
public interface MarkRepository extends JpaRepository<Mark, Integer> {
}