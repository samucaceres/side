package de.desarrollospy.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.desarrollospy.side.modelo.FacturaSifenView;

@Repository
public interface FacturaSifenViewRepository extends JpaRepository<FacturaSifenView, Long> {}