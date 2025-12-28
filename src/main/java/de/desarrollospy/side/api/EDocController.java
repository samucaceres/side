package de.desarrollospy.side.api;

import com.roshka.sifen.core.beans.response.RespuestaConsultaRUC;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.desarrollospy.side.dto.CancelacionRequest;
import de.desarrollospy.side.exception.AppException;
import de.desarrollospy.side.servicios.ImpresionService;
import de.desarrollospy.side.servicios.SifenService;

@RestController
@RequestMapping("/")
public class EDocController {

    @Autowired
    private SifenService sifenService;
    @Autowired
    private ImpresionService impresionService;

    @GetMapping("/consultar-ruc/{ruc}")
    public ResponseEntity<RespuestaConsultaRUC> consultarRuc(@PathVariable("ruc") String ruc) {
        // La excepción es manejada por el GlobalExceptionHandler, 
        // así que aquí solo nos preocupamos por el caso de éxito.
        RespuestaConsultaRUC respuesta = sifenService.consultarRuc(ruc);
        return ResponseEntity.ok(respuesta);
    }
    
    @PostMapping("/procesar-documento/{idDocumento}/{tipoDocumento}")
    public ResponseEntity<?> procesarDocumento(
            @PathVariable("idDocumento") String idDocumento,
            @PathVariable("tipoDocumento") String tipoDocumento) {
        
        // Llamada al servicio
        String resultado = sifenService.procesarDocumento(idDocumento, tipoDocumento);
        
        // Retornamos JSON simple con el mensaje
        return ResponseEntity.ok(Map.of("mensaje", resultado));
    }
    
    @PostMapping("/cancelar-documento")
    public ResponseEntity<?> cancelarDocumento(@RequestBody CancelacionRequest request) {
        try {
            // Validaciones básicas
            if (request.getIdDocumento() == null || request.getMotivo() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Faltan datos obligatorios (idDocumento, motivo)"));
            }
            
            // Asignar "FE" por defecto si viene nulo
            String tipo = request.getTipoDocumento() != null ? request.getTipoDocumento() : "FE";

            String resultado = sifenService.enviarCancelacion(
                    request.getIdDocumento(), 
                    tipo, 
                    request.getMotivo()
            );
            
            return ResponseEntity.ok(Map.of("mensaje", resultado));

        } catch (AppException e) {
            // Devolvemos el mensaje controlado por el servicio
            return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno al cancelar: " + e.getMessage()));
        }
    }
    
    @GetMapping("/imprimir/{idDocumento}")
    public ResponseEntity<?> imprimirDocumento(@PathVariable("idDocumento") String idDocumento) {
        try {
            // Generar los bytes del PDF usando el servicio OpenPDF
            byte[] pdfBytes = impresionService.generarFacturaPdf(idDocumento);

            // Configurar encabezados HTTP para PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            
            // "inline" hace que el PDF se abra en el navegador. 
            // Si prefieres que se descargue directo, usa "attachment".
            headers.setContentDisposition(ContentDisposition.inline().filename(idDocumento + ".pdf").build());

            // Retornar respuesta binaria
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al generar el PDF: " + e.getMessage()));
        }
    }
}