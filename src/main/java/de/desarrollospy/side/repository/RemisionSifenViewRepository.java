package de.desarrollospy.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.desarrollospy.side.modelo.RemisionSifenView;

@Repository
public interface RemisionSifenViewRepository extends JpaRepository<RemisionSifenView, Long> {}