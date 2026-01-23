package de.desarrollospy.side.servicios.impl;

import com.roshka.sifen.Sifen;
import com.roshka.sifen.core.SifenConfig;
import com.roshka.sifen.core.beans.DocumentoElectronico;
import com.roshka.sifen.core.beans.EventosDE;
import com.roshka.sifen.core.beans.response.RespuestaConsultaRUC;
import com.roshka.sifen.core.beans.response.RespuestaRecepcionDE;
import com.roshka.sifen.core.beans.response.RespuestaRecepcionEvento;
import com.roshka.sifen.core.exceptions.SifenException;
import com.roshka.sifen.core.fields.request.de.*;
import com.roshka.sifen.core.fields.request.event.TgGroupTiEvt;
import com.roshka.sifen.core.fields.request.event.TrGeVeCan;
import com.roshka.sifen.core.fields.request.event.TrGesEve;
import com.roshka.sifen.core.fields.response.TgResProc;
import com.roshka.sifen.core.fields.response.event.TgResProcEVe;
import com.roshka.sifen.core.types.*;
import com.roshka.sifen.internal.ctx.GenerationCtx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.provider.HibernateUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import de.desarrollospy.side.dto.DocumentoStatusDTO;
import de.desarrollospy.side.exception.AppException;
import de.desarrollospy.side.modelo.Cobro;
import de.desarrollospy.side.modelo.DetalleCobro;
import de.desarrollospy.side.modelo.EDoc;
import de.desarrollospy.side.modelo.EntidadGobierno;
import de.desarrollospy.side.modelo.ErroresDocumento;
import de.desarrollospy.side.modelo.Evento;
import de.desarrollospy.side.modelo.NotaCredito;
import de.desarrollospy.side.modelo.NotaCreditoDetalle;
import de.desarrollospy.side.modelo.NotaCreditoSifenView;
import de.desarrollospy.side.repository.CobroRepository;
import de.desarrollospy.side.repository.DetalleCobroRepository;
import de.desarrollospy.side.repository.EDocRepository;
import de.desarrollospy.side.repository.EntidadGobiernoRepository;
import de.desarrollospy.side.repository.ErroresDocumentoRepository;
import de.desarrollospy.side.repository.EventoRepository;
import de.desarrollospy.side.repository.FacturaSifenViewRepository;
import de.desarrollospy.side.repository.NotaCreditoDetalleRepository;
import de.desarrollospy.side.repository.NotaCreditoRepository;
import de.desarrollospy.side.repository.NotaCreditoSifenViewRepository;
import de.desarrollospy.side.servicios.SifenService;
import jakarta.transaction.Transactional;

//Imports adicionales para FE
import com.roshka.sifen.core.fields.request.de.TgOpeCom;
import com.roshka.sifen.core.fields.request.de.TgCamFE;
import com.roshka.sifen.core.fields.request.de.TgCamCond;
import com.roshka.sifen.core.fields.request.de.TgPaConEIni;
import com.roshka.sifen.core.fields.request.de.TgPagCred;
import com.roshka.sifen.core.fields.request.de.TgCuotas; // Si manejas detalle de cuotas
import com.roshka.sifen.core.types.TTipTra;
import com.roshka.sifen.core.types.TTImp;
import com.roshka.sifen.core.types.TiCondOpe;
import com.roshka.sifen.core.types.TiTiPago;
import com.roshka.sifen.core.types.TiCondCred;
import com.roshka.sifen.core.types.TiIndPres;

import de.desarrollospy.side.modelo.FacturaSifenView;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
 // Importar Lote
import com.roshka.sifen.core.beans.response.RespuestaRecepcionLoteDE;

//Agregar imports de los modelos nuevos y tipos SIFEN para Remisión
import de.desarrollospy.side.modelo.RemisionSifenView;
import de.desarrollospy.side.repository.RemisionSifenViewRepository; // Asumo que creaste este repo
import com.roshka.sifen.core.fields.request.de.TgCamNRE;
import com.roshka.sifen.core.fields.request.de.TgTransp;
import com.roshka.sifen.core.fields.request.de.TgVehTras;
import com.roshka.sifen.core.fields.request.de.TgCamTrans;
import com.roshka.sifen.core.fields.request.de.TgCamSal;
import com.roshka.sifen.core.fields.request.de.TgCamEnt;
import com.roshka.sifen.core.types.TiMotEmi;
import com.roshka.sifen.core.types.TiRespEmiNR;
import com.roshka.sifen.core.types.TiModTrans;
import com.roshka.sifen.core.types.TiRespFlete;

@Service
public class SifenServiceImpl implements SifenService {

    private static final Logger logger = Logger.getLogger(SifenServiceImpl.class.getName());
    
    @Autowired
    private EDocRepository eDocRepository;
   /* @Autowired
    private CobroRepository cobroRepository;*/
    @Autowired
    private FacturaSifenViewRepository facturaSifenViewRepository;
    @Autowired
    private DetalleCobroRepository detalleCobroRepository;
    @Autowired
    private NotaCreditoRepository notaCreditoRepository;
    @Autowired
    private NotaCreditoDetalleRepository notaCreditoDetalleRepository;
    @Autowired
    private ErroresDocumentoRepository erroresDocumentoRepository;
    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private RemisionSifenViewRepository remisionSifenViewRepository;
    @Autowired
    private EntidadGobiernoRepository entidadGobiernoRepository;
    @Autowired
    private NotaCreditoSifenViewRepository notaCreditoSifenViewRepository;

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

