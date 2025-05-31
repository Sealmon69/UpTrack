package org.uptrack;

import java.util.ArrayList;
import java.util.List;

public class RecordHistory {
    private List<String> history;

    public RecordHistory() {
        this.history = new ArrayList<>();
    }

    public void addEntry(String entry) {
        history.add(entry);
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    public void clear() {
        history.clear();
    }

    public int getEntryCount() {
        return history.size();
    }
}