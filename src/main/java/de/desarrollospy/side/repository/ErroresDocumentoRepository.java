package de.desarrollospy.side.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import de.desarrollospy.side.modelo.ErroresDocumento;

@Repository
public interface ErroresDocumentoRepository extends JpaRepository<ErroresDocumento, Long> {
    // No necesita atributos.
    // Al extender JpaRepository, ya tienes disponible el método .save() 
    // que usamos en el servicio.
	
	List<ErroresDocumento> findByIdDocumentoOriginal(String idDocumentoOriginal);
	List<ErroresDocumento> findByIdDocumentoOriginalAndTipoDocumento(String idDocumentoOriginal, String tipoDocumento);
}