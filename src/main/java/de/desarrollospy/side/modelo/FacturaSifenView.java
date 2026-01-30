package de.desarrollospy.side.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "v_datos_sifen_factura", schema = "public")
@Immutable // Entidad de solo lectura para mapear la vista SQL
public class FacturaSifenView implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "factura_id")
    private Long facturaId;

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

    // --- 4. Condición de Venta ---
    @Column(name = "condicion_tipo")
    private Integer condicionTipo; // 1=Contado, 2=Crédito

    @Column(name = "cantidad_cuotas")
    private Integer cantidadCuotas;

    @Column(name = "credito_plazo")
    private String creditoPlazo;

    // --- 6. Totales (Calculados en Vista) ---
    @Column(name = "total_operacion")
    private BigDecimal totalOperacion;

    // --- 5. DETALLES (JSON Mágico) ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalles_json")
    private List<DetalleFacturaJson> detalles = new ArrayList<>();

    // --- 7. DOCUMENTOS ASOCIADOS (JSON Mágico) ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "documentos_asociados_json")
    private List<DocumentoAsociadoJson> documentosAsociados = new ArrayList<>();
    
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
   
    @Column(name = "moneda")
    private String moneda;
    
    @Column(name = "tipo_cambio")
    private Integer tipoCambio;

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================
    
    

    public Long getFacturaId() { return facturaId; }
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
	public void setFacturaId(Long facturaId) { this.facturaId = facturaId; }

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

    public Integer getCondicionTipo() { return condicionTipo; }
    public void setCondicionTipo(Integer condicionTipo) { this.condicionTipo = condicionTipo; }

    public Integer getCantidadCuotas() { return cantidadCuotas; }
    public void setCantidadCuotas(Integer cantidadCuotas) { this.cantidadCuotas = cantidadCuotas; }

    public String getCreditoPlazo() { return creditoPlazo; }
    public void setCreditoPlazo(String creditoPlazo) { this.creditoPlazo = creditoPlazo; }

    public BigDecimal getTotalOperacion() { return totalOperacion; }
    public void setTotalOperacion(BigDecimal totalOperacion) { this.totalOperacion = totalOperacion; }

    public List<DetalleFacturaJson> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleFacturaJson> detalles) { this.detalles = detalles; }

    public List<DocumentoAsociadoJson> getDocumentosAsociados() { return documentosAsociados; }
    public void setDocumentosAsociados(List<DocumentoAsociadoJson> documentosAsociados) { this.documentosAsociados = documentosAsociados; }

    // ==========================================
    // CLASES INTERNAS (DTOs para JSON)
    // ==========================================

    /**
     * Mapea cada ítem del array JSON 'detalles_json'
     */
    public static class DetalleFacturaJson implements Serializable {
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

        @JsonProperty("descuento")
        private BigDecimal descuento;
        
        @JsonProperty("subtotal")
        private BigDecimal subtotal;

        @JsonProperty("tasa_iva")
        private Integer tasaIva;        // 5, 10, 0
        
        @JsonProperty("afectacion_iva")
        private Integer afectacionIva;  // 1=Gravada, 3=Exenta

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
     * Mapea cada ítem del array JSON 'documentos_asociados_json'
     */
    public static class DocumentoAsociadoJson implements Serializable {
        @JsonProperty("tipo_documento")
        private String tipoDocumento;

        @JsonProperty("cdc")
        private String cdc;
        
        @JsonProperty("numero")
        private Integer numero;

        @JsonProperty("fecha_emision")
        private LocalDateTime fechaEmision;

        // Getters y Setters
        public String getTipoDocumento() { return tipoDocumento; }
        public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

        public String getCdc() { return cdc; }
        public void setCdc(String cdc) { this.cdc = cdc; }
        
        public Integer getNumero() { return numero; }
        public void setNumero(Integer numero) { this.numero = numero; }

        public LocalDateTime getFechaEmision() { return fechaEmision; }
        public void setFechaEmision(LocalDateTime fechaEmision) { this.fechaEmision = fechaEmision; }
    }

	public String getReceptorDistritoDesc() {
		return receptorDistritoDesc;
	}
	public void setReceptorDistritoDesc(String receptorDistritoDesc) {
		this.receptorDistritoDesc = receptorDistritoDesc;
	}
	public Integer getReceptorDistritoId() {
		return receptorDistritoId;
	}
	public void setReceptorDistritoId(Integer receptorDistritoId) {
		this.receptorDistritoId = receptorDistritoId;
	}
	public String getReceptorDepartamentoDesc() {
		return receptorDepartamentoDesc;
	}
	public void setReceptorDepartamentoDesc(String receptorDepartamentoDesc) {
		this.receptorDepartamentoDesc = receptorDepartamentoDesc;
	}
	public Integer getReceptorDepartamentoId() {
		return receptorDepartamentoId;
	}
	public void setReceptorDepartamentoId(Integer receptorDepartamentoId) {
		this.receptorDepartamentoId = receptorDepartamentoId;
	}
	public String getReceptorCiudadDesc() {
		return receptorCiudadDesc;
	}
	public void setReceptorCiudadDesc(String receptorCiudadDesc) {
		this.receptorCiudadDesc = receptorCiudadDesc;
	}
	public Integer getReceptorCiudadId() {
		return receptorCiudadId;
	}
	public void setReceptorCiudadId(Integer receptorCiudadId) {
		this.receptorCiudadId = receptorCiudadId;
	}
    
    
}