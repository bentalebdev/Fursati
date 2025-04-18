package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Tracabilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TracabiliteRepository extends JpaRepository<Tracabilite, Long> {
}
