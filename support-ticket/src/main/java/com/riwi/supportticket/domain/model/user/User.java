package com.riwi.supportticket.domain.model.user;

// Usuario del sistema (solicitante, técnico o admin). Java puro, sin Spring ni JPA.
public class User {

    private Long id;            // Va en el claim "sub" del JWT y en Ticket.requesterId
    private String document;    // Único (DuplicateDocumentException)
    private String fullName;    // Editable en PUT /api/users/me
    private String email;       // Corporativo (@helpflow.com), único; es el usuario del login
    private String sede;        // Barranquilla, Bogotá, Medellín; editable
    private String area;        // Área de la empresa (ej.: Contabilidad); editable
    private boolean active;     // false = inactivo: no inicia sesión ni crea tickets
    private Role role;          // Va en el claim "role" del JWT; el registro siempre pone USUARIO
    private String password;    // Encriptada con BCrypt; nunca en texto plano ni en logs

    public User() {
    }

    public User(Long id, String document, String fullName, String email, String sede, String area,
                boolean active, Role role, String password) {
        this.id = id;
        this.document = document;
        this.fullName = fullName;
        this.email = email;
        this.sede = sede;
        this.area = area;
        this.active = active;
        this.role = role;
        this.password = password;
    }

    // Solo un usuario activo puede iniciar sesión y crear tickets
    public boolean isActive() {
        return active;
    }

    // Perfil básico: lo único que el usuario puede editar de sí mismo
    public void updateProfile(String fullName, String sede, String area) {
        this.fullName = fullName;
        this.sede = sede;
        this.area = area;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSede() {
        return sede;
    }

    public void setSede(String sede) {
        this.sede = sede;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
