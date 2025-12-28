package de.desarrollospy.side.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import de.desarrollospy.side.modelo.EDoc;

import java.util.List;
import java.util.Optional;

@Repository
public interface EDocRepository extends JpaRepository<EDoc, Long> {
    
    // Para buscar si ya existe un documento generado previamente
    Optional<EDoc> findByIdDocumentoOriginalAndTipoDocumento(String idDocumentoOriginal, String tipoDocumento);
    
    // Para buscar por CDC
    Optional<EDoc> findByCdc(String cdc);
    
 // Query para obtener los Nro de Lote distintos pendientes de verificación
    @Query("SELECT DISTINCT e.nroLote FROM EDoc e WHERE e.estado = 'ENVIADO SET' AND e.nroLote IS NOT NULL")
    List<String> findLotesPendientes();
}