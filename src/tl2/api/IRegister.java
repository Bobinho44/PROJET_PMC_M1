package tl2.api;

import tl2.api.exception.AbortException;

import java.util.UUID;

public interface IRegister<T> {

    /**
     * Creates a copy of the register
     *
     * @return the copy of the register
     */
    IRegister<T> copy();

    /**
     * Gets the uuid of the register
     *
     * @return the uuid of the register
     */
    UUID getUUID();

    /**
     * Gets the value of the register
     *
     * @return the value of the register
     */
    T getValue();

    /**
     * Sets the value of the register
     *
     * @param value the value
     */
    void setValue(Object value);

    /**
     * Gets the date of the register
     *
     * @return the date of the register
     */
    Integer getDate();

    /**
     * Sets the date of the register
     *
     * @param date the date
     */
    void setDate(Integer date);

    /**
     * Tries to lock the register
     *
     * @return true if the register has been locked, false otherwise
     */
    boolean tryLock();

    /**
     * Unlocks the register
     */
    void unlock();

    /**
     * Reads in the register
     *
     * @param transaction the transaction
     * @return the read value
     * @throws AbortException if the register have already been modified by another transaction
     */
    T read(ITransaction transaction) throws AbortException;

    /**
     * Write in the register
     *
     * @param transaction the transaction
     * @param value       the value
     */
    void write(ITransaction transaction, T value);

}