package de.desarrollospy.side.servicios;

import com.roshka.sifen.core.beans.response.RespuestaConsultaRUC;

import de.desarrollospy.side.dto.DocumentoStatusDTO;

public interface SifenService {
    
    /**
     * Consulta el estado de un contribuyente en el Sifen.
     * @param ruc RUC a consultar (con o sin DV, según lógica interna)
     * @return Objeto RespuestaConsultaRUC de la librería Sifen
     */
    RespuestaConsultaRUC consultarRuc(String ruc);
    
    /**
     * Procesa, firma y envía un documento a la SET.
     * @param idDocumento ID compuesto (ej: "123-1-1-1-1")
     * @param tipoDocumento Tipo (ej: "FE")
     * @return Mensaje de resultado
     */
    String procesarDocumento(String idDocumento, String tipoDocumento);
    
    String enviarCancelacion(String idDocumento, String tipoDocumento, String motivo);
    
    DocumentoStatusDTO consultarEstadoDocumento(String idDocumentoOriginal,String tipoDocumento);
}