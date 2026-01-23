package de.desarrollospy.side.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.desarrollospy.side.modelo.EntidadGobierno;

@Repository
public interface EntidadGobiernoRepository extends JpaRepository<EntidadGobierno, Long> {
    // No necesita atributos.
    // Al extender JpaRepository, ya tienes disponible el método .save() 
    // que usamos en el servicio.
	
	List<EntidadGobierno> findByRuc(String ruc);
}