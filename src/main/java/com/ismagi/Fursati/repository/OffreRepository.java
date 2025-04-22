package com.ismagi.Fursati.repository;

import com.ismagi.Fursati.entity.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

}
