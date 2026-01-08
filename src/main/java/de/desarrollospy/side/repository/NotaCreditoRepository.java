package de.desarrollospy.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import de.desarrollospy.side.modelo.NotaCredito;

import java.util.Optional;

@Repository
public interface NotaCreditoRepository extends JpaRepository<NotaCredito, Long> {

    /**
     * Busca una Nota de Crédito por su ID primario y el ID de la sede relacionada.
     * @param id El identificador único de la tabla notas_creditos.
     * @param sedeId El identificador de la sede.
     * @return Un Optional con la entidad encontrada.
     */
    @Query("SELECT n FROM NotaCredito n WHERE n.id = :id")
    Optional<NotaCredito> findById(
            @Param("id") String id
    );
}