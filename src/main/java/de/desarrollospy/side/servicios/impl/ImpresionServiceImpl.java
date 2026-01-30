package de.desarrollospy.side.servicios.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.roshka.sifen.core.beans.DocumentoElectronico;
import com.roshka.sifen.core.fields.request.de.TgActEco;
import com.roshka.sifen.core.fields.request.de.TgCamItem;
import com.roshka.sifen.core.fields.request.de.TgCamNRE;
import com.roshka.sifen.core.fields.request.de.TgCamTrans;
import com.roshka.sifen.core.fields.request.de.TgDatRec;
import com.roshka.sifen.core.fields.request.de.TgEmis;
import com.roshka.sifen.core.fields.request.de.TgTransp;
import com.roshka.sifen.core.fields.request.de.TgVehTras;
import com.roshka.sifen.internal.response.SifenObjectFactory;
import de.desarrollospy.side.modelo.EDoc;
import de.desarrollospy.side.repository.EDocRepository;
import de.desarrollospy.side.servicios.ImpresionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;


@Service
public class ImpresionServiceImpl implements ImpresionService {

    @Autowired
    private EDocRepository eDocRepository;
    

    // --- FUENTES ---
    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 6);
    private static final Font FONT_SMALL_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6);
    
    // Fuentes específicas para el Pie de Página
    private static final Font FONT_FOOTER_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font FONT_FOOTER_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
    private static final Font FONT_CDC = FontFactory.getFont(FontFactory.COURIER_BOLD, 9);
    
    @Override
    public byte[] generarFacturaPdf(String idDocumento, String tipoDocumento) throws Exception {
        
        // 1. Recuperar XML de la Base de Datos
        EDoc eDoc = eDocRepository.findByIdDocumentoOriginalAndTipoDocumento(idDocumento, tipoDocumento)
                .orElseThrow(() -> new Exception("Documento no encontrado: " + idDocumento));

        // 2. Parsear XML a Objeto SIFEN
        org.w3c.dom.Document xmlDoc = getDomDocument(eDoc.getXml());
        DocumentoElectronico de = parsearObjetoDE(xmlDoc);
        
        // Extraer datos que a veces no mapean directo en el objeto raíz al deserializar
        String cdcReal = extraerValorAtributo(xmlDoc, "DE", "Id");
        String qrReal = extraerValorEtiqueta(xmlDoc, "dCarQR"); // Extraer cadena QR del XML
        
        if (cdcReal != null) de.setId(cdcReal);

        // 3. Iniciar PDF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        document.setMargins(20, 20, 20, 20); // Márgenes
        PdfWriter writer = PdfWriter.getInstance(document, out);
        
        document.open();

        // 4. Generar Contenido según Tipo
        if ("NR".equalsIgnoreCase(tipoDocumento) || "REM".equalsIgnoreCase(tipoDocumento)) {
            generarCuerpoRemision(document, de);
        } else if ("NC".equalsIgnoreCase(tipoDocumento) || "NCE".equalsIgnoreCase(tipoDocumento)) {
            // NUEVO: Llamada para Nota de Crédito
            generarCuerpoNotaCredito(document, de);
        } else {
            generarCuerpoFactura(document, de);
        }

        // 5. Pie de Página (Común)
        agregarPiePagina(document, de, qrReal);

        document.close();
        return out.toByteArray();
    }

    // ============================================================================================
    // LÓGICA DE REMISIÓN (NRE)
    // ============================================================================================
    private void generarCuerpoRemision(Document doc, DocumentoElectronico de) throws DocumentException {
        // --- CABECERA ---
        agregarCabeceraKuDE(doc, de, "NOTA DE REMISIÓN ELECTRÓNICA");

        // --- 1. DESTINATARIO DE LA MERCADERÍA ---
        TgDatRec receptor = de.getgDatGralOpe().getgDatRec();
        
        PdfPTable tDest = new PdfPTable(1);
        tDest.setWidthPercentage(100);
        tDest.setSpacingAfter(5);
        
        PdfPCell cDest = new PdfPCell();
        cDest.setBorder(Rectangle.BOX);
        cDest.setPadding(5);
        
        // Título Sección
        Paragraph pTitulo = new Paragraph("DESTINATARIO DE LA MERCADERÍA", FONT_SMALL_BOLD);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        cDest.addElement(pTitulo);

        // Datos Destinatario
        PdfPTable tDatosDest = new PdfPTable(2);
        tDatosDest.setWidthPercentage(100);
        
        String nombreRec = receptor.getdNomRec();
        String docRec = (receptor.getdRucRec() != null) 
                ? receptor.getdRucRec() + "-" + receptor.getdDVRec() 
                : receptor.getdNumIDRec();

        addLabelValue(tDatosDest, "Nombre o Razón Social:", nombreRec);
        addLabelValue(tDatosDest, "RUC/Documento ID:", docRec);
        
        cDest.addElement(tDatosDest);
        tDest.addCell(cDest);
        doc.add(tDest);

        // --- 2. DATOS DEL TRASLADO ---
        TgTransp transp = de.getgDtipDE().getgTransp();
        TgCamNRE nre = de.getgDtipDE().getgCamNRE(); // Datos específicos NRE
        
        PdfPTable tTras = new PdfPTable(1);
        tTras.setWidthPercentage(100);
        tTras.setSpacingAfter(5);
        PdfPCell cTras = new PdfPCell();
        cTras.setBorder(Rectangle.BOX);
        cTras.setPadding(5);

        Paragraph pTitTras = new Paragraph("DATOS DEL TRASLADO", FONT_SMALL_BOLD);
        pTitTras.setAlignment(Element.ALIGN_CENTER);
        cTras.addElement(pTitTras);
        
        PdfPTable tDetTras = new PdfPTable(2);
        tDetTras.setWidthPercentage(100);
        
        addLabelValue(tDetTras, "Motivo de emisión:", nre.getiMotEmiNR().getDescripcion());
        addLabelValue(tDetTras, "Responsable:", nre.getiRespEmiNR().getDescripcion());
        addLabelValue(tDetTras, "Inicio Traslado:", formatDate(transp.getdIniTras()));
        addLabelValue(tDetTras, "Fin Traslado:", formatDate(transp.getdFinTras()));
        
        // Direcciones (Salida y Entrega)
        String dirSalida = transp.getgCamSal().getdDirLocSal() + " - " + transp.getgCamSal().getdDesCiuSal();
        String dirEntrega = transp.getgCamEntList().get(0).getdDirLocEnt() + " - " + transp.getgCamEntList().get(0).getdDesCiuEnt();
        
        addLabelValue(tDetTras, "Punto de Partida:", dirSalida);
        addLabelValue(tDetTras, "Punto de Llegada:", dirEntrega);
        
        if (nre.getdKmR() > 0) { 
            addLabelValue(tDetTras, "Kms Estimados:", String.valueOf(nre.getdKmR())); 
            addLabelValue(tDetTras, "", "");
        }

        cTras.addElement(tDetTras);
        tTras.addCell(cTras);
        doc.add(tTras);

        // --- 3. DATOS DEL VEHICULO Y CONDUCTOR ---
        
        PdfPTable tVeh = new PdfPTable(1);
        tVeh.setWidthPercentage(100);
        tVeh.setSpacingAfter(5);
        PdfPCell cVeh = new PdfPCell();
        cVeh.setBorder(Rectangle.BOX);
        cVeh.setPadding(5);
        
        Paragraph pTitVeh = new Paragraph("DATOS DEL TRANSPORTE Y VEHÍCULO", FONT_SMALL_BOLD);
        pTitVeh.setAlignment(Element.ALIGN_CENTER);
        cVeh.addElement(pTitVeh);
        
        PdfPTable tDetVeh = new PdfPTable(2);
        tDetVeh.setWidthPercentage(100);

        addLabelValue(tDetVeh, "Tipo Transporte:", transp.getiTipTrans().getDescripcion());
        addLabelValue(tDetVeh, "Modalidad:", transp.getiModTrans().getDescripcion());
        
        // Vehículo (Si existe)
        if (transp.getgVehTrasList() != null && !transp.getgVehTrasList().isEmpty()) {
            TgVehTras veh = transp.getgVehTrasList().get(0);
            addLabelValue(tDetVeh, "Vehículo:", veh.getdMarVeh() + " - " + veh.getdTiVehTras());
            addLabelValue(tDetVeh, "Matrícula (Chapa):", veh.getdNroMatVeh());
        }

        // Conductor / Transportista
        if (transp.getgCamTrans() != null) {
            TgCamTrans transData = transp.getgCamTrans();
            
            // Si es Tercero, mostrar Transportista
            if (transData.getdNomTrans() != null) {
                 addLabelValue(tDetVeh, "Transportista:", transData.getdNomTrans());
                 String rucTrans = transData.getdRucTrans() != null ? transData.getdRucTrans() + "-" + transData.getdDVTrans() : "S/R";
                 addLabelValue(tDetVeh, "RUC Transp.:", rucTrans);
            }
            
            // Chofer
            addLabelValue(tDetVeh, "Conductor:", transData.getdNomChof());
            addLabelValue(tDetVeh, "Doc. Conductor:", transData.getdNumIDChof());
            addLabelValue(tDetVeh, "Dir. Conductor:", transData.getdDirChof() != null ? transData.getdDirChof() : "-");
            addLabelValue(tDetVeh, "", "");
        }

        cVeh.addElement(tDetVeh);
        tVeh.addCell(cVeh);
        doc.add(tVeh);

        // --- 4. DATOS DE LA MERCADERIA (TABLA) ---
        Paragraph pTitMerc = new Paragraph("DATOS DE LA MERCADERÍA", FONT_SMALL_BOLD);
        pTitMerc.setAlignment(Element.ALIGN_CENTER);
        pTitMerc.setSpacingAfter(2);
        doc.add(pTitMerc);
        
        float[] cols = {1f, 1.5f, 6f}; // Cant, Unidad, Descripcion
        PdfPTable tItems = new PdfPTable(cols);
        tItems.setWidthPercentage(100);
        tItems.setHeaderRows(1);
        
        addHeaderCell(tItems, "CANTIDAD");
        addHeaderCell(tItems, "UNIDAD");
        addHeaderCell(tItems, "DESCRIPCIÓN");
        
        for (TgCamItem item : de.getgDtipDE().getgCamItemList()) {
            addCellDataCenter(tItems, item.getdCantProSer().toString());
            addCellDataCenter(tItems, item.getcUniMed().getAbreviatura()); 
            addCellDataLeft(tItems, item.getdDesProSer());
        }
        
        doc.add(tItems);
    }

    // ============================================================================================
    // LÓGICA DE FACTURA (FE)
    // ============================================================================================
    private void generarCuerpoFactura(Document doc, DocumentoElectronico de) throws DocumentException {
        // --- CABECERA ---
        agregarCabeceraKuDE(doc, de, "FACTURA ELECTRÓNICA");

        // --- DATOS DE LA OPERACIÓN ---
        PdfPTable tInfo = new PdfPTable(1);
        tInfo.setWidthPercentage(100);
        tInfo.setSpacingAfter(5);
        PdfPCell cInfo = new PdfPCell();
        cInfo.setBorder(Rectangle.BOX);
        cInfo.setPadding(5);
        
        PdfPTable tDetInfo = new PdfPTable(2);
        tDetInfo.setWidthPercentage(100);
        tDetInfo.setWidths(new float[] {0.6f, 0.4f}); // Ajuste visual
        
        // Izquierda
        PdfPTable tIzq = new PdfPTable(1);
        addLabelValueSimple(tIzq, "Fecha de Emisión:", formatDateTime(de.getgDatGralOpe().getdFeEmiDE()));
        
        // Condición
        String condicion = "Contado";
        if (de.getgDtipDE().getgCamCond().getiCondOpe().getVal() == 2) {
            condicion = "Crédito"; 
            if (de.getgDtipDE().getgCamCond().getgPagCred() != null) {
                condicion += " (" + de.getgDtipDE().getgCamCond().getgPagCred().getdPlazoCre() + ")";
            }
        }
        addLabelValueSimple(tIzq, "Condición:", condicion);
        
        // --- CAMBIO: Moneda y Tipo de Cambio ---
        String moneda = de.getgDatGralOpe().getgOpeCom().getcMoneOpe().name();
        boolean isGuarani = "PYG".equals(moneda);
        
        // Configuramos el formateador según la moneda
        DecimalFormat df = getDecimalFormatter(isGuarani);
        DecimalFormat dfGs = getDecimalFormatter(true); // Para total en Guaraníes siempre sin decimales
        
        String txtMoneda = "Moneda: " + moneda;
        BigDecimal tipoCambio = BigDecimal.ONE; 
        
        if (!isGuarani) {
            if (de.getgDatGralOpe().getgOpeCom().getdTiCam() != null) {
                tipoCambio = de.getgDatGralOpe().getgOpeCom().getdTiCam();
                // Mostramos el tipo de cambio con formato estándar (ej. 2 decimales para cotización si fuera necesario, o default)
                txtMoneda += "    Tipo de Cambio: " + getDecimalFormatter(false).format(tipoCambio);
            }
        }
        addLabelValueSimple(tIzq, "", txtMoneda); 
        
        // Derecha (Cliente)
        PdfPTable tDer = new PdfPTable(1);
        TgDatRec rec = de.getgDatGralOpe().getgDatRec();
        String docRec = (rec.getdRucRec() != null) ? rec.getdRucRec() + "-" + rec.getdDVRec() : rec.getdNumIDRec();
        
        addLabelValueSimple(tDer, "RUC/CI:", docRec);
        addLabelValueSimple(tDer, "Razón Social:", rec.getdNomRec());
        addLabelValueSimple(tDer, "Dirección:", rec.getdDirRec() != null ? rec.getdDirRec() : "-");
        
        // Juntar
        tDetInfo.addCell(createNoBorderCell(tIzq));
        tDetInfo.addCell(createNoBorderCell(tDer));
        
        cInfo.addElement(tDetInfo);
        tInfo.addCell(cInfo);
        doc.add(tInfo);

        // --- TABLA DE ITEMS ---
        float[] cols = {0.8f, 3.5f, 0.8f, 0.8f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f}; 
        PdfPTable tItems = new PdfPTable(cols);
        tItems.setWidthPercentage(100);
        tItems.setHeaderRows(1);
        
        String[] headers = {"Cod", "Descripción", "U.M.", "Cant", "Precio Unit.", "Descuento", "Exentas", "5%", "10%"};
        for(String h : headers) addHeaderCell(tItems, h);
        
        for (TgCamItem item : de.getgDtipDE().getgCamItemList()) {
            addCellDataCenter(tItems, item.getdCodInt()); // Cod
            addCellDataLeft(tItems, item.getdDesProSer()); // Descripcion
            addCellDataCenter(tItems, "UNI"); 
            addCellDataCenter(tItems, item.getdCantProSer().toString()); // Cantidad
            
            // Usamos df (con o sin decimales según moneda)
            addCellDataRight(tItems, df.format(item.getgValorItem().getdPUniProSer())); // Precio Unitario
            
            // Descuento
            BigDecimal descuento = BigDecimal.ZERO;
            if (item.getgValorItem().getgValorRestaItem() != null && item.getgValorItem().getgValorRestaItem().getdDescItem() != null) {
                descuento = item.getgValorItem().getgValorRestaItem().getdDescItem();
            }
            addCellDataRight(tItems, df.format(descuento)); 
            
            // Calculo del Total Item
            BigDecimal totalItem = item.getgValorItem().getdPUniProSer().multiply(item.getdCantProSer()).subtract(descuento);
            if (item.getgValorItem().getgValorRestaItem() != null && item.getgValorItem().getgValorRestaItem().getdTotOpeItem() != null) {
                 totalItem = item.getgValorItem().getgValorRestaItem().getdTotOpeItem();
            }

            int tasa = item.getgCamIVA().getdTasaIVA().intValue();
            
            // Columnas de Impuestos
            if (tasa == 5) {
                addCellDataRight(tItems, "0"); 
                addCellDataRight(tItems, df.format(totalItem)); // 5%
                addCellDataRight(tItems, "0"); 
            } else if (tasa == 10) {
                addCellDataRight(tItems, "0"); 
                addCellDataRight(tItems, "0"); 
                addCellDataRight(tItems, df.format(totalItem)); // 10%
            } else { 
                addCellDataRight(tItems, df.format(totalItem)); // Exenta
                addCellDataRight(tItems, "0"); 
                addCellDataRight(tItems, "0"); 
            }
        }
        
        // --- TOTALES ---
        // Fila Subtotales
        PdfPCell cSub = new PdfPCell(new Phrase("SUBTOTAL:", FONT_BOLD)); 
        cSub.setColspan(6); 
        tItems.addCell(cSub);
        
        addCellDataRight(tItems, df.format(de.getgTotSub().getdSubExe()));
        addCellDataRight(tItems, df.format(de.getgTotSub().getdSub5()));
        addCellDataRight(tItems, df.format(de.getgTotSub().getdSub10()));
        
        // Fila Total Operación
        PdfPCell cTotOp = new PdfPCell(new Phrase("TOTAL DE LA OPERACIÓN:", FONT_BOLD));
        cTotOp.setColspan(8);
        tItems.addCell(cTotOp);
        
        PdfPCell cTotOpVal = new PdfPCell(new Phrase(df.format(de.getgTotSub().getdTotOpe()), FONT_BOLD));
        cTotOpVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tItems.addCell(cTotOpVal);
        
        // --- CAMBIO: Cálculo de Total en Guaraníes ---
        BigDecimal totalOperacion = de.getgTotSub().getdTotOpe();
        BigDecimal totalGuaranies = totalOperacion; 

        if (!isGuarani) {
            // Si es moneda extranjera, multiplicamos por la cotización
            totalGuaranies = totalOperacion.multiply(tipoCambio);
        }

        // Fila Total Guaraníes (Siempre dfGs -> sin decimales)
        PdfPCell cTotGs = new PdfPCell(new Phrase("TOTAL EN GUARANÍES:", FONT_BOLD));
        cTotGs.setColspan(8);
        tItems.addCell(cTotGs);
        
        PdfPCell cTotGsVal = new PdfPCell(new Phrase(dfGs.format(totalGuaranies), FONT_BOLD));
        cTotGsVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tItems.addCell(cTotGsVal);
        
        doc.add(tItems);
        
        // Liquidación IVA
        PdfPTable tLiq = new PdfPTable(1);
        tLiq.setWidthPercentage(100);
        
        PdfPCell cLiq = new PdfPCell();
        cLiq.setPadding(3);
        cLiq.setBorder(Rectangle.BOX);
        
        String liqStr = String.format("LIQUIDACIÓN IVA:               (5%%) %s               (10%%) %s               TOTAL IVA: %s", 
                df.format(de.getgTotSub().getdIVA5()),
                df.format(de.getgTotSub().getdIVA10()),
                df.format(de.getgTotSub().getdTotIVA()));
        
        Paragraph pLiq = new Paragraph(liqStr, FONT_NORMAL);
        cLiq.addElement(pLiq);
        
        tLiq.addCell(cLiq);
        doc.add(tLiq);
    }
    
 // ============================================================================================
    // LÓGICA DE NOTA DE CRÉDITO (NC) - ACTUALIZADA MULTIMONEDA
    // ============================================================================================
    private void generarCuerpoNotaCredito(Document doc, DocumentoElectronico de) throws DocumentException {
        // --- CABECERA ---
        agregarCabeceraKuDE(doc, de, "NOTA DE CRÉDITO ELECTRÓNICA");

        // --- DATOS DE LA OPERACIÓN ---
        PdfPTable tInfo = new PdfPTable(1);
        tInfo.setWidthPercentage(100);
        tInfo.setSpacingAfter(5);
        PdfPCell cInfo = new PdfPCell();
        cInfo.setBorder(Rectangle.BOX);
        cInfo.setPadding(5);
        
        PdfPTable tDetInfo = new PdfPTable(2);
        tDetInfo.setWidthPercentage(100);
        tDetInfo.setWidths(new float[] {0.6f, 0.4f}); 
        
        // Columna Izquierda (Datos Operación)
        PdfPTable tIzq = new PdfPTable(1);
        addLabelValueSimple(tIzq, "Fecha de Emisión:", formatDateTime(de.getgDatGralOpe().getdFeEmiDE()));
        
        // Motivo de Emisión (NC)
        if (de.getgDtipDE().getgCamNCDE() != null) {
            String motivo = de.getgDtipDE().getgCamNCDE().getiMotEmi().getDescripcion();
            addLabelValueSimple(tIzq, "Motivo:", motivo);
        }
        // Documento Asociado
        if (de.getgCamDEAsocList() != null && !de.getgCamDEAsocList().isEmpty()) {
            com.roshka.sifen.core.fields.request.de.TgCamDEAsoc asoc = de.getgCamDEAsocList().get(0);
            if (asoc.getdCdCDERef() != null) {
                addLabelValueSimple(tIzq, "DE Asociado (CDC):", formatearCDC(asoc.getdCdCDERef()));
            } else {
                String nroAsoc = asoc.getdNumDocAso();
                String infoAsoc = "Timbrado: " + asoc.getdNTimDI() + " Nro: " + nroAsoc;
                addLabelValueSimple(tIzq, "Comprobante Asociado:", infoAsoc);
            }
        }
        
        // --- CAMBIO: Moneda y Tipo de Cambio en NC ---
        String moneda = de.getgDatGralOpe().getgOpeCom().getcMoneOpe().name();
        boolean isGuarani = "PYG".equals(moneda);
        
        // Configuramos el formateador según la moneda (true = sin decimales, false = 2 decimales)
        DecimalFormat df = getDecimalFormatter(isGuarani);
        DecimalFormat dfGs = getDecimalFormatter(true); // Para total en Guaraníes siempre sin decimales
        
        String txtMoneda = "Moneda: " + moneda;
        BigDecimal tipoCambio = BigDecimal.ONE; 
        
        if (!isGuarani) {
            if (de.getgDatGralOpe().getgOpeCom().getdTiCam() != null) {
                tipoCambio = de.getgDatGralOpe().getgOpeCom().getdTiCam();
                // Mostramos el tipo de cambio con formato de 2 decimales
                txtMoneda += "    Tipo de Cambio: " + getDecimalFormatter(false).format(tipoCambio);
            }
        }
        addLabelValueSimple(tIzq, "", txtMoneda);
        
        // Columna Derecha (Datos Cliente)
        PdfPTable tDer = new PdfPTable(1);
        TgDatRec rec = de.getgDatGralOpe().getgDatRec();
        String docRec = (rec.getdRucRec() != null) ? rec.getdRucRec() + "-" + rec.getdDVRec() : rec.getdNumIDRec();
        
        addLabelValueSimple(tDer, "RUC/CI:", docRec);
        addLabelValueSimple(tDer, "Razón Social:", rec.getdNomRec());
        addLabelValueSimple(tDer, "Dirección:", rec.getdDirRec() != null ? rec.getdDirRec() : "-");
        
        tDetInfo.addCell(createNoBorderCell(tIzq));
        tDetInfo.addCell(createNoBorderCell(tDer));
        
        cInfo.addElement(tDetInfo);
        tInfo.addCell(cInfo);
        doc.add(tInfo);

        // --- TABLA DE ITEMS (NC) ---
        float[] cols = {0.8f, 3.5f, 0.8f, 0.8f, 1.2f, 1.2f, 1.2f, 1.2f, 1.2f}; 
        PdfPTable tItems = new PdfPTable(cols);
        tItems.setWidthPercentage(100);
        tItems.setHeaderRows(1);
        
        String[] headers = {"Cod", "Descripción", "U.M.", "Cant", "Precio Unit.", "Descuento", "Exentas", "5%", "10%"};
        for(String h : headers) addHeaderCell(tItems, h);
        
        for (TgCamItem item : de.getgDtipDE().getgCamItemList()) {
            addCellDataCenter(tItems, item.getdCodInt()); 
            addCellDataLeft(tItems, item.getdDesProSer()); 
            addCellDataCenter(tItems, "UNI"); 
            addCellDataCenter(tItems, item.getdCantProSer().toString()); 
            
            // Usamos df (con o sin decimales según moneda)
            addCellDataRight(tItems, df.format(item.getgValorItem().getdPUniProSer())); 
            
            BigDecimal descuento = BigDecimal.ZERO;
            if (item.getgValorItem().getgValorRestaItem() != null && item.getgValorItem().getgValorRestaItem().getdDescItem() != null) {
                descuento = item.getgValorItem().getgValorRestaItem().getdDescItem();
            }
            addCellDataRight(tItems, df.format(descuento)); 
            
            // Cálculo del total por ítem (usando el valor ya calculado por SIFEN o manual)
            BigDecimal totalItem = item.getgValorItem().getdPUniProSer().multiply(item.getdCantProSer()).subtract(descuento);
            if (item.getgValorItem().getgValorRestaItem() != null && item.getgValorItem().getgValorRestaItem().getdTotOpeItem() != null) {
                 totalItem = item.getgValorItem().getgValorRestaItem().getdTotOpeItem();
            }

            int tasa = item.getgCamIVA().getdTasaIVA().intValue();
            
            // Columnas de Impuestos formateadas
            if (tasa == 5) {
                addCellDataRight(tItems, "0");
                addCellDataRight(tItems, df.format(totalItem));
                addCellDataRight(tItems, "0");
            } else if (tasa == 10) {
                addCellDataRight(tItems, "0");
                addCellDataRight(tItems, "0");
                addCellDataRight(tItems, df.format(totalItem));
            } else { 
                addCellDataRight(tItems, df.format(totalItem));
                addCellDataRight(tItems, "0");
                addCellDataRight(tItems, "0");
            }
        }
        
        // --- TOTALES (NC) ---
        // Fila Subtotales
        PdfPCell cSub = new PdfPCell(new Phrase("SUBTOTAL:", FONT_BOLD)); 
        cSub.setColspan(6); 
        tItems.addCell(cSub);
        
        addCellDataRight(tItems, df.format(de.getgTotSub().getdSubExe()));
        addCellDataRight(tItems, df.format(de.getgTotSub().getdSub5()));
        addCellDataRight(tItems, df.format(de.getgTotSub().getdSub10()));
        
        // Fila Total Operación
        PdfPCell cTotOp = new PdfPCell(new Phrase("TOTAL DE LA OPERACIÓN:", FONT_BOLD));
        cTotOp.setColspan(8);
        tItems.addCell(cTotOp);
        
        PdfPCell cTotOpVal = new PdfPCell(new Phrase(df.format(de.getgTotSub().getdTotOpe()), FONT_BOLD));
        cTotOpVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tItems.addCell(cTotOpVal);
        
        // --- Cálculo Total Guaraníes (NC) ---
        BigDecimal totalOperacion = de.getgTotSub().getdTotOpe();
        BigDecimal totalGuaranies = totalOperacion; 

        if (!isGuarani) {
            // Si es moneda extranjera, multiplicamos por la cotización
            totalGuaranies = totalOperacion.multiply(tipoCambio);
        }

        // Fila Total Guaraníes (Siempre dfGs -> sin decimales)
        PdfPCell cTotGs = new PdfPCell(new Phrase("TOTAL EN GUARANÍES:", FONT_BOLD));
        cTotGs.setColspan(8);
        tItems.addCell(cTotGs);
        
        PdfPCell cTotGsVal = new PdfPCell(new Phrase(dfGs.format(totalGuaranies), FONT_BOLD));
        cTotGsVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tItems.addCell(cTotGsVal);
        
        doc.add(tItems);
        
        // Liquidación IVA (NC) - Formateada
        PdfPTable tLiq = new PdfPTable(1);
        tLiq.setWidthPercentage(100);
        
        PdfPCell cLiq = new PdfPCell();
        cLiq.setPadding(3);
        cLiq.setBorder(Rectangle.BOX);
        
        String liqStr = String.format("LIQUIDACIÓN IVA:               (5%%) %s               (10%%) %s               TOTAL IVA: %s", 
                df.format(de.getgTotSub().getdIVA5()),
                df.format(de.getgTotSub().getdIVA10()),
                df.format(de.getgTotSub().getdTotIVA()));
        
        Paragraph pLiq = new Paragraph(liqStr, FONT_NORMAL);
        cLiq.addElement(pLiq);
        
        tLiq.addCell(cLiq);
        doc.add(tLiq);
    }

    // ============================================================================================
    // MÉTODOS AUXILIARES COMUNES
    // ============================================================================================

    private void agregarCabeceraKuDE(Document doc, DocumentoElectronico de, String tituloDocumento) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.25f, 0.45f, 0.30f});

        // 1. Logo (Izquierda)
        PdfPCell cellLogo = new PdfPCell();
        cellLogo.setBorder(Rectangle.BOX);
        cellLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        try {
            Image logo = Image.getInstance(getClass().getResource("/img/logo.png")); 
            logo.scaleToFit(120, 120);
            logo.setAlignment(Element.ALIGN_CENTER);
            cellLogo.addElement(logo);
        } catch (Exception e) {
            cellLogo.addElement(new Paragraph("LOGO", FONT_TITLE));
        }

        // 2. Datos Emisor (Centro)
        PdfPCell cellEmisor = new PdfPCell();
        cellEmisor.setBorder(Rectangle.BOX);
        cellEmisor.setPadding(5);
        TgEmis emisor = de.getgDatGralOpe().getgEmis();
        
        Paragraph pEmi = new Paragraph();
        pEmi.setLeading(9);
        pEmi.setAlignment(Element.ALIGN_CENTER);
        pEmi.add(new Chunk(emisor.getdNomEmi() + "\n", FONT_TITLE));
        
        // --- CAMBIO: Espaciado reducido para Actividades Económicas ---
        if (emisor.getgActEcoList() != null && !emisor.getgActEcoList().isEmpty()) {
            for (TgActEco act : emisor.getgActEcoList()) {
                Paragraph pAct = new Paragraph();
                pAct.setLeading(6); // Espacio entre líneas reducido
                pAct.setAlignment(Element.ALIGN_CENTER);
                pAct.add(new Chunk(act.getdDesActEco(), FONT_SMALL));
                pEmi.add(pAct);
            }
        }
        
        pEmi.add(new Chunk("Dirección: " + emisor.getdDirEmi() + "\n", FONT_SMALL));
        pEmi.add(new Chunk(emisor.getdDesCiuEmi() + " - Paraguay\n", FONT_SMALL));
        pEmi.add(new Chunk("Tel: " + emisor.getdTelEmi(), FONT_SMALL));
        cellEmisor.addElement(pEmi);

        // 3. Datos Timbrado (Derecha)
        PdfPCell cellTimb = new PdfPCell();
        cellTimb.setBorder(Rectangle.BOX);
        cellTimb.setPadding(5);
        
        Paragraph pTimb = new Paragraph();
        pTimb.setAlignment(Element.ALIGN_LEFT);
        pTimb.add(new Chunk("RUC: " + emisor.getdRucEm() + "-" + emisor.getdDVEmi() + "\n", FONT_BOLD));
        pTimb.add(new Chunk("TIMBRADO N°: " + de.getgTimb().getdNumTim() + "\n", FONT_NORMAL));
        
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String iniVig = de.getgTimb().getdFeIniT().format(fmt);
        pTimb.add(new Chunk("Inicio Vigencia: " + iniVig + "\n\n", FONT_SMALL));
        
        pTimb.add(new Chunk(tituloDocumento + "\n", FONT_BOLD));
        String nro = de.getgTimb().getdEst() + "-" + de.getgTimb().getdPunExp() + "-" + de.getgTimb().getdNumDoc();
        pTimb.add(new Chunk("N° " + nro, FONT_TITLE));
        
        cellTimb.addElement(pTimb);

        table.addCell(cellLogo);
        table.addCell(cellEmisor);
        table.addCell(cellTimb);
        
        doc.add(table);
        
        // Header "KuDE de..."
        PdfPTable tKude = new PdfPTable(1);
        tKude.setWidthPercentage(100);
        PdfPCell cKude = new PdfPCell(new Phrase("KuDE de " + tituloDocumento, FONT_SMALL));
        cKude.setBackgroundColor(new Color(240, 240, 240));
        cKude.setHorizontalAlignment(Element.ALIGN_CENTER);
        cKude.setBorder(Rectangle.NO_BORDER);
        tKude.addCell(cKude);
        doc.add(tKude);
        
        doc.add(Chunk.NEWLINE);
    }

    private void agregarPiePagina(Document doc, DocumentoElectronico de, String qrData) throws DocumentException {
        // Tabla contenedora principal (QR a la izquierda, Textos a la derecha)
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.18f, 0.82f}); // 18% QR, 82% Texto
        table.setSpacingBefore(10); 
        
        // --- COLUMNA 1: QR ---
        PdfPCell cQr = new PdfPCell();
        cQr.setBorder(Rectangle.BOX);
        cQr.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cQr.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        cQr.setPadding(1); 
        
        try {
            if (qrData != null && !qrData.isEmpty()) {
                QRCodeWriter qrWriter = new QRCodeWriter();
                // Generamos con más resolución para que no se pixele al escalar
                BitMatrix bitMatrix = qrWriter.encode(qrData, BarcodeFormat.QR_CODE, 400, 400);
                ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOut);
                Image qrImg = Image.getInstance(pngOut.toByteArray());
                
                qrImg.scaleToFit(100, 100); 
                
                cQr.addElement(qrImg);
            }
        } catch (Exception e) {
            cQr.addElement(new Phrase("QR Error", FONT_SMALL));
        }
        table.addCell(cQr);

        // --- COLUMNA 2: TEXTOS LEGALES Y CDC ---
        PdfPCell cInfo = new PdfPCell();
        cInfo.setBorder(Rectangle.BOX);
        cInfo.setPadding(6);
        
        PdfPTable tTextos = new PdfPTable(1);
        tTextos.setWidthPercentage(100);
        
        // 1. Instrucciones de Consulta
        PdfPCell cInstrucciones = new PdfPCell();
        cInstrucciones.setBorder(Rectangle.NO_BORDER);
        cInstrucciones.setPaddingBottom(4);
        
        Paragraph pInstr = new Paragraph();
        pInstr.setLeading(9); 
        pInstr.add(new Chunk("Consulte la validez de esta Factura Electrónica con el número de CDC impreso abajo en:\n", FONT_FOOTER_NORMAL));
        Font fontLink = FontFactory.getFont(FontFactory.HELVETICA, 7, Font.NORMAL, Color.BLUE);
        Chunk cLink = new Chunk("https://ekuatia.set.gov.py/consultas", fontLink);
        pInstr.add(cLink);
        cInstrucciones.addElement(pInstr);
        tTextos.addCell(cInstrucciones);

        // 2. Barra CDC (Fondo Gris)
        PdfPCell cCDC = new PdfPCell();
        cCDC.setBorder(Rectangle.NO_BORDER);
        cCDC.setBackgroundColor(new Color(230, 230, 230)); 
        cCDC.setPaddingTop(3);
        cCDC.setPaddingBottom(3);
        cCDC.setPaddingLeft(5);
        cCDC.setVerticalAlignment(Element.ALIGN_MIDDLE);

        String cdcFormateado = formatearCDC(de.getId());
        Paragraph pCDC = new Paragraph("CDC: " + cdcFormateado, FONT_CDC);
        pCDC.setAlignment(Element.ALIGN_CENTER); 
        cCDC.addElement(pCDC);
        
        PdfPCell cSpacer = new PdfPCell(); 
        cSpacer.setBorder(Rectangle.NO_BORDER);
        cSpacer.setFixedHeight(3);
        tTextos.addCell(cSpacer); 
        tTextos.addCell(cCDC);    
        tTextos.addCell(cSpacer); 

        // 3. Leyenda Legal
        PdfPCell cLegal = new PdfPCell();
        cLegal.setBorder(Rectangle.NO_BORDER);
        
        Paragraph pLegal = new Paragraph();
        pLegal.setLeading(8); 
        
        pLegal.add(new Chunk("ESTE DOCUMENTO ES UNA REPRESENTACIÓN GRÁFICA DE UN DOCUMENTO ELECTRÓNICO (XML)\n", FONT_FOOTER_BOLD));
        pLegal.add(new Chunk("Si su documento electrónico presenta algún error, podrá solicitar la modificación dentro de las 72 horas siguientes de la emisión de este comprobante.", FONT_FOOTER_NORMAL));
        
        cLegal.addElement(pLegal);
        tTextos.addCell(cLegal);

        cInfo.addElement(tTextos);
        table.addCell(cInfo);

        doc.add(table);
    }
    
    // --- Helpers de Formato ---
    
    private void addLabelValue(PdfPTable table, String label, String value) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, FONT_BOLD));
        c1.setBorder(Rectangle.NO_BORDER);
        table.addCell(c1);
        
        PdfPCell c2 = new PdfPCell(new Phrase(value, FONT_NORMAL));
        c2.setBorder(Rectangle.NO_BORDER);
        table.addCell(c2);
    }
    
    private void addLabelValueSimple(PdfPTable table, String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", FONT_BOLD));
        p.add(new Chunk(value, FONT_NORMAL));
        PdfPCell c = new PdfPCell(p);
        c.setBorder(Rectangle.NO_BORDER);
        table.addCell(c);
    }
    
    private PdfPCell createNoBorderCell(PdfPTable content) {
        PdfPCell c = new PdfPCell(content);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }
    
    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FONT_BOLD));
        c.setBackgroundColor(Color.LIGHT_GRAY);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c);
    }
    
    private void addCellDataLeft(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FONT_NORMAL));
        c.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(c);
    }
    
    private void addCellDataCenter(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FONT_NORMAL));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c);
    }
    
    private void addCellDataRight(PdfPTable table, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FONT_NORMAL));
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c);
    }

    private String formatDate(LocalDate date) {
        if(date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    private String formatDateTime(LocalDateTime date) {
        if(date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    private String formatearCDC(String cdc) {
        if (cdc == null) return "";
        return cdc.replaceAll("(.{4})", "$1 ").trim();
    }
    
    // Helper actualizado para soportar decimales dinámicos
    private DecimalFormat getDecimalFormatter(boolean isGuarani) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "PY"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        if (isGuarani) {
            return new DecimalFormat("#,###", symbols); // Sin decimales
        } else {
            return new DecimalFormat("#,###.00", symbols); // Con 2 decimales
        }
    }
    
    // Helper default para compatibilidad
    private DecimalFormat getDecimalFormatter() {
        return getDecimalFormatter(true); 
    }

    // --- Métodos de Parseo XML ---
    private org.w3c.dom.Document getDomDocument(byte[] xmlBytes) throws Exception {
        String xml = new String(Base64.getDecoder().decode(xmlBytes), StandardCharsets.UTF_8);
        xml = xml.replaceAll(">[\\s\r\n]*<", "><");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); 
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private DocumentoElectronico parsearObjetoDE(org.w3c.dom.Document doc) throws Exception {
        NodeList nodeList = doc.getElementsByTagNameNS("*", "DE");
        if (nodeList.getLength() == 0) nodeList = doc.getElementsByTagName("DE");
        if (nodeList.getLength() > 0) return SifenObjectFactory.getFromNode(nodeList.item(0), DocumentoElectronico.class);
        throw new Exception("XML Inválido: Falta nodo <DE>");
    }
    
    private String extraerValorAtributo(org.w3c.dom.Document doc, String tagName, String attributeName) {
        try {
            NodeList nodeList = doc.getElementsByTagNameNS("*", tagName);
            if (nodeList.getLength() == 0) nodeList = doc.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                Node node = nodeList.item(0);
                if (node.getAttributes().getNamedItem(attributeName) != null) {
                    return node.getAttributes().getNamedItem(attributeName).getNodeValue();
                }
            }
        } catch (Exception e) {}
        return null;
    }

    private String extraerValorEtiqueta(org.w3c.dom.Document doc, String tagName) {
        try {
            NodeList nodeList = doc.getElementsByTagNameNS("*", tagName);
            if (nodeList.getLength() == 0) nodeList = doc.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) return nodeList.item(0).getTextContent();
        } catch (Exception e) {}
        return null;
    }
}