    @Override
    public RespuestaConsultaRUC consultarRuc(String ruc) {
        try {
            configurarCertificado();
            logger.info("Consultando RUC: " + ruc);
            RespuestaConsultaRUC respuesta = Sifen.consultaRUC(ruc);
            logger.info("Respuesta Sifen: " + respuesta.getRespuestaBruta());
            return respuesta;
        } catch (SifenException e) {
            logger.severe("Error de SifenException: " + e.getMessage());
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en la comunicación con Sifen: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al consultar RUC: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public String procesarDocumento(String idDocumento, String tipoDocumento) {
        try {
            logger.info("Procesando documento (LOTE): " + idDocumento + " Tipo: " + tipoDocumento);
            
            DocumentoElectronico de;
            EDoc edoc;
            // Buscar si ya existe el registro en edocs
            Optional<EDoc> edocExistente = eDocRepository.findByIdDocumentoOriginalAndTipoDocumento(idDocumento, tipoDocumento);
            if (edocExistente.isPresent()) {
                edoc = edocExistente.get();
                if ("APROBADO SET".equals(edoc.getEstado())) {
                    return "DOCUMENTO YA APROBADO";
                }
            } else {
                edoc = new EDoc();
                edoc.setIdDocumentoOriginal(idDocumento);
                edoc.setTipoDocumento(tipoDocumento);
            }

            if ("FE".equalsIgnoreCase(tipoDocumento)) {
                // Lógica existente de Cobros (No se toca lo anterior)
               /* String[] parts = idDocumento.split("-");
                Cobro cobro = cobroRepository.findByCompositeId(
                        Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
                        Integer.parseInt(parts[4])
                ).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Cobro no encontrado"));

                de = generarFacturaElectronica(idDocumento, edoc, cobro);*/
            	// Nueva lógica V2
                Long facturaId = Long.parseLong(idDocumento);
                FacturaSifenView facturaView = facturaSifenViewRepository.findById(facturaId)
                       .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

                de = generarFacturaElectronicaV2(idDocumento, edoc, facturaView);

            } else if ("NC".equalsIgnoreCase(tipoDocumento)) {

            	// NUEVA LÓGICA V2 PARA NOTA DE CRÉDITO
                Long ncId = Long.parseLong(idDocumento);
                NotaCreditoSifenView ncView = notaCreditoSifenViewRepository.findById(ncId)
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Nota de Crédito no encontrada"));

                de = generarNotaCreditoElectronicaV2(idDocumento, edoc, ncView);
            } else if ("NR".equalsIgnoreCase(tipoDocumento) || "REM".equalsIgnoreCase(tipoDocumento)) { 
                // NUEVA LÓGICA PARA REMISIÓN
                // Asumiendo que idDocumento es el ID numérico de la remisión (ej: "15")
                Long remisionId = Long.parseLong(idDocumento);
                
                RemisionSifenView remisionView = remisionSifenViewRepository.findById(remisionId)
                        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Nota de Remisión no encontrada"));

                de = generarRemisionElectronica(idDocumento, edoc, remisionView);
            } else {
                throw new AppException(HttpStatus.BAD_REQUEST, "Tipo de documento no soportado");
            }

            configurarCertificado();
            
            // Ajuste de fecha de firma (Timezone fix)
            de.setdFecFirma(LocalDateTime.now().minusMinutes(5)); 

            GenerationCtx ctx = new GenerationCtx();
            ctx.setHabilitarNotaTecnica13(true);
            ctx.setSifenConfig(Sifen.getSifenConfig());
            
            String xml = de.generarXml(ctx);
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] encodedBytes = encoder.encode(xml.getBytes());

            // Guardado inicial en BD
            edoc.setCdc(de.getId());
            edoc.setEstado("PENDIENTE ENVIO");
            edoc.setXml(encodedBytes);
            edoc = eDocRepository.save(edoc);

            // --- LÓGICA DE ENVÍO POR LOTE (REPLICANDO TU CÓDIGO ORIGINAL) ---
            
            List<DocumentoElectronico> loteAEnviar = new ArrayList<>();
            loteAEnviar.add(de);
            
            // Envío Asincrónico usando la Lista directa
            RespuestaRecepcionLoteDE respuesta = Sifen.recepcionLoteDE(loteAEnviar, Sifen.getSifenConfig(), ctx);
            
            logger.info("Codigo Estado HTTP: " + respuesta.getCodigoEstado());
            logger.info("Respuesta Sifen: " + respuesta.getRespuestaBruta());

            // 1. Validar Estado HTTP
            if (respuesta.getCodigoEstado() != 200) {
                throw new AppException(HttpStatus.PRECONDITION_FAILED, "Fallo de validacion HTTP: " + respuesta.getRespuestaBruta());
            }

            // 2. Validar Estado de Recepción de Lote (0300 = Recibido OK)
            if (!"0300".equalsIgnoreCase(respuesta.getdCodRes())) {
                logger.severe("ERROR AL PROCESAR CDC: " + de.getId());
                
                // Registro de Error en Tabla
                try {
                    ErroresDocumento error = new ErroresDocumento();
                    error.setCodigoError(respuesta.getdCodRes());
                    error.setDescripcionError(respuesta.getdMsgRes());
                    error.setIdDocumentoOriginal(idDocumento);
                    error.setTipoDocumento(tipoDocumento);
                    error.setCdc(de.getId());
                    error.setFechaError(new Date());
                    error.setNroDocumento(edoc.getNroDocumento());
                    error.setEstablecimiento(edoc.getEstablecimiento());
                    error.setPuntoExpedicion(edoc.getPuntoExp());
                    error.setOperacion("Sifen.recepcionLoteDE");
                    erroresDocumentoRepository.save(error);

                    // Actualización de Estado a RECHAZADO
                    edoc.setEstado("RECHAZADO SET");
                    edoc.setFechaEstado(LocalDateTime.now());
                    edoc.setVerificado(null);
                    eDocRepository.save(edoc);
                    
                } catch (Exception e) {
                    logger.severe("Error al guardar el error en la base de datos: " + e.getMessage());
                }
                
                // Retornamos el error como texto para el cliente (sin lanzar excepción para asegurar que el save() anterior persista)
                return "RECHAZADO SET: " + respuesta.getdMsgRes();
            }

            // 3. Éxito (0300): El lote fue recibido
            // Guardamos el Nro de Lote para consulta asincrónica posterior
            edoc.setEstado("ENVIADO SET");
            edoc.setNroLote(respuesta.getdProtConsLote()); // Guardamos el protocolo de consulta
            edoc.setFechaEstado(LocalDateTime.now());
            eDocRepository.save(edoc);

            return "ENVIADO SET CORRECTAMENTE. Lote: " + respuesta.getdProtConsLote();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno: " + e.getMessage());
        }
    }

    // He añadido el parámetro Cobro para no buscarlo dos veces
    private DocumentoElectronico generarFacturaElectronica(String idDocumento, EDoc eDoc, Cobro cobro) {
        
        List<DetalleCobro> detallesCobro = detalleCobroRepository.findByCompositeId(
                cobro.getNroCobro(), cobro.getIdSede(), cobro.getIdCaja(), 
                cobro.getNroApecie(), cobro.getIdFuncio());

        // Inicializar EDoc
        if (eDoc.getId() == null) {
            eDoc.setIdDocumentoOriginal(idDocumento);
            eDoc.setTipoDocumento("FE");
            eDoc.setNroDocumento(String.format("%7s", cobro.getNroFact().trim()).replace(' ', '0'));
            eDoc.setEstablecimiento(cobro.getNroSucfac());
            eDoc.setPuntoExp(cobro.getNroExped());
            eDoc.setFechaEmision(cobro.getCobFechahora());
            eDoc.setEnviado("NO");
        }

        DocumentoElectronico de = new DocumentoElectronico();
        de.setdSisFact((short) 1);

        // 1. Datos Operación
        de.setgOpeDE(new TgOpeDE());
        de.getgOpeDE().setiTipEmi(TTipEmi.NORMAL);

        // 2. Timbrado
        de.setgTimb(new TgTimb());
        de.getgTimb().setiTiDE(TTiDE.FACTURA_ELECTRONICA);
        de.getgTimb().setdNumTim(Integer.valueOf(cobro.getNroTimb()));
        de.getgTimb().setdEst(String.format("%3s", cobro.getNroSucfac()).replace(' ', '0'));
        de.getgTimb().setdPunExp(String.format("%3s", cobro.getNroExped()).replace(' ', '0'));
        de.getgTimb().setdNumDoc(String.format("%7s", cobro.getNroFact().trim()).replace(' ', '0'));
        if (cobro.getdFeIniT() != null) {
            de.getgTimb().setdFeIniT(cobro.getdFeIniT());
        }

        // 3. Datos Generales
        de.setgDatGralOpe(new TdDatGralOpe());
        de.getgDatGralOpe().setdFeEmiDE(cobro.getCobFechahora());
        
        de.getgDatGralOpe().setgOpeCom(new TgOpeCom());
        de.getgDatGralOpe().getgOpeCom().setiTipTra(TTipTra.PRESTACION_SERVICIOS); 
        de.getgDatGralOpe().getgOpeCom().setiTImp(TTImp.IVA);
        de.getgDatGralOpe().getgOpeCom().setcMoneOpe(CMondT.PYG);
        
        if (!CMondT.PYG.equals(de.getgDatGralOpe().getgOpeCom().getcMoneOpe())) {
            //de.getgDatGralOpe().getgOpeCom().setdCondTiCam(TiCondOpe.CONTADO); 
        }

        // 4. Emisor
        TgEmis emisor = new TgEmis();
        emisor.setdRucEm("80113109");
        emisor.setdDVEmi("0");
        emisor.setiTipCont(TiTipCont.PERSONA_JURIDICA);
        emisor.setcTipReg(TTipReg.REGIMEN_CONTABLE);
        emisor.setdNomEmi("TECNOMEZA SOCIEDAD ANONIMA");
        emisor.setdDirEmi(cobro.getSedDirecc());
        emisor.setdNumCas("0");
        
        short idDepto = (cobro.getIdDepartamentoSede() != null) ? cobro.getIdDepartamentoSede().shortValue() : 0;
        TDepartamento deptoEnum = TDepartamento.getByVal(idDepto);
        if (deptoEnum == null) deptoEnum = TDepartamento.CAPITAL;
        emisor.setcDepEmi(deptoEnum);
        
        emisor.setcCiuEmi(cobro.getIdCiudadSede() != null ? cobro.getIdCiudadSede() : 1);
        emisor.setdDesCiuEmi(cobro.getCiudadSede() != null ? cobro.getCiudadSede() : "ASUNCION");
        emisor.setdTelEmi(cobro.getSedTel());
        emisor.setdEmailE("manolo@tecnomeza.com.py");

        List<TgActEco> actEcos = new ArrayList<>();
        TgActEco act1 = new TgActEco();
        act1.setcActEco("85220");
        act1.setdDesActEco("ENSEÑANZA SECUNDARIA DE FORMACIÓN TÉCNICA Y PROFESIONAL");
        actEcos.add(act1);
        emisor.setgActEcoList(actEcos);
        de.getgDatGralOpe().setgEmis(emisor);

        // 5. Receptor
        TgDatRec receptor = new TgDatRec();
        receptor.setiNatRec(cobro.getTipoDocPersona().equalsIgnoreCase("RUC") ? TiNatRec.CONTRIBUYENTE : TiNatRec.NO_CONTRIBUYENTE);
        receptor.setiTiOpe(TiTiOpe.B2C);
        receptor.setcPaisRec(PaisType.PRY);
        receptor.setdNomRec(cobro.getaNombreDe());
        
        if ("RUC".equalsIgnoreCase(cobro.getTipoDocPersona())) {
            receptor.setiTiContRec(TiTipCont.PERSONA_JURIDICA);
            String rucRaw = cobro.getRucDe();
            if (rucRaw != null && rucRaw.contains("-")) {
                receptor.setdRucRec(rucRaw.split("-")[0]);
                try {
                    receptor.setdDVRec(Short.valueOf(rucRaw.split("-")[1]));
                } catch (NumberFormatException e) {
                    receptor.setdDVRec((short)0);
                }
            } else {
                 receptor.setdRucRec(rucRaw);
                 receptor.setdDVRec((short)0); 
            }
        } else {
             receptor.setiTipIDRec(TiTipDocRec.CEDULA_PARAGUAYA);
             receptor.setdNumIDRec(cobro.getRucDe());
        }
        
//        if (cobro.getPerDirecc() != null && !cobro.getPerDirecc().isEmpty()) {
//            receptor.setdDirRec(cobro.getPerDirecc());
//            
//         // FIX CRÍTICO: Asignar valores por defecto para evitar caída del sistema
//            // Idealmente esto debería venir de la BD, pero 'Cobro' no tiene estos campos.
//            receptor.setcDepRec(TDepartamento.CAPITAL);
//            receptor.setcDisRec((short)1); // ID Distrito genérico (Asunción)
//            receptor.setcCiuRec(1); // ID Ciudad genérico (Asunción)
//            receptor.setdDesCiuRec("ASUNCION");
//            receptor.setdDesDisRec("ASUNCION");
//        }
        de.getgDatGralOpe().setgDatRec(receptor);

     // 6. Condición de Operación y Campos de Factura
        de.setgDtipDE(new TgDtipDE());

        // A. Campos Obligatorios de Factura Electrónica (gCamFE)
        com.roshka.sifen.core.fields.request.de.TgCamFE gCamFE = new com.roshka.sifen.core.fields.request.de.TgCamFE();
        gCamFE.setiIndPres(com.roshka.sifen.core.types.TiIndPres.OPERACION_PRESENCIAL);
        de.getgDtipDE().setgCamFE(gCamFE);

        // B. Condición de Operación (gCamCond)
        TgCamCond camCond = new TgCamCond();
        boolean esContado = cobro.getDorespNomb() != null && cobro.getDorespNomb().trim().equalsIgnoreCase("FACTURA CONTADO");
        camCond.setiCondOpe(esContado ? TiCondOpe.CONTADO : TiCondOpe.CREDITO);

        if (esContado) {
            // SI ES CONTADO: Debemos agregar la Forma de Pago (Efectivo por defecto)
            List<com.roshka.sifen.core.fields.request.de.TgPaConEIni> listaPagos = new ArrayList<>();
            com.roshka.sifen.core.fields.request.de.TgPaConEIni pago = new com.roshka.sifen.core.fields.request.de.TgPaConEIni();
            pago.setiTiPago(com.roshka.sifen.core.types.TiTiPago.EFECTIVO); // Asumimos Efectivo
            pago.setdDesTiPag("Efectivo");
            pago.setdMonTiPag(new BigDecimal(cobro.getTotal())); // Monto total de la operación
            pago.setcMoneTiPag(CMondT.PYG);
            listaPagos.add(pago);
            camCond.setgPaConEIniList(listaPagos);
        } else {
            // SI ES CRÉDITO: Debemos agregar el detalle del crédito
            com.roshka.sifen.core.fields.request.de.TgPagCred cred = new com.roshka.sifen.core.fields.request.de.TgPagCred();
            cred.setiCondCred(com.roshka.sifen.core.types.TiCondCred.PLAZO);
            cred.setdPlazoCre("30 días"); // Valor estándar para evitar error (Ajustar si tienes el dato real)
            camCond.setgPagCred(cred);
        }

        de.getgDtipDE().setgCamCond(camCond);
        
        // 7. --- LOGICA ORIGINAL DE ITEMS Y DESCUENTOS ---
        
        de.getgDtipDE().setgCamItemList(new ArrayList<TgCamItem>());
        TgCamItem item;
        TgValorItem val;
        TgCamIVA iva;	
        
        List<DetalleCobro> detCobros = new ArrayList<DetalleCobro>();
        List<DetalleCobro> detCobroDtos = new ArrayList<DetalleCobro>();
        
        //Se separan las cuotas de los descuentos
        for (DetalleCobro detCobro : detallesCobro) {
            if (detCobro.getMonto().signum() == -1) {
                detCobro.setEsGlobal(false);
                detCobroDtos.add(detCobro);
            } else {
                detCobro.setMontoDescuento(BigDecimal.ZERO);
                detCobros.add(detCobro);
            }
        }
        
        //Se verifica si el monto afecta a una sola cuota o a varias cuotas
        for(DetalleCobro desc : detCobroDtos) {
            for(DetalleCobro detCobro: detCobros) {
                if (desc.getDesNroCta().equals(detCobro.getCodInt())) {
                    if(desc.getMonto().abs().compareTo(detCobro.getMonto())>0) {
                        desc.setEsGlobal(true);
                    }
                }
            }
        }
        
        //Se asignan los montos de descuentos para cada cuota
        for(DetalleCobro desc : detCobroDtos) {
            
            if(desc.isEsGlobal()){ //Si el descuento afecta a varias cuotas
                
                BigDecimal totalMontoConDesc = BigDecimal.ZERO;
                BigDecimal totalPorcen = BigDecimal.ZERO;
                BigDecimal totalDescuento = BigDecimal.ZERO;
                
                //Se calcula el monto total afectado por el descuento
                for(DetalleCobro detCobro: detCobros){		
                    // Nota: Asegúrate que idAranc y ctaSaldo no sean nulos en BD para evitar NullPointer
                    if(detCobro.getIdAranc() != null && desc.getIdAranc() != null 
                       && detCobro.getIdAranc().compareTo(desc.getIdAranc())==0 
                       && detCobro.getCtaSaldo() != null && detCobro.getCtaSaldo().compareTo(BigDecimal.ZERO)==0) {
                        totalMontoConDesc = totalMontoConDesc.add(detCobro.getMonto());
                    }
                }
                
                //Se calcula el porcentaje de descuento
                if(totalMontoConDesc.compareTo(BigDecimal.ZERO) != 0) {
                    totalPorcen = desc.getMonto().abs().multiply(BigDecimal.valueOf(100)).divide(totalMontoConDesc, 2, RoundingMode.HALF_UP);
                }
                
                //Se calcula el monto del descuento para cada cuota
                for(DetalleCobro detCobro: detCobros){		
                     if(detCobro.getIdAranc() != null && desc.getIdAranc() != null 
                        && detCobro.getIdAranc().compareTo(desc.getIdAranc())==0 
                        && detCobro.getCtaSaldo() != null && detCobro.getCtaSaldo().compareTo(BigDecimal.ZERO)==0) {
                        detCobro.setMontoDescuento(totalPorcen.multiply(detCobro.getMonto()).divide(BigDecimal.valueOf(100)));
                        totalDescuento = totalDescuento.add(detCobro.getMontoDescuento());
                    }
                }
                
            }else {//Si el descuento afecta a una sola cuota
                
                //Se busca la cuota afectada por el descuento
                for(DetalleCobro detCobro: detCobros){
                    if (desc.getDesNroCta().equals(detCobro.getCodInt())) {
                        detCobro.setMontoDescuento(desc.getMonto().abs());
                    }
                }
                    
            }
        }
        
        int afecIva;
        for(DetalleCobro detCobro: detCobros){		
            item = new TgCamItem();
            item.setdCodInt(detCobro.getCodInt() != null ? detCobro.getCodInt() : "0");
            item.setdDesProSer(detCobro.getDescriCta()!=null?detCobro.getDescriCta():detCobro.getDetProSer());	
            if(detCobro.getObs()!=null && !detCobro.getObs().trim().equals("")) {
                item.setdDesProSer(detCobro.getObs());
            }
            item.setcUniMed(TcUniMed.UNI);
            item.setdCantProSer(new BigDecimal(detCobro.getCantidad()));
            
            val = new TgValorItem();
            val.setdPUniProSer(detCobro.getMonto());					
            
            
            if(detCobro.getExenta() != null && detCobro.getExenta() > 0)
                afecIva = 3;				
            else
                afecIva = 1;
            
            iva = new TgCamIVA();
            iva.setiAfecIVA(TiAfecIVA.getByVal((short)afecIva));				
            iva.setdPropIVA((afecIva == 1) ? new BigDecimal(100) : BigDecimal.ZERO);	
            
            BigDecimal tasaVal = BigDecimal.ZERO;
            if(afecIva == 1) {
                if(detCobro.getIva5() != null && detCobro.getIva5() > 0) tasaVal = new BigDecimal(5);
                else tasaVal = new BigDecimal(10);
            }
            iva.setdTasaIVA(tasaVal);
            iva.setdBasExe(null);
            item.setgCamIVA(iva);

            TgValorRestaItem resta = new TgValorRestaItem();
            
            // Lógica de asignación de descuento
            if(detCobro.getMontoDescuento() != null && detCobro.getMontoDescuento().compareTo(BigDecimal.ZERO)>0){
                resta.setdDescItem(detCobro.getMontoDescuento());
            }

            val.setgValorRestaItem(resta);
            item.setgValorItem(val);
            
            de.getgDtipDE().getgCamItemList().add(item);
        }
        
        // Total Sub (Obligatorio)
        de.setgTotSub(new TgTotSub()); 

        return de;
    }
    
    /**
     * Genera el objeto DocumentoElectronico (FE) a partir de la vista FacturaSifenView.
     * Cumple con Manual Técnico 150.
     */
    private DocumentoElectronico generarFacturaElectronicaV2(String idDocumento, EDoc edoc, FacturaSifenView view) {
        DocumentoElectronico de = new DocumentoElectronico();
        de.setdSisFact((short) 1);

        // --- 0. Inicializar EDoc (Si es nuevo) ---
        if (edoc.getId() == null) {
            edoc.setIdDocumentoOriginal(idDocumento);
            edoc.setTipoDocumento("FE");
            String nroDocStr = String.format("%07d", view.getNumeroDocumento());
            edoc.setNroDocumento(nroDocStr);
            edoc.setEstablecimiento(view.getCodigoEstablecimiento());
            edoc.setPuntoExp(view.getCodigoPuntoExpedicion());
            edoc.setFechaEmision(view.getFechaEmision());
            edoc.setEnviado("NO");
        }

        // --- 1. Datos Operación (gOpeDE) ---
        de.setgOpeDE(new TgOpeDE());
        de.getgOpeDE().setiTipEmi(TTipEmi.NORMAL);
        // Info opcional del emisor
        de.getgOpeDE().setdInfoEmi("Factura generada desde Sistema Integrado"); 
        // dInfoFisc es opcional en FE, obligatorio en NR. Aquí lo dejamos null o agregamos si se requiere.

        // --- 2. Timbrado (gTimb) ---
        de.setgTimb(new TgTimb());
        de.getgTimb().setiTiDE(TTiDE.FACTURA_ELECTRONICA);
        de.getgTimb().setdNumTim(view.getNumeroTimbrado().intValue());
        de.getgTimb().setdEst(view.getCodigoEstablecimiento());
        de.getgTimb().setdPunExp(view.getCodigoPuntoExpedicion());
        de.getgTimb().setdNumDoc(String.format("%07d", view.getNumeroDocumento()));
        if (view.getFechaInicioTimbrado() != null) {
            de.getgTimb().setdFeIniT(view.getFechaInicioTimbrado());
        } else {
            // Fallback a fecha de emisión si no hay fecha inicio timbrado (Validar regla de negocio)
            de.getgTimb().setdFeIniT(view.getFechaEmision().toLocalDate());
        }

        // --- 3. Datos Generales (gDatGralOpe) ---
        de.setgDatGralOpe(new TdDatGralOpe());
        de.getgDatGralOpe().setdFeEmiDE(view.getFechaEmision());

        // A. Operación Comercial (gOpeCom) - OBLIGATORIO para FE
        TgOpeCom opeCom = new TgOpeCom();
        // Mapeo del tipo de transacción (D011)
        // Asumiendo que view.getTipoOperacion() trae valores alineados con SIFEN o hacemos mapping
        // 1=Venta Mercadería, 2=Servicios, etc.
        // Si tu sistema usa otros IDs, ajusta este switch
        short tipoTransaccion = view.getTipoOperacion() != null ? view.getTipoOperacion().shortValue() : 1;
        opeCom.setiTipTra(TTipTra.getByVal(tipoTransaccion)); 
        
        opeCom.setiTImp(TTImp.IVA); // Impuesto afectado: IVA (1)
        opeCom.setcMoneOpe(CMondT.PYG); // Moneda: Guaraníes por defecto. Ajustar si tu vista trae moneda.
        
        // Si fuera moneda extranjera:
        // opeCom.setdTiCam(tipoCambio);
        // opeCom.setdCondTiCam(TiCondTiCam.GLOBAL); 
        
        de.getgDatGralOpe().setgOpeCom(opeCom);

        // B. Emisor (gEmis)
        TgEmis emisor = new TgEmis();
        // Separar RUC y DV
        if (view.getEmisorRuc().contains("-")) {
            emisor.setdRucEm(view.getEmisorRuc().split("-")[0]);
            emisor.setdDVEmi(view.getEmisorRuc().split("-")[1]);
        } else {
            emisor.setdRucEm(view.getEmisorRuc());
            emisor.setdDVEmi("0");
        }
        
        emisor.setiTipCont(TiTipCont.PERSONA_JURIDICA); // Ajustar según lógica de tu empresa
        emisor.setcTipReg(TTipReg.REGIMEN_CONTABLE);    // Ajustar según configuración
        emisor.setdNomEmi(view.getEmisorRazonSocial());
        emisor.setdDirEmi(view.getEmisorDireccion());
        emisor.setdNumCas("0");
        
        // Datos Geográficos Emisor (Ajustar mapeo de códigos)
        emisor.setcDepEmi(TDepartamento.CENTRAL); // Deberías mapear view.getEmisorCiudadId() a TDepartamento
        emisor.setcCiuEmi(view.getEmisorCiudadId()); // Asunción
        emisor.setdDesCiuEmi(view.getEmisorCiudadDesc());
        emisor.setdTelEmi(view.getEmisorTelefono()); // Dato que faltaba en la vista, idealmente agregarlo
        emisor.setdEmailE(view.getEmisorEmail());

        // Actividad Económica
        List<TgActEco> actEcos = new ArrayList<>();
        TgActEco act1 = new TgActEco();
        act1.setcActEco("62020"); // Código genérico, ajustar
        act1.setdDesActEco("ACTIVIDADES DE CONSULTORÍA Y GESTIÓN DE SERVICIOS INFORMÁTICOS");
        actEcos.add(act1);
        
        TgActEco act2 = new TgActEco();
        act2.setcActEco("33200"); // Código genérico, ajustar
        act2.setdDesActEco("INSTALACIÓN DE MÁQUINAS Y EQUIPOS");
        actEcos.add(act2);
        emisor.setgActEcoList(actEcos);
        de.getgDatGralOpe().setgEmis(emisor);

        // C. Receptor (gDatRec)
        TgDatRec receptor = new TgDatRec();
        receptor.setdNomRec(view.getReceptorRazonSocial());
        // Operación B2C/B2B (D202)
        // Mapeo simple: Si tiene RUC es B2B (1), sino B2C (2). O usar view.getNaturalezaReceptor()
        receptor.setiTiOpe(view.getReceptorDocumentoNumero().contains("-") ? TiTiOpe.B2B : TiTiOpe.B2C);
        receptor.setcPaisRec(PaisType.PRY);

        if (view.getReceptorDocumentoNumero().contains("-")) {
            // Es Contribuyente
            receptor.setiNatRec(TiNatRec.CONTRIBUYENTE);
            receptor.setiTiContRec(TiTipCont.PERSONA_FISICA); // O Jurídica
            String[] rucParts = view.getReceptorDocumentoNumero().split("-");
            receptor.setdRucRec(rucParts[0]);
            receptor.setdDVRec(Short.valueOf(rucParts[1]));
        } else {
            // No Contribuyente
            receptor.setiNatRec(TiNatRec.NO_CONTRIBUYENTE);
            short tipoDoc = view.getReceptorTipoDocumentoCodigo() != null ? view.getReceptorTipoDocumentoCodigo().shortValue() : TiTipDocRec.CEDULA_PARAGUAYA.getVal();
            receptor.setiTipIDRec(TiTipDocRec.getByVal(tipoDoc));
            receptor.setdNumIDRec(view.getReceptorDocumentoNumero());
        }

        // Dirección del Receptor (Opcional en B2C, Obligatorio en B2B/Remisiones)
        if (view.getReceptorDireccion() != null && !view.getReceptorDireccion().isEmpty()) {
            receptor.setdDirRec(view.getReceptorDireccion());
            receptor.setdNumCasRec(0);
            // Si tu vista no trae Dpto/Ciudad del receptor, usa valores genéricos para evitar rechazo
            // O déjalos null si no son estrictamente obligatorios para tu caso B2C
            receptor.setcDepRec(TDepartamento.getByVal(view.getReceptorDepartamentoId().shortValue()));
            receptor.setcDisRec(view.getReceptorDistritoId().shortValue());
            receptor.setcCiuRec(view.getReceptorCiudadId());
            receptor.setdDesCiuRec(view.getReceptorCiudadDesc() != null ? view.getReceptorCiudadDesc() : "ASUNCION (DISTRITO)");
            receptor.setdDesDisRec(view.getReceptorDistritoDesc() != null ? view.getReceptorDistritoDesc() : "ASUNCION (DISTRITO)");
        }
        // Contacto Receptor
        if (view.getReceptorTelefono() != null) receptor.setdTelRec(view.getReceptorTelefono());
        if (view.getReceptorEmail() != null) receptor.setdEmailRec(view.getReceptorEmail());

        de.getgDatGralOpe().setgDatRec(receptor);
        
		List<EntidadGobierno> entGobs =  entidadGobiernoRepository.findByRuc(view.getReceptorDocumentoNumero());
		
		
		if(entGobs != null && entGobs.size()>0) {
			//cuando TiTiOpe.B2G
			de.getgDatGralOpe().getgDatRec().setiTiOpe(TiTiOpe.B2G);
			TgCompPub gCompPub = new TgCompPub();
			gCompPub.setdAnoCont((short) 25);
			gCompPub.setdEntCont(11111);
			LocalDate fechaContrato = LocalDate.now();
			fechaContrato = fechaContrato.minusDays(10l);
			gCompPub.setdFeCodCont(fechaContrato);
			gCompPub.setdModCont("11");
			gCompPub.setdSecCont(1111111);
			de.getgDtipDE().getgCamFE().setgCompPub(gCompPub);	
			//cuando TiTiOpe.B2G
			
		}

        // --- 4. Datos Específicos FE (gDtipDE -> gCamFE) ---
        de.setgDtipDE(new TgDtipDE());
        TgCamFE camFE = new TgCamFE();
        camFE.setiIndPres(TiIndPres.OPERACION_PRESENCIAL); // O ajustar según lógica
        de.getgDtipDE().setgCamFE(camFE);

        // --- 5. Condición de Operación (gCamCond) ---
        TgCamCond camCond = new TgCamCond();
        // view.getCondicionTipo(): 1=Contado, 2=Crédito
        boolean esContado = view.getCondicionTipo() != null && view.getCondicionTipo() == 1;
        camCond.setiCondOpe(esContado ? TiCondOpe.CONTADO : TiCondOpe.CREDITO);

        if (esContado) {
            // Pago Contado
            List<TgPaConEIni> listaPagos = new ArrayList<>();
            TgPaConEIni pago = new TgPaConEIni();
            pago.setiTiPago(TiTiPago.EFECTIVO); // Asumimos efectivo por defecto o mapear de vista
            pago.setdDesTiPag("Efectivo");
            pago.setdMonTiPag(view.getTotalOperacion()); // Monto total
            pago.setcMoneTiPag(CMondT.PYG);
            listaPagos.add(pago);
            camCond.setgPaConEIniList(listaPagos);
        } else {
            // Pago Crédito
            TgPagCred cred = new TgPagCred();
            cred.setiCondCred(TiCondCred.PLAZO); // O CUOTA según negocio
            cred.setdPlazoCre(view.getCreditoPlazo() != null ? view.getCreditoPlazo() : "30 días");
            
            // Si fuera por cuotas:
            // cred.setiCondCred(TiCondCred.CUOTA);
            // cred.setdCuotas((short) view.getCantidadCuotas());
            // List<TgCuotas> listaCuotas = ...
            // cred.setgCuotasList(listaCuotas);
            
            camCond.setgPagCred(cred);
        }
        de.getgDtipDE().setgCamCond(camCond);

        // --- 6. Documentos Asociados (gCamDEAsoc) ---
        // Si la factura proviene de una remisión
        if (view.getDocumentosAsociados() != null && !view.getDocumentosAsociados().isEmpty()) {
            List<TgCamDEAsoc> docsAsoc = new ArrayList<>();
            for (FacturaSifenView.DocumentoAsociadoJson docJson : view.getDocumentosAsociados()) {
                TgCamDEAsoc asoc = new TgCamDEAsoc();
                // E12.1 - Indicador de tipo de documento asociado (1=Electrónico, 2=Impreso)
                // Asumimos electrónico si tiene CDC
                asoc.setiTipDocAso(TiTipDocAso.ELECTRONICO); 
                asoc.setdCdCDERef(docJson.getCdc()); // CDC de la Remisión
                
                // Si fuera impreso, se llenarían otros campos (timbrado, numero, etc.)
                docsAsoc.add(asoc);
            }
            de.setgCamDEAsocList(docsAsoc);
        }

        // --- 7. Ítems (gCamItem) ---
        List<TgCamItem> items = new ArrayList<>();
        
        for (FacturaSifenView.DetalleFacturaJson detalle : view.getDetalles()) {
            TgCamItem item = new TgCamItem();
            item.setdCodInt(detalle.getCodigoInterno());
            item.setdDesProSer(detalle.getDescripcion());
            item.setcUniMed(TcUniMed.getByVal(detalle.getUnidadMedidaCod().shortValue())); // 77=UNI
            item.setdCantProSer(detalle.getCantidad());
            
            // Valores del Ítem
            TgValorItem val = new TgValorItem();
            val.setdPUniProSer(detalle.getPrecioUnitario());
            
         // --- CORRECCIÓN INICIO ---
            // Siempre debemos instanciar TgValorRestaItem, aunque sea con ceros.
            // El SDK lo necesita para generar el nodo gValorRestaItem y calcular el total (EA008).
            TgValorRestaItem resta = new TgValorRestaItem();
            
            if (detalle.getDescuento() != null && detalle.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                resta.setdDescItem(detalle.getDescuento());
                // Opcional: Si tienes el porcentaje, setear resta.setdPorcDesIt(...)
            } else {
                // Si no hay descuento, el SDK suele manejar null como cero, 
                // pero por seguridad podemos no setear nada o setear ZERO.
                // Lo importante es que el objeto 'resta' exista.
                resta.setdDescItem(BigDecimal.ZERO);
            }
            
            // Asignar SIEMPRE el objeto resta al valor del ítem
            val.setgValorRestaItem(resta);
            // --- CORRECCIÓN FIN ---
            
            // Impuestos (IVA)
            TgCamIVA iva = new TgCamIVA();
            // Mapeo: 1=Gravado, 2=Exonerado, 3=Exento
            int afecIva = detalle.getAfectacionIva() != null ? detalle.getAfectacionIva() : 1; 
            iva.setiAfecIVA(TiAfecIVA.getByVal((short)afecIva));
            
            iva.setdPropIVA(afecIva == 1 || afecIva == 4 ? new BigDecimal(100) : BigDecimal.ZERO);
            iva.setdTasaIVA(new BigDecimal(detalle.getTasaIva())); // 10, 5, 0
            
            // Base Gravada y Liquidación lo calcula el SDK automáticamente al llamar a generarXml
            // pero si necesitas setear algo manual, hazlo aquí.
            
            if(de.getgDatGralOpe().getgDatRec().getiTiOpe() == TiTiOpe.B2G) {
				
				item.setdDncpG("11111111");
				item.setdDncpE("1111");					
				
			}
            
            item.setgCamIVA(iva);
            item.setgValorItem(val);
            items.add(item);
        }
        
        de.getgDtipDE().setgCamItemList(items);

        // --- 8. Totales (gTotSub) ---
        // El SDK calcula automáticamente los subtotales, totales de IVA y total general
        // basándose en los ítems cargados. Solo instanciamos el objeto.
        de.setgTotSub(new TgTotSub());

        return de;
    }
    
    public DocumentoElectronico generarNotaCreditoElectronica(String idDocumento, EDoc edoc, NotaCredito nc) throws AppException {
			DocumentoElectronico de = new DocumentoElectronico();
			de.setdSisFact((short)1);
			//de.Id y de.dDVId se setean en de.generalXml()
			
			if (edoc.getId() == null) {
				edoc.setIdDocumentoOriginal(idDocumento);
				edoc.setTipoDocumento("NC");
				edoc.setNroDocumento(String.format("%7s", nc.getNroNc().trim()).replace(' ', '0'));
				edoc.setEstablecimiento(nc.getEstablecimiento());
				edoc.setPuntoExp(nc.getPuntoExp());
				edoc.setFechaEmision(nc.getNotaFechahora());
				edoc.setEnviado("NO");
	        }
			
			//TgOpeDE
			de.setgOpeDE(new TgOpeDE());//dCodSeg se genera en este constructor			
			de.getgOpeDE().setiTipEmi(TTipEmi.NORMAL);
			//de.getgOpeDE().setdInfoEmi("1");//
			//de.getgOpeDE().setdInfoFisc("Información de interés del Fisco respecto al DE");
			
			//TgTimb
			de.setgTimb(new TgTimb());
			de.getgTimb().setiTiDE(TTiDE.NOTA_DE_CREDITO_ELECTRONICA);			
			de.getgTimb().setdNumTim(nc.getNcredTimbrado());
			String establecimiento = nc.getEstablecimiento();
			//String establecimiento = "001";
			String puntoExp = nc.getPuntoExp();
			//String puntoExp = "001";
			String nroComp = edoc.getNroDocumento();
			de.getgTimb().setdEst(String.format("%" + 3 + "s", String.valueOf(establecimiento)).replace(' ', '0'));
			de.getgTimb().setdPunExp(String.format("%" + 3 + "s", String.valueOf(puntoExp)).replace(' ', '0'));
			de.getgTimb().setdNumDoc(String.format("%" + 7 + "s", String.valueOf(nroComp.trim())).replace(' ', '0'));
			de.getgTimb().setdFeIniT(nc.getdFeIniT());			
			//TdDatGralOpe
			de.setgDatGralOpe(new TdDatGralOpe());
			//Date fechaEmision = docs.getAutoFactura().getdFeEmiDE();	//Descomentar cuando se obtenga la fecha de la factura	
			LocalDateTime fechaEmision = nc.getNotaFechahora();	
			de.getgDatGralOpe().setdFeEmiDE(fechaEmision);
			de.getgDatGralOpe().setgOpeCom(new TgOpeCom());
			de.getgDatGralOpe().getgOpeCom().setiTipTra(TTipTra.PRESTACION_SERVICIOS);
			de.getgDatGralOpe().getgOpeCom().setiTImp(TTImp.IVA);
			de.getgDatGralOpe().getgOpeCom().setcMoneOpe(CMondT.PYG);
			
			//TdDatGralOpe.TgEmis
			de.getgDatGralOpe().setgEmis(new TgEmis());
			de.getgDatGralOpe().getgEmis().setdRucEm("80156217");		
			de.getgDatGralOpe().getgEmis().setdDVEmi("1");
			de.getgDatGralOpe().getgEmis().setiTipCont(TiTipCont.PERSONA_JURIDICA);
			de.getgDatGralOpe().getgEmis().setcTipReg(TTipReg.REGIMEN_CONTABLE);			
			de.getgDatGralOpe().getgEmis().setdNomEmi("INSTITUTO TECNICO SUPERIOR INTERCONTINENTAL");	
			de.getgDatGralOpe().getgEmis().setdDirEmi(nc.getSedDirecc());
			de.getgDatGralOpe().getgEmis().setdNumCas("0");
			//MIENTRAS ESTE EN MODO DE PRUEBAS SE DEBE ESPECIFICAR QUE NO TIENE VALOR FISCAL
//			de.getgDatGralOpe().getgEmis().setdNomEmi("INSTITUTO TECNICO SUPERIOR INTERCONTINENTAL - DOCUMENTO ELECTRÓNICO SIN VALOR COMERCIAL NI FISCAL - GENERADO EN AMBIENTE DE PRUEBA");
//			
			
			de.getgDatGralOpe().getgEmis().setdDirEmi(nc.getSedDirecc());			
			//de.getgDatGralOpe().getgEmis().setdNumCas("0");			
			// departamento y ciudad de emision por establecimiento
			de.getgDatGralOpe().getgEmis().setcDepEmi(TDepartamento.getByVal((short)nc.getIdDepartamentoSede()));
			de.getgDatGralOpe().getgEmis().setcCiuEmi(nc.getIdCiudadSede());
			de.getgDatGralOpe().getgEmis().setdDesCiuEmi(nc.getCiudadSede());			
			de.getgDatGralOpe().getgEmis().setdTelEmi(nc.getSedTel());	
			de.getgDatGralOpe().getgEmis().setdEmailE("contabutic.2017@gmail.com");				
			
			de.getgDatGralOpe().getgEmis().setgActEcoList(new ArrayList<TgActEco>());			
			
			TgActEco actEco = new TgActEco();	
			actEco.setcActEco("85220");			
			actEco.setdDesActEco("ENSEÑANZA SECUNDARIA DE FORMACIÓN TÉCNICA Y PROFESIONAL");	
			de.getgDatGralOpe().getgEmis().getgActEcoList().add(actEco);
			
			//TdDatGralOpe.TgDatRec
			de.getgDatGralOpe().setgDatRec(new TgDatRec());	
			de.getgDatGralOpe().getgDatRec().setiNatRec(nc.getTipoDocPersona().equals("RUC")?TiNatRec.CONTRIBUYENTE:TiNatRec.NO_CONTRIBUYENTE);			
			de.getgDatGralOpe().getgDatRec().setiTiOpe(TiTiOpe.B2C);
			de.getgDatGralOpe().getgDatRec().setcPaisRec(PaisType.PRY);
			de.getgDatGralOpe().getgDatRec().setdNomRec(nc.getaNombreDe());
			
			de.getgDatGralOpe().getgDatRec().setiTiContRec(TiTipCont.PERSONA_FISICA);
			if(nc.getTipoDocPersona().equalsIgnoreCase("RUC") && 
					Long.valueOf(nc.getRuc().substring(0, nc.getRuc().length()-2)) > 80000000){
				de.getgDatGralOpe().getgDatRec().setiTiContRec(TiTipCont.PERSONA_JURIDICA);				
			}
			
			if(nc.getTipoDocPersona().equalsIgnoreCase("RUC")){
				//solo el tipo de documento es RUC se informa dRucRec y dDVRec
				de.getgDatGralOpe().getgDatRec().setdRucRec(nc.getRuc().substring(0, nc.getRuc().length()-2));				
				de.getgDatGralOpe().getgDatRec().setdDVRec((short)Integer.valueOf(nc.getRuc().substring(nc.getRuc().length()-1, nc.getRuc().length())).intValue());
			}else{
				//si tiene guion se debe eliminar
				//de.getgDatGralOpe().getgDatRec().setdNumIDRec(nc.getRuc().replaceAll("-", ""));
				de.getgDatGralOpe().getgDatRec().setdNumIDRec(nc.getRuc());
				//solo cuando no es RUC se debe informar el Tipo de documento de identidad del receptor iTipIDRec
				if(nc.getTipoDocPersona().equalsIgnoreCase("CI"))
					de.getgDatGralOpe().getgDatRec().setiTipIDRec(TiTipDocRec.CEDULA_PARAGUAYA);
				else if(nc.getTipoDocPersona().equalsIgnoreCase("PAS"))
					de.getgDatGralOpe().getgDatRec().setiTipIDRec(TiTipDocRec.PASAPORTE);
				else if(nc.getTipoDocPersona().equalsIgnoreCase("DIE"))
					de.getgDatGralOpe().getgDatRec().setiTipIDRec(TiTipDocRec.CEDULA_EXTRANJERA);
				else if(nc.getTipoDocPersona().equalsIgnoreCase("INN"))
					de.getgDatGralOpe().getgDatRec().setiTipIDRec(TiTipDocRec.INNOMINADO);
				else
					de.getgDatGralOpe().getgDatRec().setiTipIDRec(TiTipDocRec.OTRO);
				
			}


				
						
			//////////////////////////////////// DATOS DE NOTA DE CREDITO ////////////////////////////////////////////
			de.setgCamDEAsocList(new ArrayList<TgCamDEAsoc> ());
			String idOrig = "";
			List<NotaCreditoDetalle> detalles = notaCreditoDetalleRepository.findByNotaCreditoId(nc.getId());
			for(NotaCreditoDetalle dnc:detalles) {		
				if(!dnc.getFactura().equals(idOrig)) {
					TgCamDEAsoc factura = new TgCamDEAsoc();			
					if(dnc.getCdcFactura() != null && !dnc.getCdcFactura().isEmpty()){
						factura.setiTipDocAso(TiTipDocAso.ELECTRONICO);
						factura.setdCdCDERef(detalles.get(0).getCdcFactura());
					}else{
						factura.setiTipDocAso(TiTipDocAso.IMPRESO);
						factura.setdNTimDI("10976918");				
						factura.setdEstDocAso(String.format("%" + 3 + "s", String.valueOf(dnc.getdEstDocAso())).replace(' ', '0'));
						factura.setdPExpDocAso(String.format("%" + 3 + "s", String.valueOf(dnc.getdPExpDocAso())).replace(' ', '0'));				
						factura.setdNumDocAso(String.format("%" + 7 + "s", String.valueOf(dnc.getdNumDocAso().trim())).replace(' ', '0'));					
						factura.setiTipoDocAso(TiTIpoDoc.FACTURA);				
						factura.setdFecEmiDI(dnc.getdFecEmiDI());
					}	
					de.getgCamDEAsocList().add(factura);	
					idOrig = dnc.getFactura();
				}
			}
			de.setgDtipDE(new TgDtipDE());	
			de.getgDtipDE().setgCamNCDE(new TgCamNCDE());
			
			switch(nc.getMotivo()) {
				case "DESCUENTO":
					de.getgDtipDE().getgCamNCDE().setiMotEmi(TiMotEmi.DESCUENTO);	
					break;
				case "DESCUENTOS":
					de.getgDtipDE().getgCamNCDE().setiMotEmi(TiMotEmi.DESCUENTO);	
					break;
				case "DEVOLUCIONES":
					de.getgDtipDE().getgCamNCDE().setiMotEmi(TiMotEmi.DEVOLUCION);	
					break;
				default:
					de.getgDtipDE().getgCamNCDE().setiMotEmi(TiMotEmi.AJUSTE_DE_PRECIO);	
			}	
			de.getgDtipDE().setgCamCond(new TgCamCond());
			de.getgDtipDE().getgCamCond().setiCondOpe(TiCondOpe.CONTADO);
			de.getgDtipDE().getgCamCond().setgPaConEIniList(new ArrayList<TgPaConEIni>());
			TgPaConEIni formaPago = new TgPaConEIni();
			formaPago.setiTiPago(TiTiPago.PAGO_ELECTRONICO);
			formaPago.setdMonTiPag(new BigDecimal(nc.getTotal()));
			formaPago.setcMoneTiPag(CMondT.PYG);
			de.getgDtipDE().getgCamCond().getgPaConEIniList().add(formaPago);
			
			//DE.TgDtipDE.gCamItem
			de.getgDtipDE().setgCamItemList(new ArrayList<TgCamItem>());
			TgCamItem item;
			TgValorItem val;
			TgCamIVA iva;	
			
			List<NotaCreditoDetalle> detNC = new ArrayList<NotaCreditoDetalle>();
			List<NotaCreditoDetalle> detNCDtos = new ArrayList<NotaCreditoDetalle>();
			for (NotaCreditoDetalle det: detalles) {
				if (det.getMonto() < 0) {
					detNCDtos.add(det);
				} else {
					detNC.add(det);
				}
			}
			
			int afecIva;		
			for(NotaCreditoDetalle det: detNC){
				item = new TgCamItem();
				item.setdCodInt(det.getCodInt());
				item.setdDesProSer(det.getDescriCta());	
				item.setcUniMed(TcUniMed.UNI);
				item.setdCantProSer(new BigDecimal(det.getCantidad()));
				
				val = new TgValorItem();
				val.setdPUniProSer(new BigDecimal(det.getMonto()));					
				if(det.getExenta() > 0)
					afecIva = 3;				
				else
					afecIva = 1;
				
				iva = new TgCamIVA();
				iva.setiAfecIVA(TiAfecIVA.getByVal((short)afecIva));				
				iva.setdPropIVA((afecIva == 1) ? new BigDecimal(100) : BigDecimal.ZERO);	
				iva.setdTasaIVA((afecIva == 1 ) ? (det.getIva5()>0?new BigDecimal(5):new BigDecimal(10)) : BigDecimal.ZERO);
				iva.setdBasExe(null);
				item.setgCamIVA(iva);
				
				val.setgValorRestaItem(new TgValorRestaItem());	
				item.setgValorItem(val);
				
				
				de.getgDtipDE().getgCamItemList().add(item);
			}
			de.setgTotSub(new TgTotSub());
			
			//TgTotSub esta parte ya hace todo solo el programa de generacion de xml
			de.setgTotSub(new TgTotSub());	
			
			return de;
		
	} 
    
    
    /**
     * Genera el objeto DocumentoElectronico (NC) a partir de la vista NotaCreditoSifenView.
     * Cumple con Manual Técnico 150.
     */
    private DocumentoElectronico generarNotaCreditoElectronicaV2(String idDocumento, EDoc edoc, NotaCreditoSifenView view) {
        DocumentoElectronico de = new DocumentoElectronico();
        de.setdSisFact((short) 1);

        // --- 0. Inicializar EDoc ---
        if (edoc.getId() == null) {
            edoc.setIdDocumentoOriginal(idDocumento);
            edoc.setTipoDocumento("NC");
            String nroDocStr = String.format("%07d", view.getNumeroDocumento());
            edoc.setNroDocumento(nroDocStr);
            edoc.setEstablecimiento(view.getCodigoEstablecimiento());
            edoc.setPuntoExp(view.getCodigoPuntoExpedicion());
            edoc.setFechaEmision(view.getFechaEmision());
            edoc.setEnviado("NO");
        }

        // --- 1. Datos Operación (gOpeDE) ---
        de.setgOpeDE(new TgOpeDE());
        de.getgOpeDE().setiTipEmi(TTipEmi.NORMAL);
        de.getgOpeDE().setdInfoEmi("Nota de Crédito generada desde Sistema Integrado");

        // --- 2. Timbrado (gTimb) ---
        de.setgTimb(new TgTimb());
        de.getgTimb().setiTiDE(TTiDE.NOTA_DE_CREDITO_ELECTRONICA);
        de.getgTimb().setdNumTim(view.getNumeroTimbrado().intValue());
        de.getgTimb().setdEst(view.getCodigoEstablecimiento());
        de.getgTimb().setdPunExp(view.getCodigoPuntoExpedicion());
        de.getgTimb().setdNumDoc(String.format("%07d", view.getNumeroDocumento()));
        if (view.getFechaInicioTimbrado() != null) {
            de.getgTimb().setdFeIniT(view.getFechaInicioTimbrado());
        } else {
            de.getgTimb().setdFeIniT(view.getFechaEmision().toLocalDate());
        }

        // --- 3. Datos Generales (gDatGralOpe) ---
        de.setgDatGralOpe(new TdDatGralOpe());
        de.getgDatGralOpe().setdFeEmiDE(view.getFechaEmision());

        // A. Operación Comercial (gOpeCom) - Obligatorio NC
        TgOpeCom opeCom = new TgOpeCom();
        // Mapear desde vista o default
        short tipoTransaccion = view.getTipoOperacion() != null ? view.getTipoOperacion().shortValue() : 1;
        opeCom.setiTipTra(TTipTra.getByVal(tipoTransaccion));
        opeCom.setiTImp(TTImp.IVA);
        opeCom.setcMoneOpe(CMondT.PYG);
        de.getgDatGralOpe().setgOpeCom(opeCom);

        // B. Emisor (gEmis)
        TgEmis emisor = new TgEmis();
        if (view.getEmisorRuc().contains("-")) {
            emisor.setdRucEm(view.getEmisorRuc().split("-")[0]);
            emisor.setdDVEmi(view.getEmisorRuc().split("-")[1]);
        } else {
            emisor.setdRucEm(view.getEmisorRuc());
            emisor.setdDVEmi("0");
        }
        
        emisor.setiTipCont(TiTipCont.PERSONA_JURIDICA); 
        emisor.setcTipReg(TTipReg.REGIMEN_CONTABLE);
        emisor.setdNomEmi(view.getEmisorRazonSocial());
        emisor.setdDirEmi(view.getEmisorDireccion());
        emisor.setdNumCas("0");
        
        emisor.setcDepEmi(TDepartamento.CENTRAL); 
        emisor.setcCiuEmi(view.getEmisorCiudadId()); 
        emisor.setdDesCiuEmi(view.getEmisorCiudadDesc());
        emisor.setdTelEmi(view.getEmisorTelefono());
        emisor.setdEmailE(view.getEmisorEmail());

        // Actividad Económica
        List<TgActEco> actEcos = new ArrayList<>();
        TgActEco act1 = new TgActEco();
        act1.setcActEco("62020"); 
        act1.setdDesActEco("ACTIVIDADES DE CONSULTORÍA Y GESTIÓN DE SERVICIOS INFORMÁTICOS");
        actEcos.add(act1);
        emisor.setgActEcoList(actEcos);
        de.getgDatGralOpe().setgEmis(emisor);

        // C. Receptor (gDatRec)
        TgDatRec receptor = new TgDatRec();
        receptor.setdNomRec(view.getReceptorRazonSocial());
        receptor.setiTiOpe(view.getReceptorDocumentoNumero().contains("-") ? TiTiOpe.B2B : TiTiOpe.B2C);
        receptor.setcPaisRec(PaisType.PRY);

        if (view.getReceptorDocumentoNumero().contains("-")) {
            receptor.setiNatRec(TiNatRec.CONTRIBUYENTE);
            receptor.setiTiContRec(TiTipCont.PERSONA_FISICA); 
            String[] rucParts = view.getReceptorDocumentoNumero().split("-");
            receptor.setdRucRec(rucParts[0]);
            receptor.setdDVRec(Short.valueOf(rucParts[1]));
        } else {
            receptor.setiNatRec(TiNatRec.NO_CONTRIBUYENTE);
            short tipoDoc = view.getReceptorTipoDocumentoCodigo() != null ? view.getReceptorTipoDocumentoCodigo().shortValue() : TiTipDocRec.CEDULA_PARAGUAYA.getVal();
            receptor.setiTipIDRec(TiTipDocRec.getByVal(tipoDoc));
            receptor.setdNumIDRec(view.getReceptorDocumentoNumero());
        }

        if (view.getReceptorDireccion() != null && !view.getReceptorDireccion().isEmpty()) {
            receptor.setdDirRec(view.getReceptorDireccion());
            receptor.setdNumCasRec(0);
            receptor.setcDepRec(TDepartamento.getByVal(view.getReceptorDepartamentoId().shortValue()));
            receptor.setcDisRec(view.getReceptorDistritoId().shortValue());
            receptor.setcCiuRec(view.getReceptorCiudadId());
            receptor.setdDesCiuRec(view.getReceptorCiudadDesc());
            receptor.setdDesDisRec(view.getReceptorDistritoDesc());
        }
        
        // Verificar Entidad de Gobierno (B2G) si aplica
        // ... (Tu lógica B2G existente) ...

        de.getgDatGralOpe().setgDatRec(receptor);

        // --- 4. Datos Específicos NC (gDtipDE -> gCamNCDE) ---
        de.setgDtipDE(new TgDtipDE());
        TgCamNCDE ncDe = new TgCamNCDE();
        
        // Motivo de emisión (E11.1)
        short motivo = view.getMotivoEmision() != null ? view.getMotivoEmision().shortValue() : TiMotEmi.DEVOLUCION.getVal();
        ncDe.setiMotEmi(TiMotEmi.getByVal(motivo));
        de.getgDtipDE().setgCamNCDE(ncDe);

        // --- 5. Documentos Asociados (gCamDEAsoc) ---
        // Grupo E12 - Obligatorio para NC
        if (view.getDocumentoAsociado() != null) {
            List<TgCamDEAsoc> docsAsoc = new ArrayList<>();
            TgCamDEAsoc asoc = new TgCamDEAsoc();
            NotaCreditoSifenView.DocumentoAsociadoJson docJson = view.getDocumentoAsociado();
            
            // 1=Electrónico, 2=Impreso
            if (docJson.getTipoDocumentoAsoc() == 1) {
                asoc.setiTipDocAso(TiTipDocAso.ELECTRONICO);
                asoc.setdCdCDERef(docJson.getCdcAsoc());
            } else {
                asoc.setiTipDocAso(TiTipDocAso.IMPRESO);
                asoc.setiTipoDocAso(TiTIpoDoc.FACTURA); // Asumimos factura
                // Timbrado
                if (docJson.getTimbradoAsoc() != null) {
                    asoc.setdNTimDI(String.valueOf(docJson.getTimbradoAsoc()));
                }
                // Formateo de número (Establecimiento-PuntoExp-Numero)
                // Ojo: Si tu vista devuelve el número entero, hay que ver si tienes est/pexp de la factura asociada
                // Por ahora usamos un formateo genérico o extraemos del JSON si lo agregamos
                // asoc.setdEstDocAso(...);
                // asoc.setdPExpDocAso(...);
                asoc.setdNumDocAso(String.format("%07d", docJson.getNumeroAsoc()));
                
                if (docJson.getFechaEmisionAsoc() != null) {
                    asoc.setdFecEmiDI(docJson.getFechaEmisionAsoc());
                }
            }
            docsAsoc.add(asoc);
            de.setgCamDEAsocList(docsAsoc);
        }

        // --- 6. Condición de Operación (gCamCond) ---
        // Para NC suele ser Contado por definición (ya se facturó antes), 
        // pero SIFEN pide el grupo. Usamos Contado -> Pago Electrónico o Compensación
        TgCamCond camCond = new TgCamCond();
        camCond.setiCondOpe(TiCondOpe.CONTADO);
        
        List<TgPaConEIni> listaPagos = new ArrayList<>();
        TgPaConEIni pago = new TgPaConEIni();
        // NOTA: Para NC, el medio de pago suele ser "Devolución" o "Compensación", 
        // pero el SDK/SIFEN valida contra una lista. TiTiPago.OTRO o COMPENSACION es común.
        pago.setiTiPago(TiTiPago.EFECTIVO); // Ajustar según negocio (ej. Compensación)
        pago.setdDesTiPag("Devolución/Compensación");
        pago.setdMonTiPag(view.getTotalOperacion());
        pago.setcMoneTiPag(CMondT.PYG);
        listaPagos.add(pago);
        camCond.setgPaConEIniList(listaPagos);
        
        de.getgDtipDE().setgCamCond(camCond);

        // --- 7. Ítems (gCamItem) ---
        List<TgCamItem> items = new ArrayList<>();
        
        for (NotaCreditoSifenView.DetalleNotaCreditoJson detalle : view.getDetalles()) {
            TgCamItem item = new TgCamItem();
            item.setdCodInt(detalle.getCodigoInterno());
            item.setdDesProSer(detalle.getDescripcion());
            item.setcUniMed(TcUniMed.getByVal(detalle.getUnidadMedidaCod().shortValue())); 
            item.setdCantProSer(detalle.getCantidad());
            
            // Valores
            TgValorItem val = new TgValorItem();
            val.setdPUniProSer(detalle.getPrecioUnitario());
            
            // Valor Resta (Descuento) - Obligatorio instanciar
            TgValorRestaItem resta = new TgValorRestaItem();
            resta.setdDescItem(BigDecimal.ZERO); // Generalmente 0 en la línea de NC
            val.setgValorRestaItem(resta);
            
            // Impuestos
            TgCamIVA iva = new TgCamIVA();
            int afecIva = detalle.getAfectacionIva() != null ? detalle.getAfectacionIva() : 1; 
            iva.setiAfecIVA(TiAfecIVA.getByVal((short)afecIva));
            
            iva.setdPropIVA(afecIva == 1 || afecIva == 4 ? new BigDecimal(100) : BigDecimal.ZERO);
            iva.setdTasaIVA(new BigDecimal(detalle.getTasaIva()));
            
            item.setgCamIVA(iva);
            item.setgValorItem(val);
            items.add(item);
        }
        
        de.getgDtipDE().setgCamItemList(items);

        // --- 8. Totales ---
        de.setgTotSub(new TgTotSub());

        return de;
    }
    
    private DocumentoElectronico generarRemisionElectronica(String idDocumento, EDoc edoc, RemisionSifenView view) {
        DocumentoElectronico de = new DocumentoElectronico();
        de.setdSisFact((short) 1);

        // --- 0. Inicializar EDoc si es nuevo ---
        if (edoc.getId() == null) {
            edoc.setIdDocumentoOriginal(idDocumento);
            edoc.setTipoDocumento("NR"); // O 'REM'
            // Formatear número de documento a 7 dígitos (ej: 0000123)
            String nroDocStr = String.format("%07d", view.getNumeroDocumento());
            edoc.setNroDocumento(nroDocStr);
            edoc.setEstablecimiento(view.getCodigoEstablecimiento());
            edoc.setPuntoExp(view.getCodigoPuntoExpedicion());
            edoc.setFechaEmision(view.getFechaEmision());
            edoc.setEnviado("NO");
        }

        // --- 1. Datos Operación (gOpeDE) ---
        de.setgOpeDE(new TgOpeDE());
        de.getgOpeDE().setiTipEmi(TTipEmi.NORMAL);
        // El InfoEmi es opcional, puedes poner algo genérico o dejarlo null
        //de.getgOpeDE().setdInfoEmi(view.getObservaciones() != null ? view.getObservaciones() : "Remisión de mercaderías");
        de.getgOpeDE().setdInfoEmi("Remisión de mercaderías y/o servicios");
        de.getgOpeDE().setdInfoFisc("Sin Valor Comercial");

        // --- 2. Timbrado (gTimb) ---
        de.setgTimb(new TgTimb());
        de.getgTimb().setiTiDE(TTiDE.NOTA_DE_REMISION_ELECTRONICA);
        de.getgTimb().setdNumTim(view.getNumeroTimbrado().intValue());
        de.getgTimb().setdEst(view.getCodigoEstablecimiento());
        de.getgTimb().setdPunExp(view.getCodigoPuntoExpedicion());
        de.getgTimb().setdNumDoc(String.format("%07d", view.getNumeroDocumento()));
        // Importante: La fecha de inicio de vigencia del timbrado debería venir de la vista o configuración
        // Por ahora uso la fecha de emisión como fallback si no está en la vista, pero idealmente debe ser la fecha real del timbrado.
        de.getgTimb().setdFeIniT(view.getFechaInicioTimbrado() != null ? view.getFechaInicioTimbrado() : null); 

        // --- 3. Datos Generales (gDatGralOpe) ---
        de.setgDatGralOpe(new TdDatGralOpe());
        de.getgDatGralOpe().setdFeEmiDE(view.getFechaEmision());
        
        // NOTA: Para Remisión (C002=7), el grupo gOpeCom (Operación Comercial) NO se debe informar según Manual 150.
        // Por lo tanto, no hacemos de.getgDatGralOpe().setgOpeCom(...);

        // --- 4. Emisor (gEmis) ---
        TgEmis emisor = new TgEmis();
        emisor.setdRucEm(view.getEmisorRuc().split("-")[0]); // Solo la parte numérica
        emisor.setdDVEmi(view.getEmisorRuc().split("-")[1]); // Digito verificador
        
        // Asumiendo persona jurídica por defecto o lógica basada en RUC
        emisor.setiTipCont(TiTipCont.PERSONA_JURIDICA); 
        emisor.setcTipReg(TTipReg.REGIMEN_CONTABLE); // Ajustar según realidad del cliente
        emisor.setdNomEmi(view.getEmisorRazonSocial());
        emisor.setdDirEmi(view.getEmisorDireccion());
        emisor.setdNumCas("0");
        
        // Mapeo de Departamento y Ciudad (Hardcoded por ahora o mapear de tabla parámetros)
        emisor.setcDepEmi(TDepartamento.CENTRAL); // Deberías mapear view.getEmisorCiudadId() a TDepartamento
        emisor.setcCiuEmi(view.getEmisorCiudadId()); // Asunción
        emisor.setdDesCiuEmi(view.getEmisorCiudadDesc());
        emisor.setdTelEmi(view.getEmisorTelefono()); // Dato que faltaba en la vista, idealmente agregarlo
        emisor.setdEmailE(view.getEmisorEmail());

        // Actividad Económica (Obligatorio)
        List<TgActEco> actEcos = new ArrayList<>();
        TgActEco act1 = new TgActEco();
        act1.setcActEco("62020"); // Código genérico, ajustar
        act1.setdDesActEco("ACTIVIDADES DE CONSULTORÍA Y GESTIÓN DE SERVICIOS INFORMÁTICOS");
        actEcos.add(act1);
        
        TgActEco act2 = new TgActEco();
        act2.setcActEco("33200"); // Código genérico, ajustar
        act2.setdDesActEco("INSTALACIÓN DE MÁQUINAS Y EQUIPOS");
        actEcos.add(act2);
        
        emisor.setgActEcoList(actEcos);
        de.getgDatGralOpe().setgEmis(emisor);

        // --- 5. Receptor (gDatRec) ---
        TgDatRec receptor = new TgDatRec();
        receptor.setdNomRec(view.getReceptorRazonSocial());
        receptor.setiTiOpe(TiTiOpe.B2C); // Por defecto
        receptor.setcPaisRec(PaisType.PRY);
        
        // Lógica RUC vs Cédula
        String docNum = view.getReceptorDocumentoNumero();
        if (docNum.contains("-")) {
            receptor.setiNatRec(TiNatRec.CONTRIBUYENTE);
            receptor.setiTiContRec(TiTipCont.PERSONA_FISICA); // O Jurídica según lógica
            String[] rucParts = docNum.split("-");
            receptor.setdRucRec(rucParts[0]);
            receptor.setdDVRec(Short.valueOf(rucParts[1]));
        } else {
            receptor.setiNatRec(TiNatRec.NO_CONTRIBUYENTE);
            receptor.setiTipIDRec(TiTipDocRec.getByVal(view.getReceptorTipoDocumentoCodigo().shortValue()));
            receptor.setdNumIDRec(docNum);
        }
        
        if (view.getReceptorDireccion() != null) {
            receptor.setdDirRec(view.getReceptorDireccion());
            receptor.setdNumCasRec(0);
            // Datos geográficos del receptor (Valores por defecto para evitar rechazo si no hay datos precisos)
            receptor.setcDepRec(TDepartamento.getByVal(view.getReceptorDepartamentoId().shortValue()));
            receptor.setcDisRec(view.getReceptorDistritoId().shortValue());
            receptor.setcCiuRec(view.getReceptorCiudadId());
            receptor.setdDesCiuRec(view.getReceptorCiudadDesc() != null ? view.getReceptorCiudadDesc() : "ASUNCION (DISTRITO)");
            receptor.setdDesDisRec(view.getReceptorDistritoDesc() != null ? view.getReceptorDistritoDesc() : "ASUNCION (DISTRITO)");
        }
        de.getgDatGralOpe().setgDatRec(receptor);

        // --- 6. Datos Específicos de Remisión (gDtipDE -> gCamNRE) ---
        de.setgDtipDE(new TgDtipDE());
        TgCamNRE camNRE = new TgCamNRE();
        
        // E501: Motivo
        camNRE.setiMotEmiNR(TiMotivTras.getByVal(view.getMotivoEmision().shortValue()));
        // E503: Responsable
        camNRE.setiRespEmiNR(TiRespEmiNR.getByVal(view.getResponsableEmision().shortValue()));
        
        // E505: Km (Opcional)
        if (view.getKmEstimados() != null) {
            camNRE.setdKmR(view.getKmEstimados());
        }
        
     // CORRECCIÓN PARA ERROR 2605
        // Si el motivo es VENTA (1) y no hay factura asociada, E506 es OBLIGATORIO.
        if (view.getMotivoEmision() == 1) { // 1 = Traslado por ventas
             // Si tu vista devuelve LocalDateTime, conviértelo a LocalDate
             if (view.getFechaFuturaFacturacion() != null) {
                 camNRE.setdFecEm(view.getFechaFuturaFacturacion().toLocalDate());
             } else {
                 // Fallback de seguridad: usar la misma fecha de traslado
                 camNRE.setdFecEm(view.getFechaInicioTraslado().toLocalDate());
             }
        }
        // E506: Fecha futura facturación (Opcional, no la tenemos en la vista, se omite)
        
        de.getgDtipDE().setgCamNRE(camNRE);

        // --- 7. Transporte (gTransp) ---
        TgTransp transp = new TgTransp();
        
        // E901: Tipo Transporte (1=Propio, 2=Tercero)
        transp.setiTipTrans(TiTTrans.getByVal(view.getTipoTransporte().shortValue()));
        
        // E903: Modalidad (1=Terrestre)
        transp.setiModTrans(TiModTrans.getByVal(view.getModalidadTransporte().shortValue()));
        
        // E905: Responsable Flete (Obligatorio) - Asumimos Emisor si es Propio, o lógica de negocio
        // Por defecto ponemos EMISOR (1) para simplificar, ajustar si receptor paga
        transp.setiRespFlete(TiRespFlete.EMISOR_FACTURA_ELECTRONICA); 
        
        // Fechas de traslado (Obligatorias)
        transp.setdIniTras(view.getFechaInicioTraslado().toLocalDate());
        transp.setdFinTras(view.getFechaFinTraslado().toLocalDate());

        // E10.1 Salida (Obligatorio para Remisión)
        TgCamSal salida = new TgCamSal();
        salida.setdDirLocSal(view.getEmisorDireccion());
        salida.setdNumCasSal((short)0);
        salida.setcDepSal(TDepartamento.CENTRAL); // Mapear correctamente
        salida.setcCiuSal(view.getEmisorCiudadId());
        salida.setdDesCiuSal(view.getEmisorCiudadDesc() != null ? view.getEmisorCiudadDesc() : "ASUNCION (DISTRITO)");
        transp.setgCamSal(salida);

        // E10.2 Entrega (Obligatorio para Remisión)
        List<TgCamEnt> listaEntregas = new ArrayList<>();
        TgCamEnt entrega = new TgCamEnt();
        entrega.setdDirLocEnt(view.getReceptorDireccion());
        entrega.setdNumCasEnt((short)0);
        entrega.setcDepEnt(TDepartamento.getByVal(view.getReceptorDepartamentoId().shortValue())); // Mapear correctamente
        entrega.setcCiuEnt(view.getReceptorCiudadId());
        entrega.setdDesCiuEnt(view.getReceptorCiudadDesc()); // Ajustar
        listaEntregas.add(entrega);
        transp.setgCamEntList(listaEntregas);

     // --- INICIO CORRECCIÓN TRANSPORTE ---
        TgCamTrans transportista = new TgCamTrans();

        // CASO 1: TRANSPORTE PROPIO (El transportista es el mismo Emisor)
        if (view.getTipoTransporte() == 1) { 
            
            // Configurar Vehículo (Solo si es propio)
            TgVehTras vehiculo = new TgVehTras();
            vehiculo.setdMarVeh(view.getVehiculoMarca() != null ? view.getVehiculoMarca() : "GENERICO");
            vehiculo.setdTiVehTras("CAMION"); 
            vehiculo.setdTipIdenVeh((short)2); // 2=Matrícula
            vehiculo.setdNroMatVeh(view.getVehiculoMatricula() != null ? view.getVehiculoMatricula() : "SIN-CHAPA");
            
            List<TgVehTras> listaVehiculos = new ArrayList<>();
            listaVehiculos.add(vehiculo);
            transp.setgVehTrasList(listaVehiculos);

            // Configurar Transportista (Datos del Emisor)
            transportista.setiNatTrans(TiNatRec.CONTRIBUYENTE); // El emisor SIEMPRE es contribuyente
            transportista.setdNomTrans(view.getEmisorRazonSocial());
            
            if (view.getEmisorRuc().contains("-")) {
                transportista.setdRucTrans(view.getEmisorRuc().split("-")[0]);
                transportista.setdDVTrans(Short.valueOf(view.getEmisorRuc().split("-")[1]));
            } else {
                transportista.setdRucTrans(view.getEmisorRuc());
                transportista.setdDVTrans((short)0); // Fallback por si falta DV
            }

            // En Transporte Propio, el Chofer es obligatorio y va dentro de gCamTrans
            transportista.setdNomChof(view.getConductorNombre() != null ? view.getConductorNombre() : "CHOFER INTERNO");
            transportista.setdNumIDChof(view.getConductorDocumento() != null ? view.getConductorDocumento() : "0");
            String direccionChofer = view.getEmisorDireccion(); 
            transportista.setdDirChof(direccionChofer);
            transportista.setdDomFisc(view.getEmisorDireccion());

            //transportista.setiTipIDTrans(TiTipDoc.CEDULA_PARAGUAYA); 

        } else { 
            // CASO 2: TRANSPORTE TERCERIZADO (Empresa externa)
            
            // Lógica defensiva para determinar si es Contribuyente o No
            String rucTercero = view.getTransportistaRuc();
            boolean tieneRucValido = rucTercero != null && rucTercero.length() > 3 && rucTercero.contains("-");

            if (tieneRucValido) {
                // ES CONTRIBUYENTE
                transportista.setiNatTrans(TiNatRec.CONTRIBUYENTE);
                transportista.setdNomTrans(view.getTransportistaRazonSocial());
                String[] rucParts = rucTercero.split("-");
                transportista.setdRucTrans(rucParts[0]);
                transportista.setdDVTrans(Short.valueOf(rucParts[1]));
            } else {
                // NO ES CONTRIBUYENTE (Aquí fallaba antes)
                transportista.setiNatTrans(TiNatRec.NO_CONTRIBUYENTE);
                transportista.setdNomTrans(view.getTransportistaRazonSocial() != null ? view.getTransportistaRazonSocial() : "TRANSPORTISTA SIN RUC");
                
                // *** FIX DEL NULL POINTER ***
                // Siempre asignamos un tipo de documento por defecto si no hay RUC
                transportista.setiTipIDTrans(TiTipDoc.CEDULA_PARAGUAYA); 
                transportista.setdNumIDTrans(rucTercero != null ? rucTercero : "0"); 
            }

            // Datos del Chofer (Opcional en tercerizado, pero si hay datos los ponemos)
            transportista.setdNomChof(view.getConductorNombre() != null ? view.getConductorNombre() : "CHOFER TERCERO");
            transportista.setdNumIDChof(view.getConductorDocumento() != null ? view.getConductorDocumento() : "0");
        }

        transp.setgCamTrans(transportista);
        de.getgDtipDE().setgTransp(transp);
        // --- FIN CORRECCIÓN TRANSPORTE ---


        // --- 8. Ítems (gCamItem) ---
        de.getgDtipDE().setgCamItemList(new ArrayList<>());
        
        for (RemisionSifenView.DetalleItemJson detalle : view.getDetalles()) {
            TgCamItem item = new TgCamItem();
            item.setdCodInt(detalle.getCodigoInterno());
            item.setdDesProSer(detalle.getDescripcion());
            item.setcUniMed(TcUniMed.getByVal(detalle.getUnidadMedidaCod().shortValue())); // 77 = UNI
            item.setdCantProSer(detalle.getCantidad());
            
            // Remisión: Precios suelen ser referenciales o cero.
            // SIFEN exige gValorItem aunque sea remisión.
            TgValorItem val = new TgValorItem();
            val.setdPUniProSer(detalle.getPrecioUnitario()); // Puede ser 0
            
            // NOTA: En Remisión, los impuestos suelen marcarse como Exentos o IVA 10% 
            // dependiendo de si se quiere valorizar el traslado. 
            // Para simplificar y evitar rechazos por cálculos, usaremos EXENTO si es traslado puro.
            // Si tu lógica requiere declarar IVA en remisión, ajusta aquí.
            
            TgCamIVA iva = new TgCamIVA();
            iva.setiAfecIVA(TiAfecIVA.EXENTO);
            iva.setdPropIVA(BigDecimal.ZERO);
            iva.setdTasaIVA(BigDecimal.ZERO);
            iva.setdBasExe(detalle.getCantidad().multiply(detalle.getPrecioUnitario()));
            
            item.setgCamIVA(iva);
            item.setgValorItem(val);
            
            de.getgDtipDE().getgCamItemList().add(item);
        }

        // Total Sub (Obligatorio por esquema, aunque sea remisión)
        // El SDK calculará los totales basándose en los items agregados si se llama a generarXml
        de.setgTotSub(new TgTotSub());

        return de;
    }
    
 // Método auxiliar para guardar el error
    private void registrarError(EDoc eDoc, String codigo, String descripcion, String idDocOriginal, String tipo) {
        try {
            ErroresDocumento error = new ErroresDocumento();
            error.setNroDocumento(eDoc.getNroDocumento());
            error.setEstablecimiento(eDoc.getEstablecimiento());
            error.setPuntoExpedicion(eDoc.getPuntoExp());
            error.setCdc(eDoc.getCdc());
            error.setFechaError(new Date());
            error.setCodigoError(codigo);
            
            // Truncar descripción si es muy larga para la BD
            String desc = descripcion != null && descripcion.length() > 255 ? descripcion.substring(0, 255) : descripcion;
            error.setDescripcionError(desc);
            
            error.setTipoDocumento(tipo);
            error.setIdDocumentoOriginal(idDocOriginal);
            error.setOperacion("ENVIO");
            
            erroresDocumentoRepository.save(error);
        } catch (Exception ex) {
            logger.severe("No se pudo guardar el log de errores: " + ex.getMessage());
        }
    }

    private void configurarCertificado() throws SifenException {
        // ... (Tu código existente de configuración del certificado) ...
        File certFile = new File(certificadoPath);
        if (!certFile.exists()) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "El archivo del certificado no existe en la ruta: " + certificadoPath);
        }

