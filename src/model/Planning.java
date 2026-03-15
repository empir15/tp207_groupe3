package model;

public class Planning {
    private int id;
    private int courseId;
    private int teacherId;
    private int classroomId;
    private String jour;
    private String heure;

    // Champs joints pour l'affichage
    private String coursNom;
    private String enseignantNom;
    private String salleNom;

    public Planning() {
    }

    public Planning(int id, int courseId, int teacherId, int classroomId, String jour, String heure) {
        this.id = id;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.classroomId = classroomId;
        this.jour = jour;
        this.heure = heure;
    }

    public Planning(int courseId, int teacherId, int classroomId, String jour, String heure) {
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.classroomId = classroomId;
        this.jour = jour;
        this.heure = heure;
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

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public int getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(int classroomId) {
        this.classroomId = classroomId;
    }

    public String getJour() {
        return jour;
    }

    public void setJour(String jour) {
        this.jour = jour;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public String getCoursNom() {
        return coursNom;
    }

    public void setCoursNom(String coursNom) {
        this.coursNom = coursNom;
    }

    public String getEnseignantNom() {
        return enseignantNom;
    }

    public void setEnseignantNom(String enseignantNom) {
        this.enseignantNom = enseignantNom;
    }

    public String getSalleNom() {
        return salleNom;
    }

    public void setSalleNom(String salleNom) {
        this.salleNom = salleNom;
    }
}
