package de.desarrollospy.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.desarrollospy.side.modelo.NotaCreditoSifenView;

@Repository
public interface NotaCreditoSifenViewRepository extends JpaRepository<NotaCreditoSifenView, Long> {
    // Puedes agregar métodos personalizados si es necesario, 
    // por ejemplo para buscar por número de documento o CDC.
}