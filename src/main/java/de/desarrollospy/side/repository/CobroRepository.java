package de.desarrollospy.side.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import de.desarrollospy.side.modelo.Cobro;

import java.util.Optional;

@Repository
public interface CobroRepository extends JpaRepository<Cobro, Long> {

    // Reemplaza tu query antigua de HibernateUtil
    @Query("SELECT c FROM Cobro c WHERE c.nroCobro = :nroCobro AND c.idSede = :idSede AND c.idCaja = :idCaja AND c.nroApecie = :nroApecie AND c.idFuncio = :idFuncio")
    Optional<Cobro> findByCompositeId(
            @Param("nroCobro") Integer nroCobro,
            @Param("idSede") Integer idSede,
            @Param("idCaja") Integer idCaja,
            @Param("nroApecie") Integer nroApecie,
            @Param("idFuncio") Integer idFuncio
    );
}