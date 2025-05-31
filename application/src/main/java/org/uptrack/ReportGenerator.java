package org.uptrack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

    // Konstanten für Status und Formate
    private static final String STATUS_OFFEN = "Offen";
    private static final String STATUS_ABGESCHLOSSEN = "Abgeschlossen";
    private static final String STATUS_ABGEBROCHEN = "Abgebrochen";

    private static final String FORMAT_HTML = "html";
    private static final String FORMAT_TXT = "txt";
    private static final String FORMAT_CSV = "csv";
    private static final String FORMAT_PDF = "pdf";

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ReportGenerator(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }


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


    public boolean generateComprehensiveReport(String format, boolean includeTasks, boolean includeUsers,
                                               boolean includeStatistics, boolean includeCharts,
                                               String outputPath, boolean sendEmail, List<String> emailRecipients) {
        logger.info("Starte Berichtsgenerierung im Format {}", format);

        // Formatvalidierung
        if (!isValidFormat(format)) {
            logger.error("Ungültiges Format: {}. Erlaubte Formate: html, txt, csv, pdf", format);
            return false;
        }

        // Pfadvalidierung
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                logger.error("Ausgabeverzeichnis konnte nicht erstellt werden: {}", outputPath);
                return false;
            }
        }

        // Dateigeneration
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "UpTrack_Report_" + timestamp + "." + format;
        File outputFile = new File(outputDir, filename);

        // Reportvariablen initialisieren
        StringBuilder reportContent = new StringBuilder();
        Map<String, Integer> taskStatusCounts = new HashMap<>();
        Map<String, Integer> userTypeCounts = new HashMap<>();
        int totalTasks = 0;
        int completedTasks = 0;
        int openTasks = 0;
        int canceledTasks = 0;
        double completionRate = 0.0;
        List<User> activeUsers = new ArrayList<>();
        List<User> inactiveUsers = new ArrayList<>();
        Map<String, List<Tasks>> userTaskMap = new HashMap<>();

        // Berichtsheader erstellen
        reportContent.append("# UpTrack Systemreport\n");
        reportContent.append("Generiert am: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))).append("\n\n");

        // Benutzerdaten sammeln und verarbeiten, wenn gewünscht
        if (includeUsers) {
            reportContent.append("## Benutzerinformationen\n\n");

            List<User> allUsers = userRepository.getAllUsers();
            reportContent.append("Gesamtanzahl Benutzer: ").append(allUsers.size()).append("\n\n");

            // Benutzer nach Typ gruppieren
            for (User user : allUsers) {
                String userType = user instanceof Admin ? "Administrator" : "Standardbenutzer";
                userTypeCounts.put(userType, userTypeCounts.getOrDefault(userType, 0) + 1);

                // Status bestimmen (vereinfachte Annahme)
                boolean isActive = Math.random() > 0.3; // Zufällig für Demozwecke
                if (isActive) {
                    activeUsers.add(user);
                } else {
                    inactiveUsers.add(user);
                }

                // Benutzerdetails ausgeben
                reportContent.append("- ").append(user.getUsername()).append(" (").append(userType).append(")\n");
                reportContent.append("  Status: ").append(isActive ? "Aktiv" : "Inaktiv").append("\n");

                // Weitere Benutzerdetails könnten hier hinzugefügt werden
                reportContent.append("\n");
            }

            // Benutzerstatistiken, wenn gewünscht
            if (includeStatistics) {
                reportContent.append("### Benutzerstatistiken\n\n");
                reportContent.append("Aktive Benutzer: ").append(activeUsers.size()).append("\n");
                reportContent.append("Inaktive Benutzer: ").append(inactiveUsers.size()).append("\n");

                for (Map.Entry<String, Integer> entry : userTypeCounts.entrySet()) {
                    reportContent.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
                reportContent.append("\n");
            }
        }

        // Aufgabendaten sammeln und verarbeiten, wenn gewünscht
        if (includeTasks) {
            reportContent.append("## Aufgabeninformationen\n\n");

            List<Tasks> allTasks = taskRepository.getAllTasks();

            if (allTasks.isEmpty()) {
                // Testdaten erstellen, falls keine Aufgaben vorhanden sind
                allTasks = createSampleTasks();
            }

            totalTasks = allTasks.size();
            reportContent.append("Gesamtanzahl Aufgaben: ").append(totalTasks).append("\n\n");

            // Aufgaben nach Status zählen
            for (Tasks task : allTasks) {
                String status = task.getStatus();
                taskStatusCounts.put(status, taskStatusCounts.getOrDefault(status, 0) + 1);

                if (status.equals(STATUS_ABGESCHLOSSEN)) {
                    completedTasks++;
                } else if (status.equals(STATUS_OFFEN)) {
                    openTasks++;
                } else if (status.equals(STATUS_ABGEBROCHEN)) {
                    canceledTasks++;
                }

                // Aufgaben dem Benutzer zuordnen (simuliert)
                String assignedUser = "user" + (int)(Math.random() * 5); // Zufällig für Demozwecke
                if (!userTaskMap.containsKey(assignedUser)) {
                    userTaskMap.put(assignedUser, new ArrayList<>());
                }
                userTaskMap.get(assignedUser).add(task);

                // Aufgabendetails ausgeben
                reportContent.append("- ").append(task.getTitle()).append("\n");
                reportContent.append("  Beschreibung: ").append(task.getDescription()).append("\n");
                reportContent.append("  Status: ").append(task.getStatus()).append("\n");
                reportContent.append("  Fälligkeitsdatum: ").append(task.getDueDate()).append("\n");
                reportContent.append("  Zugewiesen an: ").append(assignedUser).append("\n");

                // Aufgabenhistorie ausgeben
                List<String> history = task.getHistory();
                if (!history.isEmpty()) {
                    reportContent.append("  Historie: ").append("\n");
                    for (String entry : history) {
                        reportContent.append("    - ").append(entry).append("\n");
                    }
                }

                reportContent.append("\n");
            }

            // Aufgabenstatistiken, wenn gewünscht
            if (includeStatistics) {
                reportContent.append("### Aufgabenstatistiken\n\n");

                if (totalTasks > 0) {
                    completionRate = (double) completedTasks / totalTasks * 100;
                }

                reportContent.append("Abgeschlossene Aufgaben: ").append(completedTasks).append(" (").append(String.format("%.2f", completionRate)).append("%)\n");
                reportContent.append("Offene Aufgaben: ").append(openTasks).append("\n");
                reportContent.append("Abgebrochene Aufgaben: ").append(canceledTasks).append("\n\n");

                reportContent.append("Status-Übersicht:\n");
                for (Map.Entry<String, Integer> entry : taskStatusCounts.entrySet()) {
                    reportContent.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }

                reportContent.append("\n");

                // Fälligkeitsdatum-Analyse (vereinfacht)
                int overdueCount = 0;
                int dueSoonCount = 0;
                LocalDate today = LocalDate.now();

                for (Tasks task : allTasks) {
                    if (task.getStatus().equals(STATUS_OFFEN) && task.getDueDate() != null) {
                        try {
                            LocalDate dueDate = LocalDate.parse(task.getDueDate());
                            if (dueDate.isBefore(today)) {
                                overdueCount++;
                            } else if (dueDate.isBefore(today.plusDays(7))) {
                                dueSoonCount++;
                            }
                        } catch (Exception e) {
                            logger.warn("Fehler beim Parsen des Datums für Aufgabe: {}", task.getTitle(), e);
                        }
                    }
                }

                reportContent.append("Überfällige Aufgaben: ").append(overdueCount).append("\n");
                reportContent.append("In den nächsten 7 Tagen fällig: ").append(dueSoonCount).append("\n\n");

                // Benutzerproduktivität
                reportContent.append("### Benutzerproduktivität\n\n");
                for (Map.Entry<String, List<Tasks>> entry : userTaskMap.entrySet()) {
                    String username = entry.getKey();
                    List<Tasks> userTasks = entry.getValue();
                    int userCompleted = (int) userTasks.stream().filter(t -> t.getStatus().equals(STATUS_ABGESCHLOSSEN)).count();

                    reportContent.append("- ").append(username).append(": ").append(userTasks.size()).append(" Aufgaben insgesamt, ")
                            .append(userCompleted).append(" abgeschlossen (")
                            .append(String.format("%.2f", userTasks.isEmpty() ? 0 : (double) userCompleted / userTasks.size() * 100))
                            .append("%)\n");
                }

                reportContent.append("\n");
            }
        }

        // Charts generieren, wenn gewünscht
        if (includeCharts) {
            reportContent.append("## Visualisierungen\n\n");

            if (format.equals(FORMAT_HTML) || format.equals(FORMAT_PDF)) {
                reportContent.append("### Aufgabenstatus-Verteilung\n\n");

                // ASCII Art Chart für Textausgabe (vereinfacht)
                if (format.equals(FORMAT_TXT)) {
                    reportContent.append("Aufgabenstatus:\n");
                    for (Map.Entry<String, Integer> entry : taskStatusCounts.entrySet()) {
                        int barLength = entry.getValue() * 2; // 2 Zeichen pro Aufgabe
                        String bar = "=".repeat(Math.max(0, barLength));
                        reportContent.append(String.format("%-15s |%s| %d\n", entry.getKey(), bar, entry.getValue()));
                    }
                } else {
                    // In einem echten System würden wir hier einen richtigen Chart generieren
                    reportContent.append("[Hier würde in einer realen Implementierung ein Statusverteilungs-Diagramm angezeigt]\n\n");
                }

                reportContent.append("### Benutzeraktivität\n\n");
                reportContent.append("[Hier würde in einer realen Implementierung ein Benutzeraktivitäts-Diagramm angezeigt]\n\n");
            }
        }

        // Report schreiben
        try (FileWriter writer = new FileWriter(outputFile)) {
            // Format-spezifische Anpassungen
            String finalContent;

            if (format.equals(FORMAT_HTML)) {
                finalContent = convertToHtml(reportContent.toString());
            } else if (format.equals(FORMAT_CSV)) {
                finalContent = convertToCsv(reportContent.toString());
            } else if (format.equals(FORMAT_PDF)) {
                finalContent = reportContent.toString(); // PDF würde weitere Verarbeitung erfordern
                logger.info("Hinweis: PDF-Erzeugung würde in einer realen Implementierung zusätzliche Bibliotheken erfordern.");
            } else {
                // txt Format
                finalContent = reportContent.toString();
            }

            writer.write(finalContent);
            logger.info("Bericht erfolgreich erstellt: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Fehler beim Schreiben des Berichts", e);
            return false;
        }

        // E-Mail versenden, wenn gewünscht
        if (sendEmail && emailRecipients != null && !emailRecipients.isEmpty()) {
            logger.info("Sende Bericht per E-Mail an: {}", String.join(", ", emailRecipients));

            // In einem echten System würden wir hier eine E-Mail-Versandlogik implementieren
            boolean emailSuccess = sendReportByEmail(outputFile, emailRecipients);

            if (!emailSuccess) {
                logger.warn("Bericht wurde erstellt, konnte aber nicht per E-Mail versendet werden.");
            }
        }

        return true;
    }

    /**
     * Prüft, ob das angegebene Format gültig ist
     */
    private boolean isValidFormat(String format) {
        return format.equals(FORMAT_HTML) || format.equals(FORMAT_TXT)
                || format.equals(FORMAT_CSV) || format.equals(FORMAT_PDF);
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
                    csv.append("Element,").append(parts[0].trim()).append(",").append(parts[1].trim()).append("\n");
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
        logger.info("Simuliere E-Mail-Versand von {} an {} Empfänger", reportFile.getName(), recipients.size());
        return true;
    }

    /**
     * Erstellt Beispielaufgaben für Demonstrationszwecke
     */
    private List<Tasks> createSampleTasks() {
        List<Tasks> sampleTasks = new ArrayList<>();

        Tasks task1 = new Tasks("Dashboard implementieren", "UI für das Dashboard erstellen und mit Backend verbinden");
        task1.setDueDate("2023-12-15");
        task1.complete();

        Tasks task2 = new Tasks("Benutzerauthentifizierung", "Login-System mit Passwort-Hashing implementieren");
        task2.setDueDate("2023-12-20");

        Tasks task3 = new Tasks("Datenbankanbindung", "Repository-Klassen für Datenbankzugriff erstellen");
        task3.setDueDate("2024-01-10");
        task3.cancel();

        Tasks task4 = new Tasks("Unit Tests schreiben", "Testabdeckung für Core-Funktionalitäten erhöhen");
        task4.setDueDate("2023-12-30");

        Tasks task5 = new Tasks("Dokumentation aktualisieren", "JavaDoc und README auf aktuellen Stand bringen");
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