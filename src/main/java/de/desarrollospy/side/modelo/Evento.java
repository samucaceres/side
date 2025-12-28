package de.desarrollospy.side.modelo;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "eventos", schema = "edoc") // Asumo esquema 'edoc', ajusta si es 'public'
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cdc;
    
    private String evento; // Ej: "ANULADO SET"
    
    @Column(name = "tipo_documento")
    private String tipoDocumento;
    
    private String observacion; // Motivo
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCdc() { return cdc; }
    public void setCdc(String cdc) { this.cdc = cdc; }
    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
}