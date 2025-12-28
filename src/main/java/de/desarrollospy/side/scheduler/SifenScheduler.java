package de.desarrollospy.side.scheduler;

import com.roshka.sifen.Sifen;
import com.roshka.sifen.core.SifenConfig;
import com.roshka.sifen.core.beans.response.RespuestaConsultaLoteDE;
import com.roshka.sifen.core.fields.response.batch.TgResProcLote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.desarrollospy.side.modelo.EDoc;
import de.desarrollospy.side.modelo.ErroresDocumento;
import de.desarrollospy.side.repository.EDocRepository;
import de.desarrollospy.side.repository.ErroresDocumentoRepository;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Component
public class SifenScheduler {

    private static final Logger logger = Logger.getLogger(SifenScheduler.class.getName());

    @Autowired
    private EDocRepository eDocRepository;

    @Autowired
    private ErroresDocumentoRepository erroresDocumentoRepository;

    // Inyectamos las configuraciones para Sifen
    @Value("${app.sifen.certificado-path}")
    private String certificadoPath;
    @Value("${app.sifen.certificado-pass}")
    private String certificadoPass;
    @Value("${app.sifen.ambiente}")
    private String ambiente;
    @Value("${app.sifen.id-csc}")
    private String idCsc;
    @Value("${app.sifen.csc}")
    private String csc;

    // Se ejecuta cada 60000 ms (1 minuto) después de que termina la ejecución anterior
    @Scheduled(fixedDelay = 60000)
    public void verificarLotesEnviados() {
        logger.info("Inicio proceso automático: Verificación de Lotes ENVIADO SET");

        try {
            // 1. Obtener lotes pendientes desde la BD
            List<String> lotes = eDocRepository.findLotesPendientes();

            if (lotes == null || lotes.isEmpty()) {
                logger.info("No hay lotes pendientes de verificación.");
                return;
            }

            // 2. Configurar Sifen (necesario si el contexto se reinició o perdió config)
            configurarCertificado();

            for (String nroLote : lotes) {
                logger.info("Consultando Lote Nro: " + nroLote);

                try {
                    // 3. Consultar a Sifen
                    RespuestaConsultaLoteDE respuesta = Sifen.consultaLoteDE(nroLote);

                    // Validar respuesta HTTP 200
                    if (respuesta.getCodigoEstado() != 200) {
                        logger.warning("Error HTTP al consultar lote " + nroLote + ": " + respuesta.getRespuestaBruta());
                        continue; // Intentar siguiente lote
                    }

                    // 4. Verificar si el Lote ya fue procesado por la SET (0362)
                    // Si sigue en "Procesamiento" (0360/0361), esperamos a la siguiente vuelta.
                    if ("0362".equalsIgnoreCase(respuesta.getdCodResLot())) {
                        
                        // Iterar sobre los resultados de cada documento dentro del lote
                        for (TgResProcLote respProc : respuesta.getgResProcLoteList()) {
                            procesarResultadoIndividual(respProc);
                        }
                    } else {
                        logger.info("Lote " + nroLote + " aun en procesamiento. Estado: " + respuesta.getdCodResLot() + " - " + respuesta.getdMsgResLot());
                    }

                } catch (Exception e) {
                    logger.severe("Error al consultar lote " + nroLote + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.severe("Error general en el Scheduler: " + e.getMessage());
        }
    }

    private void procesarResultadoIndividual(TgResProcLote respProc) {
        String cdc = respProc.getId();
        String codigoEstado = respProc.getgResProc().get(0).getdCodRes();
        String mensajeEstado = respProc.getgResProc().get(0).getdMsgRes();

        // Buscar el EDoc en nuestra base de datos
        Optional<EDoc> eDocOpt = eDocRepository.findByCdc(cdc);

        if (eDocOpt.isPresent()) {
            EDoc doc = eDocOpt.get();

            // 0260 = Aprobado, 1002 = Aprobado previamente (Duplicado aceptado)
            if ("0260".equals(codigoEstado) || "1002".equals(codigoEstado)) {
                
                doc.setEstado("APROBADO SET");
                
                if ("0260".equals(codigoEstado)) {
                    doc.setNroTransaccion(respProc.getdProtAut());
                }
                // Si es 1002, a veces viene el protocolo, a veces no, depende de la SET.
                // Generalmente mantenemos el que ya teníamos o actualizamos si viene.
                
                doc.setFechaEstado(LocalDateTime.now());
                doc.setVerificado("SI");
                eDocRepository.save(doc);
                
                logger.info("Documento APROBADO: " + cdc);

            } else {
                // Cualquier otro código es RECHAZO
                doc.setEstado("RECHAZADO SET");
                doc.setFechaEstado(LocalDateTime.now());
                eDocRepository.save(doc);

                // Guardar en tabla de Errores
                registrarError(doc, codigoEstado, mensajeEstado, cdc);
                
                logger.warning("Documento RECHAZADO: " + cdc + " Motivo: " + mensajeEstado);
            }
        } else {
            logger.warning("Recibimos respuesta para CDC " + cdc + " pero no existe en nuestra BD.");
        }
    }

    private void registrarError(EDoc doc, String codigo, String descripcion, String cdc) {
        try {
            ErroresDocumento error = new ErroresDocumento();
            error.setCdc(cdc);
            error.setCodigoError(codigo);
            // Truncar descripción si es muy larga
            error.setDescripcionError(descripcion != null && descripcion.length() > 255 ? descripcion.substring(0, 255) : descripcion);
            error.setIdDocumentoOriginal(doc.getIdDocumentoOriginal());
            error.setEstablecimiento(doc.getEstablecimiento());
            error.setFechaError(new Date());
            error.setNroDocumento(doc.getNroDocumento());
            error.setOperacion("Sifen.consultaLoteDE (Scheduler)");
            error.setPuntoExpedicion(doc.getPuntoExp());
            error.setTipoDocumento(doc.getTipoDocumento());
            
            erroresDocumentoRepository.save(error);
        } catch (Exception e) {
            logger.severe("No se pudo guardar el error en BD: " + e.getMessage());
        }
    }

    private void configurarCertificado() {
        try {
            File certFile = new File(certificadoPath);
            if (!certFile.exists()) return;

            SifenConfig config;
            if ("DEV".equalsIgnoreCase(ambiente)) {
                config = new SifenConfig(SifenConfig.TipoAmbiente.DEV, SifenConfig.TipoCertificadoCliente.PFX, certificadoPath, certificadoPass);
            } else {
                config = new SifenConfig(SifenConfig.TipoAmbiente.PROD, idCsc, csc, SifenConfig.TipoCertificadoCliente.PFX, certificadoPath, certificadoPass);
            }
            Sifen.setSifenConfig(config);
        } catch (Exception e) {
            logger.severe("Error configurando certificado en Scheduler: " + e.getMessage());
        }
    }
}