package de.desarrollospy.side.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "v_det_cobros_edocs", schema = "public")
public class DetalleCobro implements Serializable {
    
    private static final long serialVersionUID = 1L;

    // Usamos @Id en cod_int asumiendo unicidad en el contexto de la consulta, 
    // o podrías necesitar una PK compuesta si cod_int se repite.
    @Id
    @Column(name = "cod_int")
    private String codInt;

    @Column(name = "id_cobro")
    private String idCobro;

    @Column(name = "descri_cta")
    private String descriCta;

    @Column(name = "det_pro_ser")
    private String detProSer;

    private int cantidad;
    private BigDecimal monto;
    private Integer exenta;
    private Integer gravada5;
    private Integer gravada10;
    private Integer iva5;
    private Integer iva10;

    @Column(name = "des_nro_cta")
    private String desNroCta;

    @Column(name = "fec_venc")
    private LocalDate fecVenc;

    @Column(name = "nro_cobro")
    private Integer nroCobro;

    @Column(name = "id_sede")
    private Integer idSede;

    @Column(name = "id_caja")
    private Integer idCaja;

    @Column(name = "nro_apecie")
    private Integer nroApecie;

    @Column(name = "id_funcio")
    private Integer idFuncio;

    @Column(name = "id_aranc")
    private Integer idAranc;

    @Column(name = "obs")
    private String obs;

    @Column(name = "cta_saldo")
    private BigDecimal ctaSaldo;

    // Campos Transitorios (No existen en la BD, usados para lógica de negocio)
    @Transient
    private Boolean esGlobal;

    @Transient
    private BigDecimal montoDescuento = BigDecimal.ZERO;

    // Getters y Setters
    public String getCodInt() { return codInt; }
    public void setCodInt(String codInt) { this.codInt = codInt; }
    public String getIdCobro() { return idCobro; }
    public void setIdCobro(String idCobro) { this.idCobro = idCobro; }
    public String getDescriCta() { return descriCta; }
    public void setDescriCta(String descriCta) { this.descriCta = descriCta; }
    public String getDetProSer() { return detProSer; }
    public void setDetProSer(String detProSer) { this.detProSer = detProSer; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public Integer getExenta() { return exenta; }
    public void setExenta(Integer exenta) { this.exenta = exenta; }
    public Integer getGravada5() { return gravada5; }
    public void setGravada5(Integer gravada5) { this.gravada5 = gravada5; }
    public Integer getGravada10() { return gravada10; }
    public void setGravada10(Integer gravada10) { this.gravada10 = gravada10; }
    public Integer getIva5() { return iva5; }
    public void setIva5(Integer iva5) { this.iva5 = iva5; }
    public Integer getIva10() { return iva10; }
    public void setIva10(Integer iva10) { this.iva10 = iva10; }
    public String getDesNroCta() { return desNroCta; }
    public void setDesNroCta(String desNroCta) { this.desNroCta = desNroCta; }
    public LocalDate getFecVenc() { return fecVenc; }
    public void setFecVenc(LocalDate fecVenc) { this.fecVenc = fecVenc; }
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
    public Integer getIdAranc() { return idAranc; }
    public void setIdAranc(Integer idAranc) { this.idAranc = idAranc; }
    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }
    public BigDecimal getCtaSaldo() { return ctaSaldo; }
    public void setCtaSaldo(BigDecimal ctaSaldo) { this.ctaSaldo = ctaSaldo; }
    
    public Boolean isEsGlobal() { return esGlobal != null ? esGlobal : false; }
    public void setEsGlobal(Boolean esGlobal) { this.esGlobal = esGlobal; }
    
    public BigDecimal getMontoDescuento() { return montoDescuento; }
    public void setMontoDescuento(BigDecimal montoDescuento) { this.montoDescuento = montoDescuento; }
}