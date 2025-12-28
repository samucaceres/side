package de.desarrollospy.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import de.desarrollospy.side.modelo.Evento;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
}