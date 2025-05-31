package org.uptrack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generiert verschiedene Berichte für das UpTrack-System
 */
public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    // Konstanten für Status
    private static final String STATUS_OFFEN = "Offen";
    private static final String STATUS_ABGESCHLOSSEN = "Abgeschlossen";
    private static final String STATUS_ABGEBROCHEN = "Abgebrochen";
    private static final String STATUS_WIEDEREROEFFNET = "Wiedereröffnet";

    // Konstanten für Formate
    private static final String FORMAT_HTML = "html";
    private static final String FORMAT_TXT = "txt";
    private static final String FORMAT_CSV = "csv";
    private static final String FORMAT_PDF = "pdf";

    // Konstanten für Dateiformatierung
    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    private static final String FILENAME_DATE_FORMAT = "yyyyMMdd_HHmmss";

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ReportGenerator(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Konfigurationsklasse für Berichte
     */
    public static class ReportConfig {
        private String format;
        private boolean includeTasks;
        private boolean includeUsers;
        private boolean includeStatistics;
        private boolean includeCharts;
        private String outputPath;
        private boolean sendEmail;
        private List<String> emailRecipients;

        public ReportConfig(String format, String outputPath) {
            this.format = format;
            this.outputPath = outputPath;
            this.includeTasks = true;
            this.includeUsers = true;
            this.includeStatistics = true;
            this.includeCharts = false;
            this.sendEmail = false;
            this.emailRecipients = new ArrayList<>();
        }

        // Getter und Setter
        public String getFormat() { return format; }
        public boolean isIncludeTasks() { return includeTasks; }
        public boolean isIncludeUsers() { return includeUsers; }
        public boolean isIncludeStatistics() { return includeStatistics; }
        public boolean isIncludeCharts() { return includeCharts; }
        public String getOutputPath() { return outputPath; }
        public boolean isSendEmail() { return sendEmail; }
        public List<String> getEmailRecipients() { return emailRecipients; }

        public ReportConfig setIncludeTasks(boolean includeTasks) {
            this.includeTasks = includeTasks;
            return this;
        }

        public ReportConfig setIncludeUsers(boolean includeUsers) {
            this.includeUsers = includeUsers;
            return this;
        }

        public ReportConfig setIncludeStatistics(boolean includeStatistics) {
            this.includeStatistics = includeStatistics;
            return this;
        }

        public ReportConfig setIncludeCharts(boolean includeCharts) {
            this.includeCharts = includeCharts;
            return this;
        }

        public ReportConfig setSendEmail(boolean sendEmail) {
            this.sendEmail = sendEmail;
            return this;
        }

        public ReportConfig setEmailRecipients(List<String> emailRecipients) {
            this.emailRecipients = emailRecipients;
            return this;
        }
    }

    /**
     * Erzeugt einen Bericht mit der angegebenen Konfiguration
     */
    public boolean generateReport(ReportConfig config) {
        return generateComprehensiveReport(
                config.getFormat(),
                config.isIncludeTasks(),
                config.isIncludeUsers(),
                config.isIncludeStatistics(),
                config.isIncludeCharts(),
                config.getOutputPath(),
                config.isSendEmail(),
                config.getEmailRecipients()
        );
    }

    /**
     * Generiert einen umfassenden Bericht über alle Benutzer und Aufgaben im System.
     */
    public boolean generateComprehensiveReport(String format, boolean includeTasks, boolean includeUsers,
                                               boolean includeStatistics, boolean includeCharts,
                                               String outputPath, boolean sendEmail, List<String> emailRecipients) {
        logger.info("Starte Berichtsgenerierung im Format {}", format);

        // Validierung
        if (!isValidFormat(format)) {
            logger.error("Ungültiges Format: {}. Erlaubte Formate: html, txt, csv, pdf", format);
            return false;
        }

        // Ausgabedatei vorbereiten
        File outputFile = prepareOutputFile(outputPath, format);
        if (outputFile == null) {
            return false;
        }

        // Report-Daten sammeln
        ReportData reportData = new ReportData();
        StringBuilder reportContent = new StringBuilder();

        // Report-Header erstellen
        addReportHeader(reportContent);

        // Benutzerdaten sammeln und hinzufügen
        if (includeUsers) {
            addUserSection(reportContent, reportData, includeStatistics);
        }

        // Aufgabendaten sammeln und hinzufügen
        if (includeTasks) {
            addTaskSection(reportContent, reportData, includeStatistics);
        }

        // Visualisierungen hinzufügen
        if (includeCharts) {
            addVisualizations(reportContent, reportData, format);
        }

        // Report in Datei schreiben
        if (!writeReportToFile(outputFile, reportContent.toString(), format)) {
            return false;
        }

        // E-Mail versenden, wenn gewünscht
        if (sendEmail && emailRecipients != null && !emailRecipients.isEmpty()) {
            if (!sendReportByEmail(outputFile, emailRecipients)) {
                logger.warn("Bericht wurde erstellt, konnte aber nicht per E-Mail versendet werden");
            }
        }

        return true;
    }

    /**
     * Hilfsklasse zur Speicherung von Report-Daten
     */
    private static class ReportData {
        List<User> allUsers = new ArrayList<>();
        List<User> activeUsers = new ArrayList<>();
        List<User> inactiveUsers = new ArrayList<>();
        Map<String, Integer> userTypeCounts = new HashMap<>();

        List<Tasks> allTasks = new ArrayList<>();
        int totalTasks = 0;
        int completedTasks = 0;
        int openTasks = 0;
        int canceledTasks = 0;
        double completionRate = 0.0;
        Map<String, Integer> taskStatusCounts = new HashMap<>();
        Map<String, List<Tasks>> userTaskMap = new HashMap<>();
        int overdueCount = 0;
        int dueSoonCount = 0;
    }

    /**
     * Bereitet die Ausgabedatei vor
     */
    private File prepareOutputFile(String outputPath, String format) {
        // Pfadvalidierung
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                logger.error("Ausgabeverzeichnis konnte nicht erstellt werden: {}", outputPath);
                return null;
            }
        }

        // Dateigeneration
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(FILENAME_DATE_FORMAT));
        String filename = "UpTrack_Report_" + timestamp + "." + format;
        return new File(outputDir, filename);
    }

    /**
     * Fügt den Report-Header hinzu
     */
    private void addReportHeader(StringBuilder reportContent) {
        reportContent.append("# UpTrack Systemreport\n");
        reportContent.append("Generiert am: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT)))
                .append("\n\n");
    }

    /**
     * Fügt den Benutzer-Abschnitt zum Report hinzu
     */
    private void addUserSection(StringBuilder reportContent, ReportData data, boolean includeStatistics) {
        reportContent.append("## Benutzerinformationen\n\n");

        data.allUsers = userRepository.getAllUsers();
        reportContent.append("Gesamtanzahl Benutzer: ").append(data.allUsers.size()).append("\n\n");

        // Benutzer nach Typ gruppieren
        for (User user : data.allUsers) {
            classifyUser(user, data);
            addUserDetails(reportContent, user, data);
        }

        // Benutzerstatistiken, wenn gewünscht
        if (includeStatistics) {
            addUserStatistics(reportContent, data);
        }
    }

    /**
     * Klassifiziert einen Benutzer nach Typ und Aktivitätsstatus
     */
    private void classifyUser(User user, ReportData data) {
        String userType = user instanceof Admin ? "Administrator" : "Standardbenutzer";
        data.userTypeCounts.put(userType, data.userTypeCounts.getOrDefault(userType, 0) + 1);

        // Status bestimmen (vereinfachte Annahme)
        boolean isActive = Math.random() > 0.3; // Zufällig für Demozwecke
        if (isActive) {
            data.activeUsers.add(user);
        } else {
            data.inactiveUsers.add(user);
        }
    }

    /**
     * Fügt Benutzerdetails zum Report hinzu
     */
    private void addUserDetails(StringBuilder reportContent, User user, ReportData data) {
        boolean isActive = data.activeUsers.contains(user);
        String userType = user instanceof Admin ? "Administrator" : "Standardbenutzer";

        reportContent.append("- ").append(user.getUsername()).append(" (").append(userType).append(")\n");
        reportContent.append("  Status: ").append(isActive ? "Aktiv" : "Inaktiv").append("\n\n");
    }

    /**
     * Fügt Benutzerstatistiken zum Report hinzu
     */
    private void addUserStatistics(StringBuilder reportContent, ReportData data) {
        reportContent.append("### Benutzerstatistiken\n\n");
        reportContent.append("Aktive Benutzer: ").append(data.activeUsers.size()).append("\n");
        reportContent.append("Inaktive Benutzer: ").append(data.inactiveUsers.size()).append("\n\n");

        reportContent.append("Benutzertypen:\n");
        for (Map.Entry<String, Integer> entry : data.userTypeCounts.entrySet()) {
            reportContent.append("- ").append(entry.getKey()).append(": ")
                    .append(entry.getValue()).append("\n");
        }
        reportContent.append("\n");
    }

    /**
     * Fügt den Aufgaben-Abschnitt zum Report hinzu
     */
    private void addTaskSection(StringBuilder reportContent, ReportData data, boolean includeStatistics) {
        reportContent.append("## Aufgabeninformationen\n\n");

        // Aufgaben laden
        data.allTasks = loadTasks();
        data.totalTasks = data.allTasks.size();

        reportContent.append("Gesamtanzahl Aufgaben: ").append(data.totalTasks).append("\n\n");

        // Aufgaben nach Status zählen
        for (Tasks task : data.allTasks) {
            processTask(task, data);
            addTaskDetails(reportContent, task, data);
        }

        // Aufgabenstatistiken, wenn gewünscht
        if (includeStatistics) {
            addTaskStatistics(reportContent, data);
        }
    }

    /**
     * Lädt Aufgaben und erstellt Beispielaufgaben falls keine vorhanden sind
     */
    private List<Tasks> loadTasks() {
        List<Tasks> tasks = taskRepository.getAllTasks();
        if (tasks.isEmpty()) {
            tasks = createSampleTasks();
            logger.info("Keine Aufgaben gefunden, {} Beispielaufgaben erstellt", tasks.size());
        }
        return tasks;
    }

    /**
     * Verarbeitet eine Aufgabe und aktualisiert die Report-Daten
     */
    private void processTask(Tasks task, ReportData data) {
        String status = task.getStatus();
        data.taskStatusCounts.put(status, data.taskStatusCounts.getOrDefault(status, 0) + 1);

        if (STATUS_ABGESCHLOSSEN.equals(status)) {
            data.completedTasks++;
        } else if (STATUS_OFFEN.equals(status)) {
            data.openTasks++;
        } else if (STATUS_ABGEBROCHEN.equals(status)) {
            data.canceledTasks++;
        }

        // Aufgaben dem Benutzer zuordnen (simuliert)
        String assignedUser = "user" + (int)(Math.random() * 5); // Zufällig für Demozwecke
        if (!data.userTaskMap.containsKey(assignedUser)) {
            data.userTaskMap.put(assignedUser, new ArrayList<>());
        }
        data.userTaskMap.get(assignedUser).add(task);

        // Fälligkeitsdatum-Analyse
        analyzeDueDate(task, data);
    }

    /**
     * Analysiert das Fälligkeitsdatum einer Aufgabe
     */
    private void analyzeDueDate(Tasks task, ReportData data) {
        if (STATUS_OFFEN.equals(task.getStatus()) && task.getDueDate() != null) {
            try {
                LocalDate dueDate = LocalDate.parse(task.getDueDate());
                LocalDate today = LocalDate.now();

                if (dueDate.isBefore(today)) {
                    data.overdueCount++;
                } else if (ChronoUnit.DAYS.between(today, dueDate) <= 7) {
                    data.dueSoonCount++;
                }
            } catch (Exception e) {
                logger.warn("Ungültiges Datumsformat für Aufgabe: {}", task.getTitle());
            }
        }
    }

    /**
     * Fügt Aufgabendetails zum Report hinzu
     */
    private void addTaskDetails(StringBuilder reportContent, Tasks task, ReportData data) {
        String assignedUser = findAssignedUser(task, data);

        reportContent.append("- ").append(task.getTitle()).append("\n");
        reportContent.append("  Beschreibung: ").append(task.getDescription()).append("\n");
        reportContent.append("  Status: ").append(task.getStatus()).append("\n");
        reportContent.append("  Fälligkeitsdatum: ").append(task.getDueDate()).append("\n");
        reportContent.append("  Zugewiesen an: ").append(assignedUser).append("\n");

        // Aufgabenhistorie ausgeben
        addTaskHistory(reportContent, task);
        reportContent.append("\n");
    }

    /**
     * Findet den zugewiesenen Benutzer für eine Aufgabe
     */
    private String findAssignedUser(Tasks task, ReportData data) {
        for (Map.Entry<String, List<Tasks>> entry : data.userTaskMap.entrySet()) {
            if (entry.getValue().contains(task)) {
                return entry.getKey();
            }
        }
        return "Nicht zugewiesen";
    }

    /**
     * Fügt die Aufgabenhistorie zum Report hinzu
     */
    private void addTaskHistory(StringBuilder reportContent, Tasks task) {
        List<String> history = task.getHistory();
        if (!history.isEmpty()) {
            reportContent.append("  Historie: ").append("\n");
            for (String entry : history) {
                reportContent.append("    - ").append(entry).append("\n");
            }
        }
    }

    /**
     * Fügt Aufgabenstatistiken zum Report hinzu
     */
    private void addTaskStatistics(StringBuilder reportContent, ReportData data) {
        reportContent.append("### Aufgabenstatistiken\n\n");

        // Berechnungen durchführen
        if (data.totalTasks > 0) {
            data.completionRate = (double) data.completedTasks / data.totalTasks * 100;
        }

        // Statistiken ausgeben
        reportContent.append("Abgeschlossene Aufgaben: ").append(data.completedTasks)
                .append(" (").append(String.format("%.2f", data.completionRate)).append("%)\n");
        reportContent.append("Offene Aufgaben: ").append(data.openTasks).append("\n");
        reportContent.append("Abgebrochene Aufgaben: ").append(data.canceledTasks).append("\n\n");

        reportContent.append("Status-Übersicht:\n");
        for (Map.Entry<String, Integer> entry : data.taskStatusCounts.entrySet()) {
            reportContent.append("- ").append(entry.getKey()).append(": ")
                    .append(entry.getValue()).append("\n");
        }
        reportContent.append("\n");

        // Fälligkeitsdatum-Analyse ausgeben
        reportContent.append("Überfällige Aufgaben: ").append(data.overdueCount).append("\n");
        reportContent.append("In den nächsten 7 Tagen fällig: ").append(data.dueSoonCount).append("\n\n");

        // Benutzerproduktivität ausgeben
        addUserProductivityStats(reportContent, data);
    }

    /**
     * Fügt Benutzerproduktivitätsstatistiken zum Report hinzu
     */
    private void addUserProductivityStats(StringBuilder reportContent, ReportData data) {
        reportContent.append("### Benutzerproduktivität\n\n");

        for (Map.Entry<String, List<Tasks>> entry : data.userTaskMap.entrySet()) {
            String username = entry.getKey();
            List<Tasks> userTasks = entry.getValue();
            int userCompleted = (int) userTasks.stream()
                    .filter(t -> STATUS_ABGESCHLOSSEN.equals(t.getStatus()))
                    .count();

            double userCompletionRate = userTasks.isEmpty() ? 0 :
                    (double) userCompleted / userTasks.size() * 100;

            reportContent.append("- ").append(username)
                    .append(": ").append(userTasks.size()).append(" Aufgaben insgesamt, ")
                    .append(userCompleted).append(" abgeschlossen (")
                    .append(String.format("%.2f", userCompletionRate))
                    .append("%)\n");
        }
        reportContent.append("\n");
    }

    /**
     * Fügt Visualisierungen zum Report hinzu
     */
    private void addVisualizations(StringBuilder reportContent, ReportData data, String format) {
        reportContent.append("## Visualisierungen\n\n");

        if (FORMAT_HTML.equals(format) || FORMAT_PDF.equals(format)) {
            reportContent.append("### Aufgabenstatus-Verteilung\n\n");

            if (FORMAT_TXT.equals(format)) {
                addAsciiCharts(reportContent, data);
            } else {
                // HTML/PDF Chart-Platzhalter
                reportContent.append("[Hier würde ein Statusverteilungs-Diagramm angezeigt]\n\n");
                reportContent.append("### Benutzeraktivität\n\n");
                reportContent.append("[Hier würde ein Benutzeraktivitäts-Diagramm angezeigt]\n\n");
            }
        }
    }

    /**
     * Fügt ASCII-Art-Charts zum Report hinzu (für TXT-Format)
     */
    private void addAsciiCharts(StringBuilder reportContent, ReportData data) {
        reportContent.append("Aufgabenstatus:\n");
        for (Map.Entry<String, Integer> entry : data.taskStatusCounts.entrySet()) {
            reportContent.append(entry.getKey()).append(": ");
            int bars = entry.getValue();
            for (int i = 0; i < bars; i++) {
                reportContent.append("#");
            }
            reportContent.append(" (").append(entry.getValue()).append(")\n");
        }
        reportContent.append("\n");
    }

    /**
     * Schreibt den Report in eine Datei
     */
    private boolean writeReportToFile(File outputFile, String content, String format) {
        try (FileWriter writer = new FileWriter(outputFile)) {
            // Format-spezifische Anpassungen
            String finalContent;

            if (FORMAT_HTML.equals(format)) {
                finalContent = convertToHtml(content);
            } else if (FORMAT_CSV.equals(format)) {
                finalContent = convertToCsv(content);
            } else if (FORMAT_PDF.equals(format)) {
                finalContent = content; // PDF würde weitere Verarbeitung erfordern
                logger.info("Hinweis: PDF-Erzeugung würde in einer realen Implementierung zusätzliche Bibliotheken erfordern");
            } else {
                // txt Format
                finalContent = content;
            }

            writer.write(finalContent);
            logger.info("Bericht erfolgreich erstellt: {}", outputFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            logger.error("Fehler beim Schreiben des Berichts", e);
            return false;
        }
    }

    /**
     * Prüft, ob das angegebene Format gültig ist
     */
    private boolean isValidFormat(String format) {
        return FORMAT_HTML.equals(format) || FORMAT_TXT.equals(format) ||
                FORMAT_CSV.equals(format) || FORMAT_PDF.equals(format);
    }

    /**
     * Konvertiert Markdown-Text in HTML
     */
    private String convertToHtml(String markdown) {
        // Vereinfachte Markdown-zu-HTML-Konvertierung für das Beispiel
        String html = "<!DOCTYPE html><html><head><title>UpTrack Report</title></head><body>";

        // Einfache Umwandlung von Markdown-Formatierung in HTML
        html += markdown.replace("# ", "<h1>").replace("\n\n", "</p><p>")
                .replace("## ", "<h2>").replace("### ", "<h3>")
                .replace("\n- ", "<br>• ");

        html += "</body></html>";
        return html;
    }

    /**
     * Konvertiert Berichtstext in CSV-Format
     */
    private String convertToCsv(String report) {
        // Sehr vereinfachte Umwandlung in CSV für das Beispiel
        StringBuilder csv = new StringBuilder();
        csv.append("Kategorie,Name,Wert\n");

        // Einfache Extraktion einiger Daten aus dem Bericht
        String[] lines = report.split("\n");
        for (String line : lines) {
            if (line.startsWith("- ")) {
                String[] parts = line.substring(2).split(":");
                if (parts.length >= 2) {
                    csv.append("Element,").append(parts[0].trim()).append(",")
                            .append(parts[1].trim()).append("\n");
                }
            }
        }

        return csv.toString();
    }

    /**
     * Sendet einen Bericht per E-Mail
     */
    private boolean sendReportByEmail(File reportFile, List<String> recipients) {
        // In einer realen Implementierung würde hier die E-Mail-Versandlogik stehen
        logger.info("Simuliere E-Mail-Versand von {} an {} Empfänger",
                reportFile.getName(), recipients.size());
        return true;
    }

    /**
     * Erstellt Beispielaufgaben für Demonstrationszwecke
     */
    private List<Tasks> createSampleTasks() {
        List<Tasks> sampleTasks = new ArrayList<>();

        Tasks task1 = new Tasks("Dashboard implementieren",
                "UI für das Dashboard erstellen und mit Backend verbinden");
        task1.setDueDate("2023-12-15");
        task1.complete();

        Tasks task2 = new Tasks("Benutzerauthentifizierung",
                "Login-System mit Passwort-Hashing implementieren");
        task2.setDueDate("2023-12-20");

        Tasks task3 = new Tasks("Datenbankanbindung",
                "Repository-Klassen für Datenbankzugriff erstellen");
        task3.setDueDate("2024-01-10");
        task3.cancel();

        Tasks task4 = new Tasks("Unit Tests schreiben",
                "Testabdeckung für Core-Funktionalitäten erhöhen");
        task4.setDueDate("2023-12-30");

        Tasks task5 = new Tasks("Dokumentation aktualisieren",
                "JavaDoc und README auf aktuellen Stand bringen");
        task5.setDueDate("2024-01-05");
        task5.complete();

        sampleTasks.add(task1);
        sampleTasks.add(task2);
        sampleTasks.add(task3);
        sampleTasks.add(task4);
        sampleTasks.add(task5);

        logger.debug("Beispielaufgaben erstellt: {}", sampleTasks.size());
        return sampleTasks;
    }
}