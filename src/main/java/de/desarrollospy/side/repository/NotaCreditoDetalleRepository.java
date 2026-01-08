package de.desarrollospy.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import de.desarrollospy.side.modelo.NotaCreditoDetalle;

import java.util.List;

@Repository
public interface NotaCreditoDetalleRepository extends JpaRepository<NotaCreditoDetalle, Long> {

    /**
     * Recupera la lista de detalles (ítems) asociados a una Nota de Crédito específica.
     * @param notaCreditoId El ID de la cabecera de la Nota de Crédito.
     * @return Una lista de objetos NotaCreditoDetalle.
     */
    @Query("SELECT d FROM NotaCreditoDetalle d WHERE d.idNcred = :idNcred")
    List<NotaCreditoDetalle> findByNotaCreditoId(
            @Param("idNcred") String notaCreditoId
    );
}