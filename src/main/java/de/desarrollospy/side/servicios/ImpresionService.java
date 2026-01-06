package de.desarrollospy.side.servicios;

public interface ImpresionService {
    byte[] generarFacturaPdf(String idDocumento, String tipoDocumento) throws Exception;
}