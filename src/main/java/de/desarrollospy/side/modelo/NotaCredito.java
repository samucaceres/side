package de.desarrollospy.side.modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "v_nota_credito_edocs", schema = "public")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotaCredito implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name = "fecha_emision")
	private Date fechaEmision;

	@Column(name = "estado")
	private String estado;

	@Column(name = "id_persona")
	private int idPersona;

	@Column(name = "a_nombre_de")
	private String aNombreDe;

	@Column(name = "ruc")
	private String ruc;

	@Column(name = "total")
	private int total;

	@Column(name = "tiva5")
	private int tiva5;

	@Column(name = "tiva10")
	private int tiva10;

	@Column(name = "ivatotal")
	private int ivatotal;

	@Column(name = "motivo")
	private String motivo;

	@Column(name = "obs")
	private String obs;

	@Column(name = "establecimiento")
	private String establecimiento;

	@Column(name = "punto_exp")
	private String puntoExp;

	@Column(name = "nro_nc")
	private String nroNc;

	@Column(name = "est_perso")
	private String estPerso;

	@Column(name = "abrev_pais")
	private String abrevPais;

	@Column(name = "fec_ini_timb")
	private Date fecIniTimb;

	@Column(name = "tipo_doc_persona")
	private String tipoDocPersona;

	@Column(name = "per_direcc")
	private String perDirecc;
	
	@Column(name = "d_fe_ini_t")
	private LocalDate dFeIniT;

	@Column(name = "ncred_timbrado")
	private int ncredTimbrado;
	
	@Column(name = "id_ciudad_sede")
	private int idCiudadSede;
	
	@Column(name = "ciudad_sede")
	private String ciudadSede;	
	
	@Column(name = "id_distrito_sede")
	private int idDistritoSede;
	
	@Column(name = "distrito_sede")
	private String distritoSede;	
	
	@Column(name = "id_departamento_sede")
	private int idDepartamentoSede;
	
	@Column(name = "departamento_sede")
	private String departamentoSede;
	
	@Column(name = "sed_direcc")
	private String sedDirecc;
	
	@Column(name = "sed_tel")
	private String sedTel;	
	
	@Column(name = "nota_fechahora")
	private LocalDateTime notaFechahora;
	
	@Column(name = "per_telmov")
	private String perTelmov;
	
	@Column(name = "per_email")
	private String perEmail;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(Date fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public int getIdPersona() {
		return idPersona;
	}

	public void setIdPersona(int idPersona) {
		this.idPersona = idPersona;
	}

	public String getaNombreDe() {
		return aNombreDe;
	}

	public void setaNombreDe(String aNombreDe) {
		this.aNombreDe = aNombreDe;
	}

	public String getRuc() {
		return ruc;
	}

	public void setRuc(String ruc) {
		this.ruc = ruc;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getTiva5() {
		return tiva5;
	}

	public void setTiva5(int tiva5) {
		this.tiva5 = tiva5;
	}

	public int getTiva10() {
		return tiva10;
	}

	public void setTiva10(int tiva10) {
		this.tiva10 = tiva10;
	}

	public int getIvatotal() {
		return ivatotal;
	}

	public void setIvatotal(int ivatotal) {
		this.ivatotal = ivatotal;
	}

	public String getMotivo() {
		return motivo;
	}

	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}

	public String getObs() {
		return obs;
	}

	public void setObs(String obs) {
		this.obs = obs;
	}

	public String getEstablecimiento() {
		return establecimiento;
	}

	public void setEstablecimiento(String establecimiento) {
		this.establecimiento = establecimiento;
	}

	public String getPuntoExp() {
		return puntoExp;
	}

	public void setPuntoExp(String puntoExp) {
		this.puntoExp = puntoExp;
	}

	public String getNroNc() {
		return nroNc;
	}

	public void setNroNc(String nroNc) {
		this.nroNc = nroNc;
	}

	public String getEstPerso() {
		return estPerso;
	}

	public void setEstPerso(String estPerso) {
		this.estPerso = estPerso;
	}

	public String getAbrevPais() {
		return abrevPais;
	}

	public void setAbrevPais(String abrevPais) {
		this.abrevPais = abrevPais;
	}

	public Date getFecIniTimb() {
		return fecIniTimb;
	}

	public void setFecIniTimb(Date fecIniTimb) {
		this.fecIniTimb = fecIniTimb;
	}

	public String getTipoDocPersona() {
		return tipoDocPersona;
	}

	public void setTipoDocPersona(String tipoDocPersona) {
		this.tipoDocPersona = tipoDocPersona;
	}

	public String getPerDirecc() {
		return perDirecc;
	}

	public void setPerDirecc(String perDirecc) {
		this.perDirecc = perDirecc;
	}

	public LocalDate getdFeIniT() {
		return dFeIniT;
	}

	public void setdFeIniT(LocalDate dFeIniT) {
		this.dFeIniT = dFeIniT;
	}

	public int getNcredTimbrado() {
		return ncredTimbrado;
	}

	public void setNcredTimbrado(int ncredTimbrado) {
		this.ncredTimbrado = ncredTimbrado;
	}

	public int getIdCiudadSede() {
		return idCiudadSede;
	}

	public void setIdCiudadSede(int idCiudadSede) {
		this.idCiudadSede = idCiudadSede;
	}

	public String getCiudadSede() {
		return ciudadSede;
	}

	public void setCiudadSede(String ciudadSede) {
		this.ciudadSede = ciudadSede;
	}

	public int getIdDistritoSede() {
		return idDistritoSede;
	}

	public void setIdDistritoSede(int idDistritoSede) {
		this.idDistritoSede = idDistritoSede;
	}

	public String getDistritoSede() {
		return distritoSede;
	}

	public void setDistritoSede(String distritoSede) {
		this.distritoSede = distritoSede;
	}

	public int getIdDepartamentoSede() {
		return idDepartamentoSede;
	}

	public void setIdDepartamentoSede(int idDepartamentoSede) {
		this.idDepartamentoSede = idDepartamentoSede;
	}

	public String getDepartamentoSede() {
		return departamentoSede;
	}

	public void setDepartamentoSede(String departamentoSede) {
		this.departamentoSede = departamentoSede;
	}

	public String getSedDirecc() {
		return sedDirecc;
	}

	public void setSedDirecc(String sedDirecc) {
		this.sedDirecc = sedDirecc;
	}

	public String getSedTel() {
		return sedTel;
	}

	public void setSedTel(String sedTel) {
		this.sedTel = sedTel;
	}

	public LocalDateTime getNotaFechahora() {
		return notaFechahora;
	}

	public void setNotaFechahora(LocalDateTime notaFechahora) {
		this.notaFechahora = notaFechahora;
	}

	public String getPerTelmov() {
		return perTelmov;
	}

	public void setPerTelmov(String perTelmov) {
		this.perTelmov = perTelmov;
	}

	public String getPerEmail() {
		return perEmail;
	}

	public void setPerEmail(String perEmail) {
		this.perEmail = perEmail;
	}

	
}