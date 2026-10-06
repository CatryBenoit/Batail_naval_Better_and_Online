package boardifier.model;

import java.util.Queue;

/*
 * NB: synchronized because events are added by the ActionPlayer threads (AI, actions)
 * while the JavaFX thread reads and removes them in Controller.update()
 */
public class EventQueue {

    private Event[] queue;
    private int size;

    public EventQueue() {
        queue = new Event[1000];
        size = 0;
    }

    public synchronized Event[] getQueue() {
        return queue;
    }

    public synchronized int getSize() {
        return size;
    }

    public synchronized Event getEvent(int index) {
        if ((index < 0) || (index >= size)) return null;
        return queue[index];
    }

    public synchronized void addChangeLocationEvent() {
        queue[size++] = new Event(Event.EventType.LOCATION);
    }

    public synchronized void addChangeVisibilityEvent() {
        queue[size++] = new Event(Event.EventType.VISIBILITY);
    }

    public synchronized void addChangeSelectionEvent() {
        queue[size++] = new Event(Event.EventType.SELECTION);
    }

    public synchronized void addChangeFaceEvent() {
        queue[size++] = new Event(Event.EventType.FACE);
    }

    public synchronized void addPutInContainerEvent(ContainerElement container, int row, int col) {
        Event e = new Event(Event.EventType.IN_CONTAINER);
        e.addParameter(container);
        e.addParameter(row);
        e.addParameter(col);
        queue[size++] = e;
    }

    public synchronized void addRemoveFromContainerEvent(ContainerElement container, int row, int col) {
        Event e = new Event(Event.EventType.OUT_CONTAINER);
        e.addParameter(container);
        e.addParameter(row);
        e.addParameter(col);
        queue[size++] = e;
    }

    public synchronized void addMoveInContainerEvent(int rowSrc, int colSrc, int rowDest, int colDest) {
        Event e = new Event(Event.EventType.MOVE_CONTAINER);
        e.addParameter(rowSrc);
        e.addParameter(colSrc);
        e.addParameter(rowDest);
        e.addParameter(colDest);
        queue[size++] = e;
    }

    public synchronized Event removeEvent(int index) {
        if ((index <0) || (index >= size)) return null;
        Event e = queue[index];
        for (int i = index; i < size - 1; i++) {
            queue[i] = queue[i + 1];
        }
        queue[size-1] = null;
        size--;
        return e;
    }

    public synchronized void clear() {
        for(int i=0;i<size;i++) queue[i] = null;
        size = 0;
    }

    public synchronized boolean isChangeFaceEvent() {
        for(Event e : queue) {
            if (e.isFaceEvent()) return true;
        }
        return false;
    }

    public synchronized boolean isChangeVisibilityEvent() {
        for(Event e : queue) {
            if (e.isVisibilityEvent()) return true;
        }
        return false;
    }
    public synchronized boolean isChangeSelectionEvent() {
        for(Event e : queue) {
            if (e.isSelectionEvent()) return true;
        }
        return false;
    }
    public synchronized boolean isChangeLocationEvent() {
        for(Event e : queue) {
            if (e.isLocationEvent()) return true;
        }
        return false;
    }
    public synchronized boolean isPutInContainerEvent() {
        for(Event e : queue) {
            if (e.isInContainerEvent()) return true;
        }
        return false;
    }
    public synchronized boolean isRemoveFromContainerEvent() {
        for(Event e : queue) {
            if (e.isOutContainerEvent()) return true;
        }
        return false;
    }
    public synchronized boolean isMoveInContainerEvent() {
        for(Event e : queue) {
            if (e.isMoveInContainerEvent()) return true;
        }
        return false;
    }
}
