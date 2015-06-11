package org.objectscape.wilco.util;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * Created by plohmann on 13.05.2015.
 */
public class ClosedOnceGuard {

    private final static boolean OPEN_MARK = false;
    private final static boolean CLOSED_MARK = true;

    public enum Mark {
        OPEN, CLOSED
    }

    final private AtomicMarkableReference<Thread> closeGuard = new AtomicMarkableReference(null, OPEN_MARK);

    public boolean close() {

        boolean wasAlreadyClosed = true;

        while(!closeGuard.isMarked()) {
            wasAlreadyClosed =! closeGuard.attemptMark(null, CLOSED_MARK);
        }

        if(wasAlreadyClosed) {
            return false;
        }

        try {
            if(!lock(CLOSED_MARK)) {
                return false;
            }
        }
        finally {
            unlock();
            return true;
        }
    }

    public boolean closeAndRun(Runnable runnable) {

        boolean wasAlreadyClosed = true;

        while(!closeGuard.isMarked()) {
            wasAlreadyClosed =! closeGuard.attemptMark(null, CLOSED_MARK);
        }

        if(wasAlreadyClosed) {
            return false;
        }

        try {
            lock(CLOSED_MARK);
            runnable.run();
        }
        catch(RuntimeException e) {
            throw e;
        }
        finally {
            unlock();
            return true;
        }
    }

    public boolean runIfOpen(Runnable runnable) {
        try {
            if(!lock(OPEN_MARK)) {
                return false;
            }
            runnable.run();
            return true;
        }
        catch(RuntimeException e) {
            throw e;
        }
        finally {
            unlock();
        }
    }

    public boolean run(Mark mark, Runnable runnable) {
        try {
            boolean internalMark = (mark.equals(Mark.OPEN)) ? OPEN_MARK : CLOSED_MARK;
            if(!lock(internalMark)) {
                return false;
            }
            runnable.run();
            return true;
        }
        catch(RuntimeException e) {
            throw e;
        }
        finally {
            unlock();
        }
    }

    public boolean runIfOpenOrClosed(Runnable runnable) {
        try {
            lock();
            runnable.run();
            return true;
        }
        catch(RuntimeException e) {
            throw e;
        }
        finally {
            unlock();
        }
    }

    private void lock() {
        Thread currentThread = Thread.currentThread();
        while(true) {
            boolean currentMark = closeGuard.isMarked() ? CLOSED_MARK : OPEN_MARK;
            if(closeGuard.compareAndSet(null, currentThread, currentMark, currentMark)) {
                break;
            }
        }
    }

    public boolean runIfClosed(Runnable runnable) {
        if(!isClosed()) {
            return false;
        }
        try {
            if(!lock(CLOSED_MARK)) {
                return false;
            }
            runnable.run();
            return true;
        }
        catch(RuntimeException e) {
            throw e;
        }
        finally {
            unlock();
        }
    }

    public boolean isClosed() {
        return closeGuard.isMarked();
    }

    public boolean isOpen() {
        return !isClosed();
    }

    private boolean lock(boolean expectedAndNewMark) {
        Thread currentThread = Thread.currentThread();
        while(!closeGuard.compareAndSet(null, currentThread, expectedAndNewMark, expectedAndNewMark)) {
            if(closeGuard.isMarked()) {
                return false;
            } else {
                // other thread won
            }
        }
        return true;
    }

    private void unlock() {
        // leave critical section
        if(closeGuard.isMarked()) {
            closeGuard.set(null, CLOSED_MARK);
        } else {
            closeGuard.set(null, OPEN_MARK);
        }
    }

}
