package de.desarrollospy.side.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "v_datos_sifen_remision", schema = "public")
@Immutable // Indica a Hibernate que esta entidad no se debe actualizar/guardar, solo leer
public class RemisionSifenView implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "remision_id")
    private Long remisionId;

    // --- Identificación ---
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

    // --- Emisor ---
    @Column(name = "emisor_razon_social")
    private String emisorRazonSocial;

    @Column(name = "emisor_ruc")
    private String emisorRuc;

    @Column(name = "emisor_direccion")
    private String emisorDireccion;

    @Column(name = "emisor_ciudad_desc")
    private String emisorCiudadDesc;
    
    @Column(name = "emisor_ciudad_id")
    private Integer emisorCiudadId;
    
    @Column(name = "emisor_telefono")
    private String emisorTelefono;

    @Column(name = "emisor_email")
    private String emisorEmail;

    // --- Receptor ---
    @Column(name = "receptor_razon_social")
    private String receptorRazonSocial;

    @Column(name = "receptor_documento_numero")
    private String receptorDocumentoNumero;

    @Column(name = "receptor_tipo_documento_codigo")
    private Integer receptorTipoDocumentoCodigo;

    @Column(name = "receptor_direccion")
    private String receptorDireccion;

    @Column(name = "receptor_email")
    private String receptorEmail;

    // --- Datos Logísticos (Cabecera) ---
    @Column(name = "motivo_emision")
    private Integer motivoEmision;

    @Column(name = "responsable_emision")
    private Integer responsableEmision;

    @Column(name = "fecha_inicio_traslado")
    private LocalDateTime fechaInicioTraslado;

    @Column(name = "fecha_fin_traslado")
    private LocalDateTime fechaFinTraslado;

    @Column(name = "km_estimados")
    private Integer kmEstimados;

    // --- Transporte ---
    @Column(name = "tipo_transporte")
    private Integer tipoTransporte;

    @Column(name = "modalidad_transporte")
    private Integer modalidadTransporte;

    @Column(name = "vehiculo_matricula")
    private String vehiculoMatricula;

    @Column(name = "vehiculo_marca")
    private String vehiculoMarca;

    @Column(name = "conductor_nombre")
    private String conductorNombre;

    @Column(name = "conductor_documento")
    private String conductorDocumento;

    @Column(name = "transportista_ruc")
    private String transportistaRuc;

    @Column(name = "transportista_razon_social")
    private String transportistaRazonSocial;
    
    @Column(name = "fecha_futura_facturacion")
    private LocalDateTime fechaFuturaFacturacion;

    // --- DETALLES (El campo JSON Mágico) ---
    // Esta anotación convierte automáticamente el JSONB de Postgres a una Lista Java
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalles_json")
    private List<DetalleItemJson> detalles = new ArrayList<>();

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

    @Column(name = "conductor_distrito_desc")
    private String conductorDistritoDesc;
    
    @Column(name = "conductor_distrito_id")
    private Integer conductorDistritoId;

    @Column(name = "conductor_departamento_desc")
    private String conductorDepartamentoDesc;
    
    @Column(name = "conductor_departamento_id")
    private Integer conductorDepartamentoId;

    @Column(name = "conductor_ciudad_desc")
    private String conductorCiudadDesc;
    
    @Column(name = "conductor_ciudad_id")
    private Integer conductorCiudadId;

    // --- Getters y Setters ---
    public Long getRemisionId() { return remisionId; }
    public void setRemisionId(Long remisionId) { this.remisionId = remisionId; }

    public Integer getNumeroDocumento() {
		return numeroDocumento;
	}
	public void setNumeroDocumento(Integer numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}
	public LocalDateTime getFechaEmision() {
		return fechaEmision;
	}
	public void setFechaEmision(LocalDateTime fechaEmision) {
		this.fechaEmision = fechaEmision;
	}
	public String getCdc() {
		return cdc;
	}
	public void setCdc(String cdc) {
		this.cdc = cdc;
	}
	public String getEstadoDnit() {
		return estadoDnit;
	}
	public void setEstadoDnit(String estadoDnit) {
		this.estadoDnit = estadoDnit;
	}
	public Long getNumeroTimbrado() {
		return numeroTimbrado;
	}
	public void setNumeroTimbrado(Long numeroTimbrado) {
		this.numeroTimbrado = numeroTimbrado;
	}
	public String getCodigoEstablecimiento() {
		return codigoEstablecimiento;
	}
	public void setCodigoEstablecimiento(String codigoEstablecimiento) {
		this.codigoEstablecimiento = codigoEstablecimiento;
	}
	public String getCodigoPuntoExpedicion() {
		return codigoPuntoExpedicion;
	}
	public void setCodigoPuntoExpedicion(String codigoPuntoExpedicion) {
		this.codigoPuntoExpedicion = codigoPuntoExpedicion;
	}
	public String getEmisorRazonSocial() {
		return emisorRazonSocial;
	}
	public void setEmisorRazonSocial(String emisorRazonSocial) {
		this.emisorRazonSocial = emisorRazonSocial;
	}
	public String getEmisorRuc() {
		return emisorRuc;
	}
	public void setEmisorRuc(String emisorRuc) {
		this.emisorRuc = emisorRuc;
	}
	public String getEmisorDireccion() {
		return emisorDireccion;
	}
	public void setEmisorDireccion(String emisorDireccion) {
		this.emisorDireccion = emisorDireccion;
	}
	public String getEmisorCiudadDesc() {
		return emisorCiudadDesc;
	}
	public void setEmisorCiudadDesc(String emisorCiudadDesc) {
		this.emisorCiudadDesc = emisorCiudadDesc;
	}
	public String getReceptorRazonSocial() {
		return receptorRazonSocial;
	}
	public void setReceptorRazonSocial(String receptorRazonSocial) {
		this.receptorRazonSocial = receptorRazonSocial;
	}
	public String getReceptorDocumentoNumero() {
		return receptorDocumentoNumero;
	}
	public void setReceptorDocumentoNumero(String receptorDocumentoNumero) {
		this.receptorDocumentoNumero = receptorDocumentoNumero;
	}
	public Integer getReceptorTipoDocumentoCodigo() {
		return receptorTipoDocumentoCodigo;
	}
	public void setReceptorTipoDocumentoCodigo(Integer receptorTipoDocumentoCodigo) {
		this.receptorTipoDocumentoCodigo = receptorTipoDocumentoCodigo;
	}
	public String getReceptorDireccion() {
		return receptorDireccion;
	}
	public void setReceptorDireccion(String receptorDireccion) {
		this.receptorDireccion = receptorDireccion;
	}
	public String getReceptorEmail() {
		return receptorEmail;
	}
	public void setReceptorEmail(String receptorEmail) {
		this.receptorEmail = receptorEmail;
	}
	public Integer getMotivoEmision() {
		return motivoEmision;
	}
	public void setMotivoEmision(Integer motivoEmision) {
		this.motivoEmision = motivoEmision;
	}
	public Integer getResponsableEmision() {
		return responsableEmision;
	}
	public void setResponsableEmision(Integer responsableEmision) {
		this.responsableEmision = responsableEmision;
	}
	public LocalDateTime getFechaInicioTraslado() {
		return fechaInicioTraslado;
	}
	public void setFechaInicioTraslado(LocalDateTime fechaInicioTraslado) {
		this.fechaInicioTraslado = fechaInicioTraslado;
	}
	public LocalDateTime getFechaFinTraslado() {
		return fechaFinTraslado;
	}
	public void setFechaFinTraslado(LocalDateTime fechaFinTraslado) {
		this.fechaFinTraslado = fechaFinTraslado;
	}
	public Integer getKmEstimados() {
		return kmEstimados;
	}
	public void setKmEstimados(Integer kmEstimados) {
		this.kmEstimados = kmEstimados;
	}
	public Integer getTipoTransporte() {
		return tipoTransporte;
	}
	public void setTipoTransporte(Integer tipoTransporte) {
		this.tipoTransporte = tipoTransporte;
	}
	public Integer getModalidadTransporte() {
		return modalidadTransporte;
	}
	public void setModalidadTransporte(Integer modalidadTransporte) {
		this.modalidadTransporte = modalidadTransporte;
	}
	public String getVehiculoMatricula() {
		return vehiculoMatricula;
	}
	public void setVehiculoMatricula(String vehiculoMatricula) {
		this.vehiculoMatricula = vehiculoMatricula;
	}
	public String getVehiculoMarca() {
		return vehiculoMarca;
	}
	public void setVehiculoMarca(String vehiculoMarca) {
		this.vehiculoMarca = vehiculoMarca;
	}
	public String getConductorNombre() {
		return conductorNombre;
	}
	public void setConductorNombre(String conductorNombre) {
		this.conductorNombre = conductorNombre;
	}
	public String getConductorDocumento() {
		return conductorDocumento;
	}
	public void setConductorDocumento(String conductorDocumento) {
		this.conductorDocumento = conductorDocumento;
	}
	public String getTransportistaRuc() {
		return transportistaRuc;
	}
	public void setTransportistaRuc(String transportistaRuc) {
		this.transportistaRuc = transportistaRuc;
	}
	public String getTransportistaRazonSocial() {
		return transportistaRazonSocial;
	}
	public void setTransportistaRazonSocial(String transportistaRazonSocial) {
		this.transportistaRazonSocial = transportistaRazonSocial;
	}
	public List<DetalleItemJson> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleItemJson> detalles) { this.detalles = detalles; }

    // --- CLASE INTERNA PARA EL JSON (POJO) ---
    // Esta clase mapea la estructura del objeto JSON que creamos en la vista SQL
    public static class DetalleItemJson implements Serializable {
        
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

        @JsonProperty("unidad_medida_cod")
        private Integer unidadMedidaCod;

        @JsonProperty("peso")
        private BigDecimal peso;

        // Getters y Setters del POJO
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

        public Integer getUnidadMedidaCod() { return unidadMedidaCod; }
        public void setUnidadMedidaCod(Integer unidadMedidaCod) { this.unidadMedidaCod = unidadMedidaCod; }
        
        public BigDecimal getPeso() { return peso; }
        public void setPeso(BigDecimal peso) { this.peso = peso; }
    }

	public LocalDate getFechaInicioTimbrado() {
		return fechaInicioTimbrado;
	}
	public void setFechaInicioTimbrado(LocalDate fechaInicioTimbrado) {
		this.fechaInicioTimbrado = fechaInicioTimbrado;
	}
	
	public LocalDateTime getFechaFuturaFacturacion() { return fechaFuturaFacturacion; }
    public void setFechaFuturaFacturacion(LocalDateTime f) { this.fechaFuturaFacturacion = f; }
	public Integer getEmisorCiudadId() {
		return emisorCiudadId;
	}
	public void setEmisorCiudadId(Integer emisorCiudadId) {
		this.emisorCiudadId = emisorCiudadId;
	}
	public String getEmisorTelefono() {
		return emisorTelefono;
	}
	public void setEmisorTelefono(String emisorTelefono) {
		this.emisorTelefono = emisorTelefono;
	}
	public String getEmisorEmail() {
		return emisorEmail;
	}
	public void setEmisorEmail(String emisorEmail) {
		this.emisorEmail = emisorEmail;
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
	public String getConductorDistritoDesc() {
		return conductorDistritoDesc;
	}
	public void setConductorDistritoDesc(String conductorDistritoDesc) {
		this.conductorDistritoDesc = conductorDistritoDesc;
	}
	public Integer getConductorDistritoId() {
		return conductorDistritoId;
	}
	public void setConductorDistritoId(Integer conductorDistritoId) {
		this.conductorDistritoId = conductorDistritoId;
	}
	public String getConductorDepartamentoDesc() {
		return conductorDepartamentoDesc;
	}
	public void setConductorDepartamentoDesc(String conductorDepartamentoDesc) {
		this.conductorDepartamentoDesc = conductorDepartamentoDesc;
	}
	public Integer getConductorDepartamentoId() {
		return conductorDepartamentoId;
	}
	public void setConductorDepartamentoId(Integer conductorDepartamentoId) {
		this.conductorDepartamentoId = conductorDepartamentoId;
	}
	public String getConductorCiudadDesc() {
		return conductorCiudadDesc;
	}
	public void setConductorCiudadDesc(String conductorCiudadDesc) {
		this.conductorCiudadDesc = conductorCiudadDesc;
	}
	public Integer getConductorCiudadId() {
		return conductorCiudadId;
	}
	public void setConductorCiudadId(Integer conductorCiudadId) {
		this.conductorCiudadId = conductorCiudadId;
	}
    
    
}