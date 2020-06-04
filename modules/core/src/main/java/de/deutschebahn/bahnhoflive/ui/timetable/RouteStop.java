package de.deutschebahn.bahnhoflive.ui.timetable;

public class RouteStop {
    public final String name;
    private boolean first = false, last = false, current;

    public RouteStop(String name) {
        this(name, false);
    }

    public RouteStop(String name, boolean current) {
        this.name = name;

        this.current = current;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}