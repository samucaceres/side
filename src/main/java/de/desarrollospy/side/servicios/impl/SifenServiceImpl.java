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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import de.desarrollospy.side.dto.DocumentoStatusDTO;
import de.desarrollospy.side.exception.AppException;
import de.desarrollospy.side.modelo.Cobro;
import de.desarrollospy.side.modelo.DetalleCobro;
import de.desarrollospy.side.modelo.EDoc;
import de.desarrollospy.side.modelo.ErroresDocumento;
import de.desarrollospy.side.modelo.Evento;
import de.desarrollospy.side.repository.CobroRepository;
import de.desarrollospy.side.repository.DetalleCobroRepository;
import de.desarrollospy.side.repository.EDocRepository;
import de.desarrollospy.side.repository.ErroresDocumentoRepository;
import de.desarrollospy.side.repository.EventoRepository;
import de.desarrollospy.side.servicios.SifenService;
import jakarta.transaction.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
 // Importar Lote
import com.roshka.sifen.core.beans.response.RespuestaRecepcionLoteDE;

@Service
public class SifenServiceImpl implements SifenService {

    private static final Logger logger = Logger.getLogger(SifenServiceImpl.class.getName());
    
    @Autowired
    private EDocRepository eDocRepository;
    @Autowired
    private CobroRepository cobroRepository;
    @Autowired
    private DetalleCobroRepository detalleCobroRepository;
    @Autowired
    private ErroresDocumentoRepository erroresDocumentoRepository;
    @Autowired
    private EventoRepository eventoRepository;

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
            
            // 1. Obtener datos
            String[] parts = idDocumento.split("-");
            Cobro cobro = cobroRepository.findByCompositeId(
                    Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), 
                    Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), 
                    Integer.parseInt(parts[4]))
                    .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "No se encontró el Cobro: " + idDocumento));

            Optional<EDoc> eDocOpt = eDocRepository.findByIdDocumentoOriginalAndTipoDocumento(idDocumento, tipoDocumento);
            EDoc eDoc;

            if (eDocOpt.isPresent()) {
                eDoc = eDocOpt.get();
                if ("APROBADO SET".equalsIgnoreCase(eDoc.getEstado())) {
                    return "El documento ya fue APROBADO por la SET. CDC: " + eDoc.getCdc();
                }
            } else {
                eDoc = new EDoc();
            }

            // Generación del Documento Electrónico
            DocumentoElectronico de;
            if ("FE".equalsIgnoreCase(tipoDocumento)) {
                de = generarFacturaElectronica(idDocumento, eDoc, cobro);
            } else {
                throw new AppException(HttpStatus.BAD_REQUEST, "Tipo de documento no implementado aún: " + tipoDocumento);
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
            eDoc.setCdc(de.getId());
            eDoc.setEstado("PENDIENTE ENVIO");
            eDoc.setXml(encodedBytes);
            eDoc = eDocRepository.save(eDoc);

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
                    error.setNroDocumento(eDoc.getNroDocumento());
                    error.setEstablecimiento(eDoc.getEstablecimiento());
                    error.setPuntoExpedicion(eDoc.getPuntoExp());
                    error.setOperacion("Sifen.recepcionLoteDE");
                    erroresDocumentoRepository.save(error);

                    // Actualización de Estado a RECHAZADO
                    eDoc.setEstado("RECHAZADO SET");
                    eDoc.setFechaEstado(LocalDateTime.now());
                    eDoc.setVerificado(null);
                    eDocRepository.save(eDoc);
                    
                } catch (Exception e) {
                    logger.severe("Error al guardar el error en la base de datos: " + e.getMessage());
                }
                
                // Retornamos el error como texto para el cliente (sin lanzar excepción para asegurar que el save() anterior persista)
                return "RECHAZADO SET: " + respuesta.getdMsgRes();
            }

            // 3. Éxito (0300): El lote fue recibido
            // Guardamos el Nro de Lote para consulta asincrónica posterior
            eDoc.setEstado("ENVIADO SET");
            eDoc.setNroLote(respuesta.getdProtConsLote()); // Guardamos el protocolo de consulta
            eDoc.setFechaEstado(LocalDateTime.now());
            eDocRepository.save(eDoc);

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
        emisor.setdRucEm("80156217");
        emisor.setdDVEmi("1");
        emisor.setiTipCont(TiTipCont.PERSONA_JURIDICA);
        emisor.setcTipReg(TTipReg.REGIMEN_CONTABLE);
        emisor.setdNomEmi("INSTITUTO TECNICO SUPERIOR INTERCONTINENTAL");
        emisor.setdDirEmi(cobro.getSedDirecc());
        emisor.setdNumCas("0");
        
        short idDepto = (cobro.getIdDepartamentoSede() != null) ? cobro.getIdDepartamentoSede().shortValue() : 0;
        TDepartamento deptoEnum = TDepartamento.getByVal(idDepto);
        if (deptoEnum == null) deptoEnum = TDepartamento.CAPITAL;
        emisor.setcDepEmi(deptoEnum);
        
        emisor.setcCiuEmi(cobro.getIdCiudadSede() != null ? cobro.getIdCiudadSede() : 1);
        emisor.setdDesCiuEmi(cobro.getCiudadSede() != null ? cobro.getCiudadSede() : "ASUNCION");
        emisor.setdTelEmi(cobro.getSedTel());
        emisor.setdEmailE("contabutic.2017@gmail.com");

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
            receptor.setiTiContRec(TiTipCont.PERSONA_FISICA);
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