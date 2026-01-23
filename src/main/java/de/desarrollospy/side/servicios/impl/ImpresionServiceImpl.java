package de.desarrollospy.side.servicios.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.roshka.sifen.core.beans.DocumentoElectronico;
import com.roshka.sifen.core.fields.request.de.TgCamItem;
import com.roshka.sifen.core.fields.request.de.TgCamNRE;
import com.roshka.sifen.core.fields.request.de.TgCamTrans;
import com.roshka.sifen.core.fields.request.de.TgDatRec;
import com.roshka.sifen.core.fields.request.de.TgEmis;
import com.roshka.sifen.core.fields.request.de.TgTransp;
import com.roshka.sifen.core.fields.request.de.TgVehTras;
import com.roshka.sifen.internal.response.SifenObjectFactory;
import de.desarrollospy.side.modelo.Cobro;
import de.desarrollospy.side.modelo.DetalleCobro;
import de.desarrollospy.side.modelo.EDoc;
import de.desarrollospy.side.repository.CobroRepository;
import de.desarrollospy.side.repository.DetalleCobroRepository;
import de.desarrollospy.side.repository.EDocRepository;
import de.desarrollospy.side.servicios.ImpresionService;
import de.desarrollospy.side.utils.NumberToLetterConverter;
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
import java.util.List;
import java.util.Locale;

import de.desarrollospy.side.modelo.NotaCredito;
import de.desarrollospy.side.modelo.NotaCreditoDetalle;
import de.desarrollospy.side.repository.NotaCreditoRepository;
import de.desarrollospy.side.repository.NotaCreditoDetalleRepository;

@Service
public class ImpresionServiceImpl implements ImpresionService {

    @Autowired
    private EDocRepository eDocRepository;
    @Autowired
    private CobroRepository cobroRepository;
    @Autowired
    private DetalleCobroRepository detalleCobroRepository;
    
    @Autowired
    private NotaCreditoRepository notaCreditoRepository;
    @Autowired
    private NotaCreditoDetalleRepository notaCreditoDetalleRepository;

