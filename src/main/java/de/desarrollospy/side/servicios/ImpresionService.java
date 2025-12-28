package de.desarrollospy.side.servicios;

public interface ImpresionService {
    byte[] generarFacturaPdf(String idDocumento) throws Exception;
}