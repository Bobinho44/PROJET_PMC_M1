package tl2.commons;

import tl2.api.IRegister;
import tl2.api.ITransaction;
import tl2.api.exception.AbortException;

import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unchecked")
public class Register<T> implements IRegister<T> {

    /**
     * Fields
     */
    private final UUID uuid;
    private T value;
    private Integer date = 0;
    private final Lock lock = new ReentrantLock();

    /**
     * Creates a new register
     *
     * @param value the initial value
     */
    public Register(T value) {
        this.uuid = UUID.randomUUID();
        this.value = value;
    }

    /**
     * Creates a new register
     *
     * @param register the register to be copied
     */
    private Register(Register<T> register) {
        this.uuid = register.getUUID();
        this.value = register.getValue();
        this.date = register.getDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Register<T> copy() {
        return new Register<>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID getUUID() {
        return uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValue(Object value) {
        this.value = (T) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getDate() {
        return date;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDate(Integer date) {
        this.date = date;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unlock() {
        lock.unlock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T read(ITransaction transaction) throws AbortException {
        if (transaction.hasLocalCopy(this)) {
            Register<T> localCopy = transaction.getLocalCopy(this);

            return localCopy.getValue();
        } else {
            transaction.createLocalCopy(this);
            transaction.addReadRegister(this);

            Register<T> localCopy = transaction.getLocalCopy(this);

            if (localCopy.getDate() > transaction.getBirthDate()) {
                throw new AbortException();
            } else {
                return localCopy.getValue();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(ITransaction transaction, T value) {
        if (!transaction.hasLocalCopy(this)) {
            transaction.createLocalCopy(this);
        }

        transaction.addWriteRegister(this);

        Register<T> localCopy = transaction.getLocalCopy(this);
        localCopy.setValue(value);
    }

}