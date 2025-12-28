package de.desarrollospy.side;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SideApplication {

    // BLOQUE ESTÁTICO: Se ejecuta antes que cualquier otra cosa
    static {
    	
    	System.setProperty("com.sun.org.apache.xerces.internal.dom.deferNodeExpansion", "false");
        System.setProperty("javax.xml.soap.SOAPConnectionFactory", "com.sun.xml.messaging.saaj.client.p2p.HttpSOAPConnectionFactory");
        // 1. Forzar SAAJ a usar la implementación incluida en el pom (com.sun.xml.messaging...)
        System.setProperty("javax.xml.soap.SAAJMetaFactory", "com.sun.xml.messaging.saaj.soap.SAAJMetaFactoryImpl");
        
        // 2. Forzar explícitamente la fábrica de mensajes a SOAP 1.2
        //System.setProperty("javax.xml.soap.MessageFactory", "com.sun.xml.messaging.saaj.soap.ver1_2.SOAPMessageFactory1_2Impl");
        
        // 3. Forzar la fábrica de elementos SOAP a 1.2
        //System.setProperty("javax.xml.soap.SOAPFactory", "com.sun.xml.messaging.saaj.soap.ver1_2.SOAPFactory1_2Impl");
        
        // 4. CRÍTICO: Forzar la conexión HTTP para que use el Content-Type 'application/soap+xml' (SOAP 1.2)
        // Si esto falla, se envía como 'text/xml' y la SET devuelve "XML Mal Formado".
        //System.setProperty("javax.xml.soap.SOAPConnectionFactory", "com.sun.xml.messaging.saaj.client.p2p.HttpSOAPConnectionFactory");
    
     // FIX DOMException: NOT_FOUND_ERR en setIdAttributeNode
        System.setProperty("com.sun.org.apache.xerces.internal.dom.deferNodeExpansion", "false");
        
        // Opcional, a veces necesaria para SAAJ estricto
        System.setProperty("saaj.use.mime.multipart.allow8bit", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(SideApplication.class, args);
    }
}