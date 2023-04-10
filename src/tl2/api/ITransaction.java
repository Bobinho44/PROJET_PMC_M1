package tl2.api;

import tl2.api.exception.AbortException;
import tl2.commons.Register;

public interface ITransaction {

    /**
     * Adds this register to the registers read during the transaction
     *
     * @param register the register
     */
    void addReadRegister(IRegister<?> register);

    /**
     * Adds this register to the registers write during the transaction
     *
     * @param register the register
     */
    void addWriteRegister(IRegister<?> register);

    /**
     * Checks if the transaction has a local copy of the register
     *
     * @param register the register
     * @return true if the transaction has a local copy of the register, false otherwise
     */
    boolean hasLocalCopy(IRegister<?> register);

    /**
     * Gets the local copy of the register from the transaction
     *
     * @param register the register
     * @return the local copy of the register from the transaction
     */
    <T> Register<T> getLocalCopy(IRegister<T> register);

    /**
     * Creates a local copy of the register to the transaction
     *
     * @param register the register
     */
    void createLocalCopy(IRegister<?> register);

    /**
     * Gets the birthdate of the transaction
     *
     * @return the birthdate of the transaction
     */
    Integer getBirthDate();

    /**
     * Checks if the transaction has been committed
     *
     * @return true if the transaction has been committed
     */
    boolean isCommited();

    /**
     * Begins the transaction
     */
    void begin();

    /**
     * Tries to commit the transaction
     *
     * @throws AbortException if registers read during the transaction have already been modified by another transaction
     */
    void try_to_commit() throws AbortException;

}