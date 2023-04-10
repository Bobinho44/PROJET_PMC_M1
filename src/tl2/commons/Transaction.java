package tl2.commons;

import tl2.api.IRegister;
import tl2.api.ITransaction;
import tl2.api.exception.AbortException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unchecked")
public class Transaction implements ITransaction {

    /**
     * Constants
     */
    public static AtomicInteger CLOCK = new AtomicInteger(0);

    /**
     * Fields
     */
    private final Set<IRegister<?>> lrs = new HashSet<>();
    private final Set<IRegister<?>> lws = new HashSet<>();
    private final Map<UUID, IRegister<?>> lcx = new HashMap<>();
    private int birthDate;
    private boolean isCommitted;

    /**
     * {@inheritDoc}
     */
    @Override
    public void addReadRegister(IRegister<?> register) {
        lrs.add(register);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWriteRegister(IRegister<?> register) {
        lws.add(register);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasLocalCopy(IRegister<?> register) {
        return lcx.containsKey(register.getUUID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Register<T> getLocalCopy(IRegister<T> register) {
        return (Register<T>) Optional.ofNullable(lcx.get(register.getUUID())).orElseThrow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createLocalCopy(IRegister<?> register) {
        lcx.put(register.getUUID(), register.copy());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getBirthDate() {
        return birthDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCommited() {
        return isCommitted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void begin() {
        lrs.clear();
        lws.clear();
        lcx.clear();

        this.birthDate = CLOCK.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void try_to_commit() throws AbortException {
        if (!tryLockAll()) {
            throw new AbortException();
        }

        for (IRegister<?> register : lrs) {
            if (register.getDate() > this.birthDate) {
                unlockAll();
                throw new AbortException();
            }
        }

        int commitDate = CLOCK.getAndIncrement();

        for (IRegister<?> register : lws) {
            register.setValue(getLocalCopy(register).getValue());
            register.setDate(commitDate);
        }

        isCommitted = true;
        unlockAll();
    }

    /**
     * Tries to lock add write registers
     *
     * @return true if all write register have been locked, false otherwise
     */
    private boolean tryLockAll() {
        for (IRegister<?> register : lws) {
            if (!register.tryLock()) {
                unlockAll();
                return false;
            }
        }

        return true;
    }

    /**
     * Unlocks all write registers
     */
    private void unlockAll() {
        for (IRegister<?> register : lws) {
            register.unlock();
        }
    }

}