package de.desarrollospy.side.dto;

import de.desarrollospy.side.modelo.EDoc;
import de.desarrollospy.side.modelo.ErroresDocumento;
import java.util.List;

public class DocumentoStatusDTO {
    
    private EDoc documento;
    private List<ErroresDocumento> errores;

    public DocumentoStatusDTO(EDoc documento, List<ErroresDocumento> errores) {
        this.documento = documento;
        this.errores = errores;
    }

    public DocumentoStatusDTO() {
    }

    public EDoc getDocumento() {
        return documento;
    }

    public void setDocumento(EDoc documento) {
        this.documento = documento;
    }

    public List<ErroresDocumento> getErrores() {
        return errores;
    }

    public void setErrores(List<ErroresDocumento> errores) {
        this.errores = errores;
    }
}