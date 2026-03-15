package model;

public class Examen {
    private int id;
    private int courseId;
    private String date;
    private String heure;
    private int classroomId;

    // Champs joints pour l'affichage
    private String coursNom;
    private String salleNom;

    public Examen() {
    }

    public Examen(int id, int courseId, String date, String heure, int classroomId) {
        this.id = id;
        this.courseId = courseId;
        this.date = date;
        this.heure = heure;
        this.classroomId = classroomId;
    }

    public Examen(int courseId, String date, String heure, int classroomId) {
        this.courseId = courseId;
        this.date = date;
        this.heure = heure;
        this.classroomId = classroomId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public int getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(int classroomId) {
        this.classroomId = classroomId;
    }

    public String getCoursNom() {
        return coursNom;
    }

    public void setCoursNom(String coursNom) {
        this.coursNom = coursNom;
    }

    public String getSalleNom() {
        return salleNom;
    }

    public void setSalleNom(String salleNom) {
        this.salleNom = salleNom;
    }
}
