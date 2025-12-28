package de.desarrollospy.side.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "edocs", schema = "edoc", indexes = {
    @Index(name = "idx_id_documento_original", columnList = "id_documento_original")
})
public class EDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cdc;

    @Column(name = "nro_documento")
    private String nroDocumento;

    @Column(name = "punto_exp")
    private String puntoExp;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Column(name = "fecha_emision_str")
    private String fechaEmisionStr;

    private String estado;

    @Column(name = "fecha_estado")
    private LocalDateTime fechaEstado;

    private String establecimiento;

    @Column(name = "id_documento_original")
    private String idDocumentoOriginal;

    @Column(name = "nro_lote")
    private String nroLote;

    private String verificado;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "nro_transaccion")
    private String nroTransaccion;

    @Column(name = "enviado")
    private String enviado;

    @Column(columnDefinition = "bytea")
    private byte[] xml;

    // Constructores
    public EDoc() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCdc() { return cdc; }
    public void setCdc(String cdc) { this.cdc = cdc; }

    public String getNroDocumento() { return nroDocumento; }
    public void setNroDocumento(String nroDocumento) { this.nroDocumento = nroDocumento; }

    public String getPuntoExp() { return puntoExp; }
    public void setPuntoExp(String puntoExp) { this.puntoExp = puntoExp; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getFechaEmisionStr() { return fechaEmisionStr; }
    public void setFechaEmisionStr(String fechaEmisionStr) { this.fechaEmisionStr = fechaEmisionStr; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaEstado() { return fechaEstado; }
    public void setFechaEstado(LocalDateTime fechaEstado) { this.fechaEstado = fechaEstado; }

    public String getEstablecimiento() { return establecimiento; }
    public void setEstablecimiento(String establecimiento) { this.establecimiento = establecimiento; }

    public String getIdDocumentoOriginal() { return idDocumentoOriginal; }
    public void setIdDocumentoOriginal(String idDocumentoOriginal) { this.idDocumentoOriginal = idDocumentoOriginal; }

    public String getNroLote() { return nroLote; }
    public void setNroLote(String nroLote) { this.nroLote = nroLote; }

    public String getVerificado() { return verificado; }
    public void setVerificado(String verificado) { this.verificado = verificado; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNroTransaccion() { return nroTransaccion; }
    public void setNroTransaccion(String nroTransaccion) { this.nroTransaccion = nroTransaccion; }

    public String getEnviado() { return enviado; }
    public void setEnviado(String enviado) { this.enviado = enviado; }

    public byte[] getXml() { return xml; }
    public void setXml(byte[] xml) { this.xml = xml; }
}