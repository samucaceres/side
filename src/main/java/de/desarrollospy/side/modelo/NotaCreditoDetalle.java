package de.desarrollospy.side.modelo;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "v_det_nota_credito_edocs", schema = "public")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotaCreditoDetalle implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "cod_int")
	private String codInt;
	@Column(name = "id_ncred")
	private String idNcred;
	@Column(name = "descri_cta")
	private String descriCta;
	@Column(name = "det_pro_ser")
	private String detProSer;
	private int cantidad;
	private double monto;
	private double exenta;
	private double gravada5;
	private double gravada10;
	private double iva5;
	private double iva10;
	@Column(name = "factura")
	private String factura;
	@Column(name = "cdc_factura")
	private String cdcFactura;
	@Column(name = "d_n_tim_d_i")
	private String dNTimDI;
	@Column(name = "d_est_doc_aso")
	private String dEstDocAso;
	@Column(name = "d_p_exp_doc_aso")
	private String dPExpDocAso;
	@Column(name = "d_num_doc_aso")
	private String dNumDocAso;
	@Column(name = "d_fec_emi_d_i")
	private LocalDate dFecEmiDI;
	@Column(name = "des_nro_cta")
	private double desNroCta;
	@Column(name = "nro_factura")
	private String nroFactura;
	@Column(name = "motivo")
	private String motivo;
	
	public String getIdNcred() {
		return idNcred;
	}
	public void setIdNcred(String idNcred) {
		this.idNcred = idNcred;
	}
	public String getDescriCta() {
		return descriCta;
	}
	public void setDescriCta(String descriCta) {
		this.descriCta = descriCta;
	}
	public String getCodInt() {
		return codInt;
	}
	public void setCodInt(String codInt) {
		this.codInt = codInt;
	}
	public String getDetProSer() {
		return detProSer;
	}
	public void setDetProSer(String detProSer) {
		this.detProSer = detProSer;
	}
	public int getCantidad() {
		return cantidad;
	}
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
	public double getMonto() {
		return monto;
	}
	public void setMonto(double monto) {
		this.monto = monto;
	}
	public double getExenta() {
		return exenta;
	}
	public void setExenta(double exenta) {
		this.exenta = exenta;
	}
	public double getGravada5() {
		return gravada5;
	}
	public void setGravada5(double gravada5) {
		this.gravada5 = gravada5;
	}
	public double getGravada10() {
		return gravada10;
	}
	public void setGravada10(double gravada10) {
		this.gravada10 = gravada10;
	}
	public double getIva5() {
		return iva5;
	}
	public void setIva5(double iva5) {
		this.iva5 = iva5;
	}
	public double getIva10() {
		return iva10;
	}
	public void setIva10(double iva10) {
		this.iva10 = iva10;
	}
	public String getCdcFactura() {
		return cdcFactura;
	}
	public void setCdcFactura(String cdcFactura) {
		this.cdcFactura = cdcFactura;
	}
	public String getdNTimDI() {
		return dNTimDI;
	}
	public void setdNTimDI(String dNTimDI) {
		this.dNTimDI = dNTimDI;
	}
	public String getdEstDocAso() {
		return dEstDocAso;
	}
	public void setdEstDocAso(String dEstDocAso) {
		this.dEstDocAso = dEstDocAso;
	}
	public String getdPExpDocAso() {
		return dPExpDocAso;
	}
	public void setdPExpDocAso(String dPExpDocAso) {
		this.dPExpDocAso = dPExpDocAso;
	}
	public String getdNumDocAso() {
		return dNumDocAso;
	}
	public void setdNumDocAso(String dNumDocAso) {
		this.dNumDocAso = dNumDocAso;
	}
	public LocalDate getdFecEmiDI() {
		return dFecEmiDI;
	}
	public void setdFecEmiDI(LocalDate dFecEmiDI) {
		this.dFecEmiDI = dFecEmiDI;
	}
	public String getFactura() {
		return factura;
	}
	public void setFactura(String factura) {
		this.factura = factura;
	}
	public double getDesNroCta() {
		return desNroCta;
	}
	public void setDesNroCta(double desNroCta) {
		this.desNroCta = desNroCta;
	}
	public String getNroFactura() {
		return nroFactura;
	}
	public void setNroFactura(String nroFactura) {
		this.nroFactura = nroFactura;
	}
	public String getMotivo() {
		return motivo;
	}
	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
	
}