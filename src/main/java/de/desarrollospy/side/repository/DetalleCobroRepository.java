package de.desarrollospy.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import de.desarrollospy.side.modelo.DetalleCobro;

import java.util.List;

@Repository
public interface DetalleCobroRepository extends JpaRepository<DetalleCobro, Long> {

    @Query("SELECT d FROM DetalleCobro d WHERE d.nroCobro = :nroCobro AND d.idSede = :idSede AND d.idCaja = :idCaja AND d.nroApecie = :nroApecie AND d.idFuncio = :idFuncio")
    List<DetalleCobro> findByCompositeId(
            @Param("nroCobro") Integer nroCobro,
            @Param("idSede") Integer idSede,
            @Param("idCaja") Integer idCaja,
            @Param("nroApecie") Integer nroApecie,
            @Param("idFuncio") Integer idFuncio
    );
}