package com.expresofast.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Envio")
@NamedStoredProcedureQuery(name = "Envio.porEstado", procedureName = "SP_OBTENER_ENVIOS_POR_ESTADO",
        resultClasses = Envio.class,
        parameters = @StoredProcedureParameter(mode = ParameterMode.IN, name = "pEstado", type = String.class))
public class Envio extends AuditableEntity {

    public interface RegistroAnterior extends jakarta.validation.groups.Default {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "envio_id")
    private Integer id;

    @Column(name = "codigo_rastreo", nullable = false, unique = true, length = 30)
    @NotBlank
    private String codigoRastreo;

    @Column(length = 100)
    @Size(max = 100)
    private String destinatario;

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    @Column(name = "direccion_destino", nullable = false, length = 200)
    @NotBlank
    private String direccionDestino;

    @Column(name = "peso_kg", precision = 10, scale = 2)
    @NotNull(groups = RegistroAnterior.class)
    @Positive
    private BigDecimal pesoKg;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull @Positive
    private BigDecimal costo;

    @Column(name = "estado_envio", nullable = false, length = 20)
    private String estadoEnvio;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id")
    @NotNull(groups = RegistroAnterior.class)
    private Vehiculo vehiculo;

    @ManyToOne
    @JoinColumn(name = "conductor_id")
    @NotNull(groups = RegistroAnterior.class)
    private Conductor conductor;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigoRastreo() {
        return codigoRastreo;
    }

    public void setCodigoRastreo(String codigoRastreo) {
        this.codigoRastreo = codigoRastreo;
    }

    public String getDireccionDestino() {
        return direccionDestino;
    }

    public void setDireccionDestino(String direccionDestino) {
        this.direccionDestino = direccionDestino;
    }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(BigDecimal pesoKg) {
        this.pesoKg = pesoKg;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public String getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(String estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
    }

    public Conductor getConductor() {
        return conductor;
    }

    public void setConductor(Conductor conductor) {
        this.conductor = conductor;
    }

    
}