 // --- FUENTES ---
    private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 6);
    private static final Font FONT_SMALL_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6);
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
        
        if (nre.getdKmR() > 0) { // Validamos que sea mayor a 0
            // Usamos String.valueOf porque los primitivos no tienen método .toString()
            addLabelValue(tDetTras, "Kms Estimados:", String.valueOf(nre.getdKmR())); 
            addLabelValue(tDetTras, "", "");
        }

        cTras.addElement(tDetTras);
        tTras.addCell(cTras);
        doc.add(tTras);

        // --- 3. DATOS DEL VEHICULO Y CONDUCTOR ---
        // SIFEN: gVehTras (Vehículo) y gCamTrans (Conductor/Transportista)
        
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
            addCellDataCenter(tItems, item.getcUniMed().getAbreviatura()); // Podrías mapear a descripción
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
            // Si hay info de cuotas
            if (de.getgDtipDE().getgCamCond().getgPagCred() != null) {
                condicion += " (" + de.getgDtipDE().getgCamCond().getgPagCred().getdPlazoCre() + ")";
            }
        }
        addLabelValueSimple(tIzq, "Condición:", condicion);
        addLabelValueSimple(tIzq, "Moneda:", de.getgDatGralOpe().getgOpeCom().getcMoneOpe().name());
        
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
        float[] cols = {0.8f, 3.5f, 1f, 1.2f, 1.2f, 1.2f, 1.2f}; 
        PdfPTable tItems = new PdfPTable(cols);
        tItems.setWidthPercentage(100);
        tItems.setHeaderRows(1);
        
        String[] headers = {"CANT", "DESCRIPCIÓN", "PRECIO", "EXENTA", "5%", "10%", "TOTAL"};
        for(String h : headers) addHeaderCell(tItems, h);
        
        DecimalFormat df = getDecimalFormatter();
        
        for (TgCamItem item : de.getgDtipDE().getgCamItemList()) {
            addCellDataCenter(tItems, item.getdCantProSer().toString());
            addCellDataLeft(tItems, item.getdDesProSer());
            addCellDataRight(tItems, df.format(item.getgValorItem().getdPUniProSer()));
            
            BigDecimal totalItem = item.getgValorItem().getgValorRestaItem().getdTotOpeItem(); // Precio * Cant - Desc
            if (totalItem == null) {
                // Fallback calculo manual si el SDK aun no lo populó (paso previo a XML)
                totalItem = item.getgValorItem().getdPUniProSer().multiply(item.getdCantProSer());
            }

            int tasa = item.getgCamIVA().getdTasaIVA().intValue();
            
            if (tasa == 5) {
                addCellDataRight(tItems, "0");
                addCellDataRight(tItems, df.format(totalItem));
                addCellDataRight(tItems, "0");
            } else if (tasa == 10) {
                addCellDataRight(tItems, "0");
                addCellDataRight(tItems, "0");
                addCellDataRight(tItems, df.format(totalItem));
            } else { // Exenta
                addCellDataRight(tItems, df.format(totalItem));
                addCellDataRight(tItems, "0");
                addCellDataRight(tItems, "0");
            }
             // Columna Total Linea (Opcional, según diseño)
             addCellDataRight(tItems, df.format(totalItem));
        }
        
        // --- TOTALES ---
        // Fila Subtotales
        PdfPCell cSub = new PdfPCell(new Phrase("SUBTOTALES:", FONT_BOLD));
        cSub.setColspan(3);
        tItems.addCell(cSub);
        
        addCellDataRight(tItems, df.format(de.getgTotSub().getdSubExe()));
        addCellDataRight(tItems, df.format(de.getgTotSub().getdSub5()));
        addCellDataRight(tItems, df.format(de.getgTotSub().getdSub10()));
        addCellDataRight(tItems, ""); // Espacio total
        
        // Fila Total General
        PdfPCell cTot = new PdfPCell(new Phrase("TOTAL A PAGAR:", FONT_BOLD));
        cTot.setColspan(6);
        tItems.addCell(cTot);
        
        PdfPCell cTotVal = new PdfPCell(new Phrase(df.format(de.getgTotSub().getdTotOpe()), FONT_BOLD));
        cTotVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tItems.addCell(cTotVal);
        
        doc.add(tItems);
        
        // Letras y Liquidación IVA
        PdfPTable tLiq = new PdfPTable(1);
        tLiq.setWidthPercentage(100);
        tLiq.setSpacingBefore(2);
        
        PdfPCell cLiq = new PdfPCell();
        cLiq.setPadding(3);
        
        String letras = NumberToLetterConverter.convertNumberToLetter(de.getgTotSub().getdTotOpe());
        cLiq.addElement(new Paragraph("TOTAL EN GUARANÍES: " + letras, FONT_NORMAL));
        
        String liqStr = String.format("LIQUIDACIÓN DEL IVA:   (5%%): %s   (10%%): %s   TOTAL IVA: %s", 
                df.format(de.getgTotSub().getdIVA5()),
                df.format(de.getgTotSub().getdIVA10()),
                df.format(de.getgTotSub().getdTotIVA()));
        cLiq.addElement(new Paragraph(liqStr, FONT_NORMAL));
        
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
            // Cargar logo desde resources (src/main/resources/img/logo.png)
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
        pEmi.setAlignment(Element.ALIGN_CENTER);
        pEmi.add(new Chunk(emisor.getdNomEmi() + "\n", FONT_TITLE));
        // Actividad
        if (emisor.getgActEcoList() != null && !emisor.getgActEcoList().isEmpty()) {
             pEmi.add(new Chunk(emisor.getgActEcoList().get(0).getdDesActEco() + "\n", FONT_SMALL));
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
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.25f, 0.75f});
        table.setSpacingBefore(10);

        // 1. QR
        PdfPCell cQr = new PdfPCell();
        cQr.setBorder(Rectangle.BOX);
        cQr.setPadding(2);
        try {
            if (qrData != null && !qrData.isEmpty()) {
                // Generar QR (tu método existente)
                QRCodeWriter qrWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrWriter.encode(qrData, BarcodeFormat.QR_CODE, 200, 200);
                ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOut);
                Image qrImg = Image.getInstance(pngOut.toByteArray());
                qrImg.scaleToFit(135, 135);
                cQr.addElement(qrImg);
            }
        } catch (Exception e) {}

        // 2. CDC y Leyenda
        PdfPCell cInfo = new PdfPCell();
        cInfo.setBorder(Rectangle.BOX);
        cInfo.setPadding(5);
        
        Paragraph p = new Paragraph();
        p.add(new Chunk("Consulte la validez de este Documento Electrónico con el número de CDC impreso abajo en:\n", FONT_SMALL));
        p.add(new Chunk("https://ekuatia.set.gov.py/consultas\n\n", FONT_SMALL)); // URL Oficial
        
        String cdcFmt = formatearCDC(de.getId());
        // Caja gris para el CDC como en la imagen
        Font fCdc = FontFactory.getFont(FontFactory.COURIER_BOLD, 10);
        Chunk chCdc = new Chunk("CDC: " + cdcFmt, fCdc);
        chCdc.setBackground(new Color(230, 230, 230));
        p.add(chCdc);
        p.add(Chunk.NEWLINE);
        p.add(Chunk.NEWLINE);
        
        p.add(new Chunk("ESTE DOCUMENTO ES UNA REPRESENTACIÓN GRÁFICA DE UN DOCUMENTO ELECTRÓNICO (XML)\n", FONT_BOLD));
        p.add(new Chunk("Si su documento electrónico presenta algún error, podrá solicitar la modificación dentro de las 72 horas siguientes de la emisión.", FONT_SMALL));
        
        cInfo.addElement(p);

        table.addCell(cQr);
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
    
    private DecimalFormat getDecimalFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "PY"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return new DecimalFormat("#,###", symbols);
    }

    // --- Métodos de Parseo XML (sin cambios mayores) ---
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