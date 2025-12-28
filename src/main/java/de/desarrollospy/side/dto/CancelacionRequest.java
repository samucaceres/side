package de.desarrollospy.side.dto;

public class CancelacionRequest {
    
    private String idDocumento;   // Ej: "3-1-1-1-1"
    private String tipoDocumento; // Ej: "FE"
    private String motivo;        // Ej: "Error en RUC"

    // Constructor vacío
    public CancelacionRequest() {
    }

    public CancelacionRequest(String idDocumento, String tipoDocumento, String motivo) {
        this.idDocumento = idDocumento;
        this.tipoDocumento = tipoDocumento;
        this.motivo = motivo;
    }

    // Getters y Setters
    public String getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(String idDocumento) {
        this.idDocumento = idDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}