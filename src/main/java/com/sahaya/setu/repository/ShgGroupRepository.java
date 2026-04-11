package com.sahaya.setu.repository;

import com.sahaya.setu.model.ShgGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShgGroupRepository extends JpaRepository<ShgGroup, Long> {
}