        SifenConfig config;

        if ("DEV".equalsIgnoreCase(ambiente)) {
            config = new SifenConfig(
                    SifenConfig.TipoAmbiente.DEV,
                    SifenConfig.TipoCertificadoCliente.PFX,
                    certificadoPath,
                    certificadoPass
            );
        } else {
            config = new SifenConfig(
                    SifenConfig.TipoAmbiente.PROD,
                    idCsc,
                    csc,
                    SifenConfig.TipoCertificadoCliente.PFX,
                    certificadoPath,
                    certificadoPass
            );
        }

        Sifen.setSifenConfig(config);
    }
    
    @Override
    @Transactional
    public String enviarCancelacion(String idDocumento, String tipoDocumento, String motivo) {
        try {
            logger.info("Iniciando cancelación para: " + idDocumento);

            // 1. Validaciones
            if (motivo == null || motivo.trim().isEmpty()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "DEBE PROPORCIONAR EL MOTIVO DE LA ANULACION");
            }

            // 2. Buscar Documento
            Optional<EDoc> eDocOpt = eDocRepository.findByIdDocumentoOriginalAndTipoDocumento(idDocumento, tipoDocumento);
            
            if (!eDocOpt.isPresent()) {
                throw new AppException(HttpStatus.NOT_FOUND, "NO SE ENCONTRÓ EL DOCUMENTO: " + idDocumento);
            }
            
            EDoc edoc = eDocOpt.get();

            // Descomentado: Es vital validar que ya esté aprobado antes de cancelar
            if (!"APROBADO SET".equalsIgnoreCase(edoc.getEstado())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "NO SE PUEDE CANCELAR UN DOCUMENTO QUE NO ESTÁ APROBADO (Estado actual: " + edoc.getEstado() + ")");
            }

            // 3. Configurar Sifen
            configurarCertificado();

            // 4. Construir Evento de Cancelación
            EventosDE eventosDE = new EventosDE();
            TrGesEve rGesEve = new TrGesEve();
            
            // ID Único del evento: Usamos timestamp para evitar tabla de secuencias extra
            rGesEve.setId(String.valueOf(System.currentTimeMillis()));
            
            // Fecha del evento (Ajuste Timezone para evitar error de fecha adelantada)
            rGesEve.setdFecFirma(LocalDateTime.now().minusMinutes(5));

            // Configurar tipo de evento (Cancelación)
            rGesEve.setgGroupTiEvt(new TgGroupTiEvt());
            TrGeVeCan cancelacion = new TrGeVeCan();
            cancelacion.setId(edoc.getCdc()); // CDC del documento a cancelar
            cancelacion.setmOtEve(motivo);
            rGesEve.getgGroupTiEvt().setrGeVeCan(cancelacion);

            eventosDE.setrGesEveList(new ArrayList<>());
            eventosDE.getrGesEveList().add(rGesEve);

            // 5. Enviar a Sifen
            logger.info("Enviando evento de cancelación a Sifen...");
            RespuestaRecepcionEvento respuesta = Sifen.recepcionEvento(eventosDE);
            
            logger.info("Respuesta Evento: " + respuesta.getRespuestaBruta());

            if (respuesta.getCodigoEstado() != 200) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Error HTTP Sifen: " + respuesta.getRespuestaBruta());
            }

            boolean cancelacionExitosa = false;
            StringBuilder msgSalida = new StringBuilder();

            // 6. Procesar Respuesta
            for (TgResProcEVe det : respuesta.getgResProcEVe()) {
                for (TgResProc procesado : det.getgResProc()) {
                    
                    msgSalida.append(procesado.getdCodRes()).append(" - ").append(procesado.getdMsgRes()).append("; ");

                    // 0600 = Evento Recibido/Procesado Correctamente
                    if ("0600".equalsIgnoreCase(procesado.getdCodRes())) {
                        
                        cancelacionExitosa = true;

                        // A. Guardar registro en tabla Eventos
                        Evento evt = new Evento();
                        evt.setCdc(edoc.getCdc());
                        evt.setEvento("ANULADO SET");
                        evt.setTipoDocumento(tipoDocumento);
                        evt.setObservacion(motivo);
                        evt.setFecha(new Date());
                        eventoRepository.save(evt);

                        // B. Actualizar EDoc
                        edoc.setEstado("ANULADO SET");
                        edoc.setFechaEstado(LocalDateTime.now());
                        // Guardamos el protocolo del evento de cancelación en NroTransaccion (o podrías usar otro campo)
                        edoc.setNroTransaccion(det.getdProtAut()); 
                        eDocRepository.save(edoc);
                        
                        logger.info("Documento anulado correctamente en BD y SET.");

                    } else {
                        // C. Manejo de error específico del evento
                        ErroresDocumento error = new ErroresDocumento();
                        error.setCodigoError(procesado.getdCodRes());
                        error.setDescripcionError(procesado.getdMsgRes());
                        error.setIdDocumentoOriginal(idDocumento);
                        error.setTipoDocumento(tipoDocumento);
                        error.setCdc(edoc.getCdc());
                        error.setFechaError(new Date());
                        error.setNroDocumento(edoc.getNroDocumento());
                        error.setEstablecimiento(edoc.getEstablecimiento());
                        error.setPuntoExpedicion(edoc.getPuntoExp());
                        error.setOperacion("Sifen.recepcionEvento(cancelacion)");
                        erroresDocumentoRepository.save(error);
                    }
                }
            }

            if (!cancelacionExitosa) {
                throw new AppException(HttpStatus.BAD_REQUEST, "No se pudo cancelar: " + msgSalida.toString());
            }

            return "CANCELACION EXITOSA: " + msgSalida.toString();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al cancelar: " + e.getMessage());
        }
    }
    
    @Override
    public DocumentoStatusDTO consultarEstadoDocumento(String idDocumentoOriginal, String tipoDocumento) {
        
        // 1. Buscar el EDoc filtrando por ID y TIPO
        Optional<EDoc> docs = eDocRepository.findByIdDocumentoOriginalAndTipoDocumento(idDocumentoOriginal, tipoDocumento);
        
        if (!docs.isPresent()) {
            throw new AppException(HttpStatus.NOT_FOUND, 
                "No se encontró el documento: " + idDocumentoOriginal + " (" + tipoDocumento + ")");
        }

        // Tomamos el primero (o el más reciente si ordenas por ID desc en el repo)
        EDoc edoc = docs.get();

        // 2. Buscar los errores asociados también filtrando por TIPO
        List<ErroresDocumento> errores = erroresDocumentoRepository.findByIdDocumentoOriginalAndTipoDocumento(idDocumentoOriginal, tipoDocumento);

        // 3. Retornar el DTO compuesto
        return new DocumentoStatusDTO(edoc, errores);
    }
    
}