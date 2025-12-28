package de.desarrollospy.side.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "v_cobros_edocs", schema = "public")
@IdClass(CobroPK.class)
public class Cobro implements Serializable {
    
    private static final long serialVersionUID = 1L;

    // Clave Primaria Compuesta
    @Id @Column(name = "nro_cobro") private Integer nroCobro;
    @Id @Column(name = "id_sede") private Integer idSede;
    @Id @Column(name = "id_caja") private Integer idCaja;
    @Id @Column(name = "nro_apecie") private Integer nroApecie;
    @Id @Column(name = "id_funcio") private Integer idFuncio;

    // Resto de columnas
    @Column(name = "id") private String id; // Mantenemos por compatibilidad, pero no es @Id
    @Column(name = "nro_fact") private String nroFact;
    @Column(name = "id_docresp") private Integer idDocresp;
    @Column(name = "cob_fecha") private Date cobFecha;
    @Column(name = "estado") private String estado;
    @Column(name = "a_nombre_de") private String aNombreDe;
    @Column(name = "ruc") private String ruc;
    @Column(name = "total") private Integer total;
    @Column(name = "iva5") private Integer iva5;
    @Column(name = "iva10") private Integer iva10;
    @Column(name = "ivatotal") private Integer ivatotal;
    @Column(name = "ruc_de") private String rucDe;
    @Column(name = "nro_reci") private String nroReci;
    @Column(name = "id_persona") private Integer idPersona;
    @Column(name = "cobro_obs") private String cobroObs;
    @Column(name = "cob_fechahora") private LocalDateTime cobFechahora;
    @Column(name = "nro_timb") private Integer nroTimb;
    @Column(name = "fec_ven_timb") private LocalDate fecVenTimb;
    @Column(name = "fec_ini_timb") private LocalDate fecIniTimb;
    @Column(name = "nro_pcobro") private Integer nroPcobro;
    @Column(name = "nro_sucfac") private String nroSucfac;
    @Column(name = "nro_exped") private String nroExped;
    @Column(name = "nfact_timbrado") private String nfactTimbrado;
    @Column(name = "est_perso") private String estPerso;
    @Column(name = "nombre_pais") private String nombrePais;
    @Column(name = "abrev_pais") private String abrevPais;
    @Column(name = "tipo_doc_persona") private String tipoDocPersona;
    @Column(name = "per_direcc") private String perDirecc;
    @Column(name = "doresp_nomb") private String dorespNomb;
    @Column(name = "d_fe_ini_t") private LocalDate dFeIniT;
    @Column(name = "id_ciudad_sede") private Integer idCiudadSede;
    @Column(name = "ciudad_sede") private String ciudadSede;
    @Column(name = "id_distrito_sede") private Integer idDistritoSede;
    @Column(name = "distrito_sede") private String distritoSede;
    @Column(name = "id_departamento_sede") private Integer idDepartamentoSede;
    @Column(name = "departamento_sede") private String departamentoSede;
    @Column(name = "sed_direcc") private String sedDirecc;
    @Column(name = "sed_tel") private String sedTel;
    @Column(name = "per_telmov") private String perTelmov;
    @Column(name = "per_email") private String perEmail;

