package de.desarrollospy.side.modelo;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "errores_documento", schema = "edoc")
public class ErroresDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nro_documento")
    private String nroDocumento;

    private String establecimiento;

    @Column(name = "punto_expedicion")
    private String puntoExpedicion;

    private String cdc;

    @Column(name = "fecha_error")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaError;

    @Column(name = "codigo_error")
    private String codigoError;

    @Column(name = "descripcion_error")
    private String descripcionError;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "id_documento_original")
    private String idDocumentoOriginal;

    @Column(name = "operacion")
    private String operacion;

    public ErroresDocumento() {
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNroDocumento() {
        return nroDocumento;
    }

    public void setNroDocumento(String nroDocumento) {
        this.nroDocumento = nroDocumento;
    }

    public String getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(String establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getPuntoExpedicion() {
        return puntoExpedicion;
    }

    public void setPuntoExpedicion(String puntoExpedicion) {
        this.puntoExpedicion = puntoExpedicion;
    }

    public String getCdc() {
        return cdc;
    }

    public void setCdc(String cdc) {
        this.cdc = cdc;
    }

    public Date getFechaError() {
        return fechaError;
    }

    public void setFechaError(Date fechaError) {
        this.fechaError = fechaError;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public void setCodigoError(String codigoError) {
        this.codigoError = codigoError;
    }

    public String getDescripcionError() {
        return descripcionError;
    }

    public void setDescripcionError(String descripcionError) {
        this.descripcionError = descripcionError;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getIdDocumentoOriginal() {
        return idDocumentoOriginal;
    }

    public void setIdDocumentoOriginal(String idDocumentoOriginal) {
        this.idDocumentoOriginal = idDocumentoOriginal;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }
}