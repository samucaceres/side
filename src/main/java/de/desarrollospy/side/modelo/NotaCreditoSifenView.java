package de.desarrollospy.side.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "v_datos_sifen_nota_credito", schema = "public")
@Immutable
public class NotaCreditoSifenView implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "nota_credito_id")
    private Long notaCreditoId;

    // --- 1. Identificación del Documento ---
    @Column(name = "numero_documento")
    private Integer numeroDocumento;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision;

    @Column(name = "cdc")
    private String cdc;

    @Column(name = "estado_dnit")
    private String estadoDnit;

    // --- Timbrado ---
    @Column(name = "numero_timbrado")
    private Long numeroTimbrado;

    @Column(name = "fecha_inicio_timbrado")
    private LocalDate fechaInicioTimbrado;

    @Column(name = "codigo_establecimiento")
    private String codigoEstablecimiento;

    @Column(name = "codigo_punto_expedicion")
    private String codigoPuntoExpedicion;

    // --- 2. Datos del Emisor ---
    @Column(name = "emisor_razon_social")
    private String emisorRazonSocial;

    @Column(name = "emisor_ruc")
    private String emisorRuc;

    @Column(name = "emisor_direccion")
    private String emisorDireccion;

    @Column(name = "emisor_ciudad_id")
    private Integer emisorCiudadId;

    @Column(name = "emisor_ciudad_desc")
    private String emisorCiudadDesc;

    @Column(name = "emisor_telefono")
    private String emisorTelefono;

    @Column(name = "emisor_email")
    private String emisorEmail;

    // --- 3. Datos del Receptor ---
    @Column(name = "receptor_razon_social")
    private String receptorRazonSocial;

    @Column(name = "receptor_documento_numero")
    private String receptorDocumentoNumero;

    @Column(name = "receptor_tipo_documento_desc")
    private String receptorTipoDocumentoDesc;
    
    @Column(name = "receptor_tipo_documento_codigo")
    private Integer receptorTipoDocumentoCodigo;

    @Column(name = "receptor_direccion")
    private String receptorDireccion;

    @Column(name = "receptor_email")
    private String receptorEmail;
    
    @Column(name = "receptor_telefono")
    private String receptorTelefono;

    @Column(name = "naturaleza_receptor")
    private Integer naturalezaReceptor;

    @Column(name = "tipo_operacion")
    private Integer tipoOperacion;

    // --- Datos Geográficos del Receptor ---
    @Column(name = "receptor_distrito_desc")
    private String receptorDistritoDesc;
    
    @Column(name = "receptor_distrito_id")
    private Integer receptorDistritoId;

    @Column(name = "receptor_departamento_desc")
    private String receptorDepartamentoDesc;
    
    @Column(name = "receptor_departamento_id")
    private Integer receptorDepartamentoId;

    @Column(name = "receptor_ciudad_desc")
    private String receptorCiudadDesc;
    
    @Column(name = "receptor_ciudad_id")
    private Integer receptorCiudadId;

    // --- 4. Datos Específicos NC ---
    @Column(name = "motivo_emision")
    private Integer motivoEmision; // 1=Devolución, 2=Descuento, etc.

    @Column(name = "total_operacion")
    private BigDecimal totalOperacion;

    // --- 5. DETALLES (JSON) ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalles_json")
    private List<DetalleNotaCreditoJson> detalles = new ArrayList<>();

    // --- 6. DOCUMENTO ASOCIADO (JSON Object) ---
    // A diferencia de listas, este es un objeto único que representa la factura afectada
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "documento_asociado_json")
    private DocumentoAsociadoJson documentoAsociado;
   
    @Column(name = "moneda")
    private String moneda;
    
    @Column(name = "tipo_cambio")
    private Integer tipoCambio;

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public Long getNotaCreditoId() { return notaCreditoId; }
    public void setNotaCreditoId(Long notaCreditoId) { this.notaCreditoId = notaCreditoId; }

    public Integer getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(Integer numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getCdc() { return cdc; }
    public void setCdc(String cdc) { this.cdc = cdc; }

    public String getEstadoDnit() { return estadoDnit; }
    public void setEstadoDnit(String estadoDnit) { this.estadoDnit = estadoDnit; }

    public Long getNumeroTimbrado() { return numeroTimbrado; }
    public void setNumeroTimbrado(Long numeroTimbrado) { this.numeroTimbrado = numeroTimbrado; }

    public LocalDate getFechaInicioTimbrado() { return fechaInicioTimbrado; }
    public void setFechaInicioTimbrado(LocalDate fechaInicioTimbrado) { this.fechaInicioTimbrado = fechaInicioTimbrado; }

    public String getCodigoEstablecimiento() { return codigoEstablecimiento; }
    public void setCodigoEstablecimiento(String codigoEstablecimiento) { this.codigoEstablecimiento = codigoEstablecimiento; }

    public String getCodigoPuntoExpedicion() { return codigoPuntoExpedicion; }
    public void setCodigoPuntoExpedicion(String codigoPuntoExpedicion) { this.codigoPuntoExpedicion = codigoPuntoExpedicion; }

    public String getEmisorRazonSocial() { return emisorRazonSocial; }
    public void setEmisorRazonSocial(String emisorRazonSocial) { this.emisorRazonSocial = emisorRazonSocial; }

    public String getEmisorRuc() { return emisorRuc; }
    public void setEmisorRuc(String emisorRuc) { this.emisorRuc = emisorRuc; }

    public String getEmisorDireccion() { return emisorDireccion; }
    public void setEmisorDireccion(String emisorDireccion) { this.emisorDireccion = emisorDireccion; }

    public Integer getEmisorCiudadId() { return emisorCiudadId; }
    public void setEmisorCiudadId(Integer emisorCiudadId) { this.emisorCiudadId = emisorCiudadId; }

    public String getEmisorCiudadDesc() { return emisorCiudadDesc; }
    public void setEmisorCiudadDesc(String emisorCiudadDesc) { this.emisorCiudadDesc = emisorCiudadDesc; }

    public String getEmisorTelefono() { return emisorTelefono; }
    public void setEmisorTelefono(String emisorTelefono) { this.emisorTelefono = emisorTelefono; }

    public String getEmisorEmail() { return emisorEmail; }
    public void setEmisorEmail(String emisorEmail) { this.emisorEmail = emisorEmail; }

    public String getReceptorRazonSocial() { return receptorRazonSocial; }
    public void setReceptorRazonSocial(String receptorRazonSocial) { this.receptorRazonSocial = receptorRazonSocial; }

    public String getReceptorDocumentoNumero() { return receptorDocumentoNumero; }
    public void setReceptorDocumentoNumero(String receptorDocumentoNumero) { this.receptorDocumentoNumero = receptorDocumentoNumero; }

    public String getReceptorTipoDocumentoDesc() { return receptorTipoDocumentoDesc; }
    public void setReceptorTipoDocumentoDesc(String receptorTipoDocumentoDesc) { this.receptorTipoDocumentoDesc = receptorTipoDocumentoDesc; }

    public Integer getReceptorTipoDocumentoCodigo() { return receptorTipoDocumentoCodigo; }
    public void setReceptorTipoDocumentoCodigo(Integer receptorTipoDocumentoCodigo) { this.receptorTipoDocumentoCodigo = receptorTipoDocumentoCodigo; }

    public String getReceptorDireccion() { return receptorDireccion; }
    public void setReceptorDireccion(String receptorDireccion) { this.receptorDireccion = receptorDireccion; }

    public String getReceptorEmail() { return receptorEmail; }
    public void setReceptorEmail(String receptorEmail) { this.receptorEmail = receptorEmail; }

    public String getReceptorTelefono() { return receptorTelefono; }
    public void setReceptorTelefono(String receptorTelefono) { this.receptorTelefono = receptorTelefono; }

    public Integer getNaturalezaReceptor() { return naturalezaReceptor; }
    public void setNaturalezaReceptor(Integer naturalezaReceptor) { this.naturalezaReceptor = naturalezaReceptor; }

    public Integer getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(Integer tipoOperacion) { this.tipoOperacion = tipoOperacion; }

    public Integer getMotivoEmision() { return motivoEmision; }
    public void setMotivoEmision(Integer motivoEmision) { this.motivoEmision = motivoEmision; }

    public BigDecimal getTotalOperacion() { return totalOperacion; }
    public void setTotalOperacion(BigDecimal totalOperacion) { this.totalOperacion = totalOperacion; }

    public List<DetalleNotaCreditoJson> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleNotaCreditoJson> detalles) { this.detalles = detalles; }

    public DocumentoAsociadoJson getDocumentoAsociado() { return documentoAsociado; }
    public void setDocumentoAsociado(DocumentoAsociadoJson documentoAsociado) { this.documentoAsociado = documentoAsociado; }

    public String getReceptorDistritoDesc() { return receptorDistritoDesc; }
    public void setReceptorDistritoDesc(String receptorDistritoDesc) { this.receptorDistritoDesc = receptorDistritoDesc; }

    public Integer getReceptorDistritoId() { return receptorDistritoId; }
    public void setReceptorDistritoId(Integer receptorDistritoId) { this.receptorDistritoId = receptorDistritoId; }

    public String getReceptorDepartamentoDesc() { return receptorDepartamentoDesc; }
    public void setReceptorDepartamentoDesc(String receptorDepartamentoDesc) { this.receptorDepartamentoDesc = receptorDepartamentoDesc; }

    public Integer getReceptorDepartamentoId() { return receptorDepartamentoId; }
    public void setReceptorDepartamentoId(Integer receptorDepartamentoId) { this.receptorDepartamentoId = receptorDepartamentoId; }

    public String getReceptorCiudadDesc() { return receptorCiudadDesc; }
    public void setReceptorCiudadDesc(String receptorCiudadDesc) { this.receptorCiudadDesc = receptorCiudadDesc; }

    public Integer getReceptorCiudadId() { return receptorCiudadId; }
    public void setReceptorCiudadId(Integer receptorCiudadId) { this.receptorCiudadId = receptorCiudadId; }

    
    
    // ==========================================
    // CLASES INTERNAS (DTOs para JSON)
    // ==========================================

    public String getMoneda() {
		return moneda;
	}
	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}
	public Integer getTipoCambio() {
		return tipoCambio;
	}
	public void setTipoCambio(Integer tipoCambio) {
		this.tipoCambio = tipoCambio;
	}



	/**
     * Mapea cada ítem del array JSON 'detalles_json'
     */
    @JsonIgnoreProperties(ignoreUnknown = true) // ESTO ES CLAVE PARA EVITAR ERRORES FUTUROS
    public static class DetalleNotaCreditoJson implements Serializable {
        @JsonProperty("item_id")
        private Long itemId;

        @JsonProperty("codigo_interno")
        private String codigoInterno;

        @JsonProperty("descripcion")
        private String descripcion;

        @JsonProperty("cantidad")
        private BigDecimal cantidad;

        @JsonProperty("precio_unitario")
        private BigDecimal precioUnitario;

        // --- CAMPO AGREGADO PARA CORREGIR EL ERROR ---
        @JsonProperty("descuento")
        private BigDecimal descuento;
        // ---------------------------------------------

        @JsonProperty("subtotal")
        private BigDecimal subtotal;

        @JsonProperty("tasa_iva")
        private Integer tasaIva;

        @JsonProperty("afectacion_iva")
        private Integer afectacionIva;

        @JsonProperty("unidad_medida_cod")
        private Integer unidadMedidaCod;

        // Getters y Setters
        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }

        public String getCodigoInterno() { return codigoInterno; }
        public void setCodigoInterno(String codigoInterno) { this.codigoInterno = codigoInterno; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public BigDecimal getCantidad() { return cantidad; }
        public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

        // GETTER Y SETTER PARA DESCUENTO
        public BigDecimal getDescuento() { return descuento; }
        public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }

        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

        public Integer getTasaIva() { return tasaIva; }
        public void setTasaIva(Integer tasaIva) { this.tasaIva = tasaIva; }

        public Integer getAfectacionIva() { return afectacionIva; }
        public void setAfectacionIva(Integer afectacionIva) { this.afectacionIva = afectacionIva; }

        public Integer getUnidadMedidaCod() { return unidadMedidaCod; }
        public void setUnidadMedidaCod(Integer unidadMedidaCod) { this.unidadMedidaCod = unidadMedidaCod; }
    }

    /**
     * Mapea el objeto JSON 'documento_asociado_json'
     */
    public static class DocumentoAsociadoJson implements Serializable {
        
        @JsonProperty("tipo_documento_asoc")
        private Integer tipoDocumentoAsoc; // 1=Electrónico, 2=Impreso

        @JsonProperty("cdc_asoc")
        private String cdcAsoc;

        @JsonProperty("timbrado_asoc")
        private Long timbradoAsoc;

        @JsonProperty("numero_asoc")
        private Integer numeroAsoc;

        @JsonProperty("fecha_emision_asoc")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate fechaEmisionAsoc;

        // Getters y Setters
        public Integer getTipoDocumentoAsoc() { return tipoDocumentoAsoc; }
        public void setTipoDocumentoAsoc(Integer tipoDocumentoAsoc) { this.tipoDocumentoAsoc = tipoDocumentoAsoc; }

        public String getCdcAsoc() { return cdcAsoc; }
        public void setCdcAsoc(String cdcAsoc) { this.cdcAsoc = cdcAsoc; }

        public Long getTimbradoAsoc() { return timbradoAsoc; }
        public void setTimbradoAsoc(Long timbradoAsoc) { this.timbradoAsoc = timbradoAsoc; }

        public Integer getNumeroAsoc() { return numeroAsoc; }
        public void setNumeroAsoc(Integer numeroAsoc) { this.numeroAsoc = numeroAsoc; }

        public LocalDate getFechaEmisionAsoc() { return fechaEmisionAsoc; }
        public void setFechaEmisionAsoc(LocalDate fechaEmisionAsoc) { this.fechaEmisionAsoc = fechaEmisionAsoc; }
    }
}