    // Getters y Setters
    public Integer getNroCobro() { return nroCobro; }
    public void setNroCobro(Integer nroCobro) { this.nroCobro = nroCobro; }
    public Integer getIdSede() { return idSede; }
    public void setIdSede(Integer idSede) { this.idSede = idSede; }
    public Integer getIdCaja() { return idCaja; }
    public void setIdCaja(Integer idCaja) { this.idCaja = idCaja; }
    public Integer getNroApecie() { return nroApecie; }
    public void setNroApecie(Integer nroApecie) { this.nroApecie = nroApecie; }
    public Integer getIdFuncio() { return idFuncio; }
    public void setIdFuncio(Integer idFuncio) { this.idFuncio = idFuncio; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNroFact() { return nroFact; }
    public void setNroFact(String nroFact) { this.nroFact = nroFact; }
    public Integer getIdDocresp() { return idDocresp; }
    public void setIdDocresp(Integer idDocresp) { this.idDocresp = idDocresp; }
    public Date getCobFecha() { return cobFecha; }
    public void setCobFecha(Date cobFecha) { this.cobFecha = cobFecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getaNombreDe() { return aNombreDe; }
    public void setaNombreDe(String aNombreDe) { this.aNombreDe = aNombreDe; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    public Integer getIva5() { return iva5; }
    public void setIva5(Integer iva5) { this.iva5 = iva5; }
    public Integer getIva10() { return iva10; }
    public void setIva10(Integer iva10) { this.iva10 = iva10; }
    public Integer getIvatotal() { return ivatotal; }
    public void setIvatotal(Integer ivatotal) { this.ivatotal = ivatotal; }
    public String getRucDe() { return rucDe; }
    public void setRucDe(String rucDe) { this.rucDe = rucDe; }
    public String getNroReci() { return nroReci; }
    public void setNroReci(String nroReci) { this.nroReci = nroReci; }
    public Integer getIdPersona() { return idPersona; }
    public void setIdPersona(Integer idPersona) { this.idPersona = idPersona; }
    public String getCobroObs() { return cobroObs; }
    public void setCobroObs(String cobroObs) { this.cobroObs = cobroObs; }
    public LocalDateTime getCobFechahora() { return cobFechahora; }
    public void setCobFechahora(LocalDateTime cobFechahora) { this.cobFechahora = cobFechahora; }
    public Integer getNroTimb() { return nroTimb; }
    public void setNroTimb(Integer nroTimb) { this.nroTimb = nroTimb; }
    public LocalDate getFecVenTimb() { return fecVenTimb; }
    public void setFecVenTimb(LocalDate fecVenTimb) { this.fecVenTimb = fecVenTimb; }
    public LocalDate getFecIniTimb() { return fecIniTimb; }
    public void setFecIniTimb(LocalDate fecIniTimb) { this.fecIniTimb = fecIniTimb; }
    public Integer getNroPcobro() { return nroPcobro; }
    public void setNroPcobro(Integer nroPcobro) { this.nroPcobro = nroPcobro; }
    public String getNroSucfac() { return nroSucfac; }
    public void setNroSucfac(String nroSucfac) { this.nroSucfac = nroSucfac; }
    public String getNroExped() { return nroExped; }
    public void setNroExped(String nroExped) { this.nroExped = nroExped; }
    public String getNfactTimbrado() { return nfactTimbrado; }
    public void setNfactTimbrado(String nfactTimbrado) { this.nfactTimbrado = nfactTimbrado; }
    public String getEstPerso() { return estPerso; }
    public void setEstPerso(String estPerso) { this.estPerso = estPerso; }
    public String getNombrePais() { return nombrePais; }
    public void setNombrePais(String nombrePais) { this.nombrePais = nombrePais; }
    public String getAbrevPais() { return abrevPais; }
    public void setAbrevPais(String abrevPais) { this.abrevPais = abrevPais; }
    public String getTipoDocPersona() { return tipoDocPersona; }
    public void setTipoDocPersona(String tipoDocPersona) { this.tipoDocPersona = tipoDocPersona; }
    public String getPerDirecc() { return perDirecc; }
    public void setPerDirecc(String perDirecc) { this.perDirecc = perDirecc; }
    public String getDorespNomb() { return dorespNomb; }
    public void setDorespNomb(String dorespNomb) { this.dorespNomb = dorespNomb; }
    public LocalDate getdFeIniT() { return dFeIniT; }
    public void setdFeIniT(LocalDate dFeIniT) { this.dFeIniT = dFeIniT; }
    public Integer getIdCiudadSede() { return idCiudadSede; }
    public void setIdCiudadSede(Integer idCiudadSede) { this.idCiudadSede = idCiudadSede; }
    public String getCiudadSede() { return ciudadSede; }
    public void setCiudadSede(String ciudadSede) { this.ciudadSede = ciudadSede; }
    public Integer getIdDistritoSede() { return idDistritoSede; }
    public void setIdDistritoSede(Integer idDistritoSede) { this.idDistritoSede = idDistritoSede; }
    public String getDistritoSede() { return distritoSede; }
    public void setDistritoSede(String distritoSede) { this.distritoSede = distritoSede; }
    public Integer getIdDepartamentoSede() { return idDepartamentoSede; }
    public void setIdDepartamentoSede(Integer idDepartamentoSede) { this.idDepartamentoSede = idDepartamentoSede; }
    public String getDepartamentoSede() { return departamentoSede; }
    public void setDepartamentoSede(String departamentoSede) { this.departamentoSede = departamentoSede; }
    public String getSedDirecc() { return sedDirecc; }
    public void setSedDirecc(String sedDirecc) { this.sedDirecc = sedDirecc; }
    public String getSedTel() { return sedTel; }
    public void setSedTel(String sedTel) { this.sedTel = sedTel; }
    public String getPerTelmov() { return perTelmov; }
    public void setPerTelmov(String perTelmov) { this.perTelmov = perTelmov; }
    public String getPerEmail() { return perEmail; }
    public void setPerEmail(String perEmail) { this.perEmail = perEmail; }
}