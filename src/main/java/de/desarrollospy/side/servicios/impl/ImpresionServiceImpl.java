package de.desarrollospy.side.servicios.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.roshka.sifen.core.beans.DocumentoElectronico;
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
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
public class ImpresionServiceImpl implements ImpresionService {

    @Autowired
    private EDocRepository eDocRepository;
    @Autowired
    private CobroRepository cobroRepository;
    @Autowired
    private DetalleCobroRepository detalleCobroRepository;

    // Fuentes
    private static final Font FONT_BOLD_BIG = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 8);
    private static final Font FONT_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 6);
    private static final Font FONT_DATA = FontFactory.getFont(FontFactory.COURIER, 8);

    @Override
    public byte[] generarFacturaPdf(String idDocumento) throws Exception {
        
        // 1. Recuperar datos de BD
        String[] parts = idDocumento.split("-");
        Cobro cobro = cobroRepository.findByCompositeId(
                Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]), Integer.parseInt(parts[3]),
                Integer.parseInt(parts[4])
        ).orElseThrow(() -> new Exception("Cobro no encontrado"));

        List<DetalleCobro> detalles = detalleCobroRepository.findByCompositeId(
                cobro.getNroCobro(), cobro.getIdSede(), cobro.getIdCaja(),
                cobro.getNroApecie(), cobro.getIdFuncio()
        );

        EDoc eDoc = eDocRepository.findByIdDocumentoOriginalAndTipoDocumento(idDocumento, "FE")
                .orElseThrow(() -> new Exception("EDoc no encontrado"));

        // 2. PARSEO ROBUSTO DEL XML (Corrección aquí)
        // Parseamos el XML completo en un Documento DOM
        org.w3c.dom.Document xmlDoc = getDomDocument(eDoc.getXml());
        
        // Mapeamos el objeto principal (Datos factura)
        DocumentoElectronico de = parsearObjetoDE(xmlDoc);
        
        // Extraemos manualmente el CDC y el QR porque SifenObjectFactory a veces falla con SOAP envelopes
        String cdcReal = extraerValorAtributo(xmlDoc, "DE", "Id");
        String qrReal = extraerValorEtiqueta(xmlDoc, "dCarQR");
        
        // Inyectamos el CDC recuperado al objeto para que no sea null
        if(cdcReal != null && !cdcReal.isEmpty()) {
            de.setId(cdcReal); 
        }

        // 3. Generación del PDF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        document.setMargins(20, 20, 20, 20);
        PdfWriter writer = PdfWriter.getInstance(document, out);

        document.open();

        agregarCabecera(document, de);
        agregarDatosCliente(document, de);
        agregarDetallesTabla(document, detalles);
        agregarTotales(document, cobro, de);
        
        // Pasamos el QR real explícitamente
        agregarPiePagina(document, de, qrReal);

        document.close();
        return out.toByteArray();
    }

    // --- MÉTODOS DE PARSEO MANUAL (SOLUCIÓN AL ERROR) ---

    private org.w3c.dom.Document getDomDocument(byte[] xmlBytes) throws Exception {
        String xml = new String(Base64.getDecoder().decode(xmlBytes), StandardCharsets.UTF_8);
        // Limpiamos espacios entre tags que rompen el parseo
        xml = xml.replaceAll(">[\\s\r\n]*<", "><");
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // Importante para XML firmados
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private DocumentoElectronico parsearObjetoDE(org.w3c.dom.Document doc) throws Exception {
        // Buscamos el nodo DE sin importar el namespace (*)
        NodeList nodeList = doc.getElementsByTagNameNS("*", "DE");
        if (nodeList.getLength() == 0) {
            // Fallback por si no tiene namespace
            nodeList = doc.getElementsByTagName("DE");
        }
        
        if (nodeList.getLength() > 0) {
            return SifenObjectFactory.getFromNode(nodeList.item(0), DocumentoElectronico.class);
        }
        throw new Exception("No se encontró el nodo <DE> en el XML");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extraerValorEtiqueta(org.w3c.dom.Document doc, String tagName) {
        try {
            // Busqueda profunda en todo el documento (incluyendo rDE que es hermano de DE)
            NodeList nodeList = doc.getElementsByTagNameNS("*", tagName);
            if (nodeList.getLength() == 0) nodeList = doc.getElementsByTagName(tagName);

            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getTextContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // O un string vacío
    }

    // --- SECCIONES PDF ---

    private void agregarCabecera(Document doc, DocumentoElectronico de) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.25f, 0.6f, 0.35f}); 

        // Izquierda
        PdfPCell cellIzq = new PdfPCell();
        cellIzq.setBorder(Rectangle.BOX);
        cellIzq.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        // LOGO
        try {
              Image logo = Image.getInstance(getClass().getResource("/img/logo.png"));
              logo.scaleToFit(80, 80);
              logo.setAlignment(Element.ALIGN_CENTER);
              cellIzq.addElement(logo);
        } catch (Exception e) { }
        
        PdfPCell cellCentro = new PdfPCell();
        cellCentro.setBorder(Rectangle.BOX);
        cellCentro.setPadding(10);
        cellCentro.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellCentro.setVerticalAlignment(Element.ALIGN_MIDDLE);

        Paragraph pInst = new Paragraph();
        pInst.setAlignment(Element.ALIGN_CENTER);
        pInst.setLeading(12f);
        pInst.add(new Chunk(de.getgDatGralOpe().getgEmis().getdNomEmi() + "\n", FONT_BOLD_BIG));
        
        // Validar lista de actividades
        if(de.getgDatGralOpe().getgEmis().getgActEcoList() != null && !de.getgDatGralOpe().getgEmis().getgActEcoList().isEmpty()) {
            String actividad = de.getgDatGralOpe().getgEmis().getgActEcoList().get(0).getdDesActEco();
            pInst.add(new Chunk(actividad + "\n", FONT_NORMAL));
        }
        
        pInst.add(new Chunk("Dirección: " + de.getgDatGralOpe().getgEmis().getdDirEmi() + "\n", FONT_NORMAL));
        pInst.add(new Chunk(de.getgDatGralOpe().getgEmis().getdDesCiuEmi() + " - Paraguay\n", FONT_NORMAL));
        pInst.add(new Chunk("Teléfono: " + de.getgDatGralOpe().getgEmis().getdTelEmi() + "\n", FONT_NORMAL));
        pInst.add(new Chunk("Email: " + de.getgDatGralOpe().getgEmis().getdEmailE() + "\n", FONT_NORMAL));
        cellCentro.addElement(pInst);

        // Derecha (RUC Box)
        PdfPCell cellDer = new PdfPCell();
        cellDer.setBorder(Rectangle.BOX);
        
        PdfPTable tableRuc = new PdfPTable(1);
        tableRuc.setWidthPercentage(100);
        PdfPCell cellBox = new PdfPCell();
        cellBox.setBorder(Rectangle.NO_BORDER);
        cellBox.setPadding(10);
        
        Paragraph pRuc = new Paragraph();
        pRuc.setAlignment(Element.ALIGN_LEFT);
        pRuc.setLeading(12f);
        
        pRuc.add(new Chunk("RUC: " + de.getgDatGralOpe().getgEmis().getdRucEm() + "-" + de.getgDatGralOpe().getgEmis().getdDVEmi() + "\n", FONT_BOLD_BIG));
        pRuc.add(new Chunk("TIMBRADO N° " + de.getgTimb().getdNumTim() + "\n", FONT_NORMAL));
        
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String vigencia = de.getgTimb().getdFeIniT() != null ? de.getgTimb().getdFeIniT().format(fmt) : "-";
        pRuc.add(new Chunk("Inicio de Vigencia: " + vigencia + "\n\n", FONT_NORMAL));
        
        pRuc.add(new Chunk("FACTURA ELECTRÓNICA\n", FONT_BOLD_BIG));
        String nroDoc = de.getgTimb().getdEst() + "-" + de.getgTimb().getdPunExp() + "-" + de.getgTimb().getdNumDoc();
        pRuc.add(new Chunk("N°: " + nroDoc, FONT_BOLD_BIG));
        
        cellBox.addElement(pRuc);
        tableRuc.addCell(cellBox);
        cellDer.addElement(tableRuc);

        table.addCell(cellIzq);
        table.addCell(cellCentro);
        table.addCell(cellDer);
        doc.add(table);
        doc.add(Chunk.NEWLINE);
    }

    private void agregarDatosCliente(Document doc, DocumentoElectronico de) throws DocumentException {
        PdfPTable tableFrame = new PdfPTable(1);
        tableFrame.setWidthPercentage(100);
        PdfPCell cellFrame = new PdfPCell();
        cellFrame.setBorder(Rectangle.BOX);
        cellFrame.setPadding(5);
        
        PdfPTable tDatos = new PdfPTable(4); 
        tDatos.setWidthPercentage(100);
        tDatos.setWidths(new float[]{0.15f, 0.35f, 0.20f, 0.30f});

        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        addLabelValue(tDatos, "Fecha de Emisión:", de.getgDatGralOpe().getdFeEmiDE().format(fmtHora));
        addLabelValue(tDatos, "Condición de Venta:", de.getgDtipDE().getgCamCond().getiCondOpe().getDescripcion());
        
        String rucCliente = de.getgDatGralOpe().getgDatRec().getdRucRec() != null 
                ? de.getgDatGralOpe().getgDatRec().getdRucRec() + "-" + de.getgDatGralOpe().getgDatRec().getdDVRec()
                : de.getgDatGralOpe().getgDatRec().getdNumIDRec();
        
        addLabelValue(tDatos, "Ruc/Doc N°:", rucCliente);
        addLabelValue(tDatos, "Tipo de Operación:", de.getgDatGralOpe().getgOpeCom().getiTipTra().getDescripcion());

        cellFrame.addElement(tDatos);
        
        PdfPTable tDatosFull = new PdfPTable(2);
        tDatosFull.setWidthPercentage(100);
        tDatosFull.setWidths(new float[]{0.15f, 0.85f});
        
        addLabelValueRow(tDatosFull, "Razón Social:", de.getgDatGralOpe().getgDatRec().getdNomRec());
        addLabelValueRow(tDatosFull, "Dirección:", de.getgDatGralOpe().getgDatRec().getdDirRec() != null ? de.getgDatGralOpe().getgDatRec().getdDirRec() : "-");
        addLabelValueRow(tDatosFull, "Teléfono:", de.getgDatGralOpe().getgDatRec().getdTelRec() != null ? de.getgDatGralOpe().getgDatRec().getdTelRec() : "-");

        cellFrame.addElement(tDatosFull);
        tableFrame.addCell(cellFrame);
        doc.add(tableFrame);
        doc.add(Chunk.NEWLINE);
    }

    private void addLabelValue(PdfPTable table, String label, String value) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, FONT_BOLD));
        cLabel.setBorder(Rectangle.NO_BORDER);
        table.addCell(cLabel);
        PdfPCell cValue = new PdfPCell(new Phrase(value, FONT_DATA));
        cValue.setBorder(Rectangle.NO_BORDER);
        table.addCell(cValue);
    }

    private void addLabelValueRow(PdfPTable table, String label, String value) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, FONT_BOLD));
        cLabel.setBorder(Rectangle.NO_BORDER);
        table.addCell(cLabel);
        PdfPCell cValue = new PdfPCell(new Phrase(value, FONT_DATA));
        cValue.setBorder(Rectangle.NO_BORDER);
        table.addCell(cValue);
    }

    private void agregarDetallesTabla(Document doc, List<DetalleCobro> detalles) throws DocumentException {
        float[] columnWidths = {1f, 5f, 1.5f, 1.5f, 1.5f, 1.5f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setHeaderRows(2);

        // Header VALOR DE VENTA
        PdfPCell cVenta = new PdfPCell(new Phrase("VALOR DE VENTA", FONT_BOLD));
        cVenta.setColspan(3); 
        cVenta.setHorizontalAlignment(Element.ALIGN_CENTER);
        cVenta.setBorder(Rectangle.BOX);
        
        // Celdas vacías header superior
        PdfPCell empty = new PdfPCell(new Phrase(""));
        empty.setBorder(Rectangle.NO_BORDER);
        table.addCell(empty); table.addCell(empty); table.addCell(empty);
        table.addCell(cVenta);

        // Header Columnas
        String[] headers = {"CANT", "DESCRIPCION", "PRECIO", "EXENTAS", "5%", "10%"};
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, FONT_BOLD));
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setBackgroundColor(Color.LIGHT_GRAY);
            c.setBorder(Rectangle.BOX);
            c.setPadding(3);
            table.addCell(c);
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "PY"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###", symbols);

        BigDecimal subExenta = BigDecimal.ZERO;
        BigDecimal sub5 = BigDecimal.ZERO;
        BigDecimal sub10 = BigDecimal.ZERO;

        for (DetalleCobro det : detalles) {
            addCellData(table, String.valueOf(det.getCantidad()), Element.ALIGN_CENTER);
            String desc = det.getDescriCta() != null ? det.getDescriCta() : det.getDetProSer();
            addCellData(table, desc, Element.ALIGN_LEFT);
            addCellData(table, df.format(det.getMonto()), Element.ALIGN_RIGHT);

            BigDecimal totalItem = det.getMonto().multiply(new BigDecimal(det.getCantidad()));
            
            if (det.getExenta() > 0) {
                addCellData(table, df.format(totalItem), Element.ALIGN_RIGHT);
                addCellData(table, "", Element.ALIGN_RIGHT);
                addCellData(table, "", Element.ALIGN_RIGHT);
                subExenta = subExenta.add(totalItem);
            } else if (det.getIva5() > 0) {
                addCellData(table, "", Element.ALIGN_RIGHT);
                addCellData(table, df.format(totalItem), Element.ALIGN_RIGHT);
                addCellData(table, "", Element.ALIGN_RIGHT);
                sub5 = sub5.add(totalItem);
            } else {
                addCellData(table, "", Element.ALIGN_RIGHT);
                addCellData(table, "", Element.ALIGN_RIGHT);
                addCellData(table, df.format(totalItem), Element.ALIGN_RIGHT);
                sub10 = sub10.add(totalItem);
            }
        }

        // Subtotales
        PdfPCell cLabelSub = new PdfPCell(new Phrase("SUBTOTAL:", FONT_BOLD));
        cLabelSub.setColspan(3);
        cLabelSub.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cLabelSub);
        
        addCellData(table, df.format(subExenta), Element.ALIGN_RIGHT);
        addCellData(table, df.format(sub5), Element.ALIGN_RIGHT);
        addCellData(table, df.format(sub10), Element.ALIGN_RIGHT);

        // Total
        PdfPCell cLabelTot = new PdfPCell(new Phrase("TOTAL A PAGAR:", FONT_BOLD));
        cLabelTot.setColspan(5);
        cLabelTot.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cLabelTot);
        
        BigDecimal totalGeneral = subExenta.add(sub5).add(sub10);
        PdfPCell cTotalVal = new PdfPCell(new Phrase(df.format(totalGeneral), FONT_BOLD));
        cTotalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(cTotalVal);

        doc.add(table);
    }
    
    private void addCellData(PdfPTable table, String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_DATA));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3);
        table.addCell(cell);
    }

    private void agregarTotales(Document doc, Cobro cobro, DocumentoElectronico de) throws DocumentException {
        String letras = NumberToLetterConverter.convertNumberToLetter(new BigDecimal(cobro.getTotal()));
        Paragraph pLetras = new Paragraph(" Son guaraníes " + letras, FONT_NORMAL);
        pLetras.setSpacingBefore(5f);
        doc.add(pLetras);
        
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "PY"));
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", symbols);

        PdfPTable tIva = new PdfPTable(4); 
        tIva.setWidthPercentage(100);
        tIva.setWidths(new float[]{0.25f, 0.25f, 0.25f, 0.25f});
        
        BigDecimal iva5 = de.getgTotSub() != null ? de.getgTotSub().getdIVA5() : BigDecimal.ZERO;
        BigDecimal iva10 = de.getgTotSub() != null ? de.getgTotSub().getdIVA10() : BigDecimal.ZERO;
        BigDecimal totalIva = de.getgTotSub() != null ? de.getgTotSub().getdTotIVA() : BigDecimal.ZERO;

        addCellPlain(tIva, "LIQUIDACION DE IVA: ", Element.ALIGN_LEFT, true);
        addCellPlain(tIva, "(5%): " + df.format(iva5), Element.ALIGN_RIGHT,false);
        addCellPlain(tIva, "(10%): " + df.format(iva10), Element.ALIGN_RIGHT,false);
        addCellPlain(tIva, "TOTAL IVA: "+ df.format(totalIva), Element.ALIGN_RIGHT,true);
        
        doc.add(tIva);
        doc.add(new Paragraph(" ", FONT_NORMAL));
    }
    
    private void addCellPlain(PdfPTable table, String text, int align, boolean isBold) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_NORMAL));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(align);
		if (isBold) {
			cell.setPhrase(new Phrase(text, FONT_BOLD));
		}
        table.addCell(cell);
    }

    private void agregarPiePagina(Document doc, DocumentoElectronico de, String qrData) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.20f, 0.80f});

        // 1. QR Code
        PdfPCell cellQr = new PdfPCell();
        cellQr.setBorder(Rectangle.BOX);
        try {
            // USAMOS EL QR EXTRAÍDO MANUALMENTE
            if (qrData != null && !qrData.isEmpty()) {
                Image qrImage = generarImagenQR(qrData);
                qrImage.scaleToFit(110, 110);
                qrImage.setAlignment(Element.ALIGN_CENTER);
                cellQr.addElement(qrImage);
            }
        } catch (Exception e) { }
        
        // 2. CDC y Leyenda
        PdfPCell cellCdc = new PdfPCell();
        cellCdc.setBorder(Rectangle.BOX);
        cellCdc.setPaddingLeft(10);
        cellCdc.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        Paragraph p = new Paragraph();
        p.setLeading(10f);
        p.add(new Chunk("Consulte la validez de este comprobante con el número de CDC impreso abajo en:\n", FONT_NORMAL));
        p.add(new Chunk("https://ekuatia.set.gov.py/consultas\n\n", FONT_NORMAL));
        
        // USAMOS EL ID (CDC) QUE INYECTAMOS MANUALMENTE
     // --- CAMBIO AQUÍ: Formateamos el CDC ---
        String cdcRaw = de.getId() != null ? de.getId() : "";
        String cdcFormateado = formatearCDC(cdcRaw);
        
        p.add(new Chunk("CDC: " + cdcFormateado + "\n\n", FONT_BOLD)); 
        // ---------------------------------------
        
        p.add(new Chunk("ESTE DOCUMENTO ES UNA REPRESENTACIÓN GRÁFICA DE UN DOCUMENTO ELECTRÓNICO (XML)\n", FONT_BOLD));
        p.add(new Chunk("Si su documento electrónico presenta algún error solicitar la modificación dentro de las 72 horas siguientes de la emisión de este comprobante.", FONT_SMALL));
        
        cellCdc.addElement(p);

        table.addCell(cellQr);
        table.addCell(cellCdc);
        doc.add(table);
    }

    private Image generarImagenQR(String text) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return Image.getInstance(pngOutputStream.toByteArray());
    }
    
 // Método auxiliar para separar el CDC en grupos de 4 dígitos
    private String formatearCDC(String cdc) {
        if (cdc == null || cdc.isEmpty()) return "NO DISPONIBLE";
        
        // Usa una expresión regular para insertar un espacio cada 4 caracteres
        // y luego elimina el espacio final si sobra.
        return cdc.replaceAll("(.{4})", "$1 ").trim();
    }
}