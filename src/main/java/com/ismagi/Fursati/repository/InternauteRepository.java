package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Internaute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InternauteRepository extends JpaRepository<Internaute, Long> {
}
