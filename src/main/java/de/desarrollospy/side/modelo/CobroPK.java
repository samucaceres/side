package de.desarrollospy.side.modelo;

import java.io.Serializable;
import java.util.Objects;

public class CobroPK implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Integer nroCobro;
    private Integer idSede;
    private Integer idCaja;
    private Integer nroApecie;
    private Integer idFuncio;

    public CobroPK() {}

    public CobroPK(Integer nroCobro, Integer idSede, Integer idCaja, Integer nroApecie, Integer idFuncio) {
        this.nroCobro = nroCobro;
        this.idSede = idSede;
        this.idCaja = idCaja;
        this.nroApecie = nroApecie;
        this.idFuncio = idFuncio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CobroPK cobroPK = (CobroPK) o;
        return Objects.equals(nroCobro, cobroPK.nroCobro) &&
                Objects.equals(idSede, cobroPK.idSede) &&
                Objects.equals(idCaja, cobroPK.idCaja) &&
                Objects.equals(nroApecie, cobroPK.nroApecie) &&
                Objects.equals(idFuncio, cobroPK.idFuncio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nroCobro, idSede, idCaja, nroApecie, idFuncio);
    }
}