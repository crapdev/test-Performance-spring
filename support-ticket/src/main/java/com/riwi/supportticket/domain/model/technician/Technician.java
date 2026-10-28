package com.riwi.supportticket.domain.model.technician;

import com.riwi.supportticket.domain.model.ticket.Category;

import java.math.BigDecimal;
import java.time.LocalDate;

// Técnico de la mesa de soporte. También es un User (userId) porque inicia sesión.
public class Technician {

    private Long id;                   // Va en Ticket.technicianId cuando se asigna
    private Long userId;               // Su User, para iniciar sesión con ROLE_TECNICO (documento, nombre y correo están allí)
    private String code;               // "TEC-014": lo único que devuelve el dispatch; con él se busca el técnico real
    private Specialty specialty;       // Regla "compatibilidad categoría–especialidad" (canHandle)
    private String sede;               // Regla de sede: HARDWARE solo lo atiende alguien de la misma sede
    private int maxActiveTickets;      // Regla de carga: tope de tickets ASIGNADO + EN_PROGRESO (3 en el seed)
    private BigDecimal salary;         // Solo informativo, ninguna regla lo usa
    private LocalDate affiliationDate; // Fecha en que entró a la mesa de soporte; solo informativo
    private boolean active;            // "Técnico habilitado": solo un técnico activo recibe tickets (TEC-020 está inactivo)

    public Technician() {
    }

    public Technician(Long id, Long userId, String code, Specialty specialty, String sede, int maxActiveTickets,
                      BigDecimal salary, LocalDate affiliationDate, boolean active) {
        this.id = id;
        this.userId = userId;
        this.code = code;
        this.specialty = specialty;
        this.sede = sede;
        this.maxActiveTickets = maxActiveTickets;
        this.salary = salary;
        this.affiliationDate = affiliationDate;
        this.active = active;
    }

    // Solo un técnico activo puede recibir asignaciones
    public boolean isActive() {
        return active;
    }

    // Atiende la categoría igual a su especialidad; OTRO lo atiende cualquiera
    public boolean canHandle(Category category) {
        if (category == Category.OTRO) {
            return true;
        }
        return specialty.name().equals(category.name());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Specialty getSpecialty() {
        return specialty;
    }

    public void setSpecialty(Specialty specialty) {
        this.specialty = specialty;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public int getMaxActiveTickets() {
        return maxActiveTickets;
    }

    public void setMaxActiveTickets(int maxActiveTickets) {
        this.maxActiveTickets = maxActiveTickets;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getAffiliationDate() {
        return affiliationDate;
    }

    public void setAffiliationDate(LocalDate affiliationDate) {
        this.affiliationDate = affiliationDate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
