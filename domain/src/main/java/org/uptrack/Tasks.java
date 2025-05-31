package org.uptrack;

public class Tasks {
    private String title;
    private String description;
    private String status;
    private String dueDate;
    private RecordHistory history;

    public Tasks(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = "Offen";
        this.history = new RecordHistory();
        this.history.addEntry("Aufgabe erstellt");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        recordHistory("Titel geändert");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        recordHistory("Beschreibung geändert");
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        recordHistory("Status auf " + status + " geändert");
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
        recordHistory("Fälligkeitsdatum auf " + dueDate + " gesetzt");
    }

    public java.util.List<String> getHistory() {
        return history.getHistory();
    }

    private void recordHistory(String event) {
        history.addEntry(event);
    }

    public void complete() {
        this.status = "Abgeschlossen";
        recordHistory("Aufgabe abgeschlossen");
    }

    public void reopen() {
        this.status = "Wiedereröffnet";
        recordHistory("Aufgabe wiedereröffnet");
    }

    public void cancel() {
        this.status = "Abgebrochen";
        recordHistory("Aufgabe abgebrochen");
    }
}