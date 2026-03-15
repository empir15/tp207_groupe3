package model;

/**
 * Représente une session planifiée (cours hebdomadaire).
 * Améliorations : heure_debut/fin, type de session (CM/TD/TP),
 * récurrence, et lien vers un groupe d'étudiants.
 */
public class Planning {

    // --- Types de session ---
    public enum TypeSession {
        CM, TD, TP
    }

    // --- Récurrences ---
    public enum Recurrence {
        HEBDOMADAIRE, BIMENSUELLE, UNIQUE
    }

    // --- Champs de base ---
    private int id;
    private int courseId;
    private int teacherId;
    private int classroomId;
    private int groupeId; // NOUVEAU : groupe d'étudiants

    private String jour; // "Lundi", "Mardi", ...
    private String heureDebut; // "08:00"
    private String heureFin; // "10:00" (NOUVEAU)

    private TypeSession typeSession; // CM / TD / TP (NOUVEAU)
    private Recurrence recurrence; // HEBDOMADAIRE / BIMENSUELLE / UNIQUE (NOUVEAU)

    // --- Champs joints pour l'affichage (non stockés en base) ---
    private String coursNom;
    private String enseignantNom;
    private String salleNom;
    private String groupeNom; // NOUVEAU

    // --- Constructeurs ---

    public Planning() {
    }

    /** Constructeur complet (lecture BDD avec ID) */
    public Planning(int id, int courseId, int teacherId, int classroomId, int groupeId,
            String jour, String heureDebut, String heureFin,
            TypeSession typeSession, Recurrence recurrence) {
        this.id = id;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.classroomId = classroomId;
        this.groupeId = groupeId;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.typeSession = typeSession;
        this.recurrence = recurrence;
    }

    /** Constructeur pour la création (sans ID) */
    public Planning(int courseId, int teacherId, int classroomId, int groupeId,
            String jour, String heureDebut, String heureFin,
            TypeSession typeSession, Recurrence recurrence) {
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.classroomId = classroomId;
        this.groupeId = groupeId;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.typeSession = typeSession;
        this.recurrence = recurrence;
    }

    // --- Getters & Setters ---

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

    public int getGroupeId() {
        return groupeId;
    }

    public void setGroupeId(int groupeId) {
        this.groupeId = groupeId;
    }

    public String getJour() {
        return jour;
    }

    public void setJour(String jour) {
        this.jour = jour;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public TypeSession getTypeSession() {
        return typeSession;
    }

    public void setTypeSession(TypeSession typeSession) {
        this.typeSession = typeSession;
    }

    public Recurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(Recurrence recurrence) {
        this.recurrence = recurrence;
    }

    // --- Champs d'affichage (joints) ---

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

    public String getGroupeNom() {
        return groupeNom;
    }

    public void setGroupeNom(String groupeNom) {
        this.groupeNom = groupeNom;
    }

    /** Retourne un résumé lisible de la plage horaire */
    public String getHeure() {
        return heureDebut + " – " + heureFin;
    }

    /** Retourne le label du type de session */
    public String getTypeSessionLabel() {
        return typeSession != null ? typeSession.name() : "";
    }

    /** Retourne le label de la récurrence */
    public String getRecurrenceLabel() {
        if (recurrence == null)
            return "";
        return switch (recurrence) {
            case HEBDOMADAIRE -> "Chaque semaine";
            case BIMENSUELLE -> "Toutes les 2 semaines";
            case UNIQUE -> "Une seule fois";
        };
    }

    @Override
    public String toString() {
        return "[" + (typeSession != null ? typeSession : "?") + "] "
                + coursNom + " — " + jour + " " + heureDebut + "–" + heureFin;
    }
}
