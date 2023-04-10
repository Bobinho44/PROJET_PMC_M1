package tl2;

import tl2.api.IRegister;
import tl2.api.ITransaction;
import tl2.api.exception.AbortException;
import tl2.commons.Register;
import tl2.commons.Transaction;

public class Main {
    public static void main(String[] args) {
        IRegister<Integer> X = new Register<>(12);
        IRegister<Integer> Y = new Register<>(4);

        System.out.println(X.getValue() + " " + Y.getValue());

        swipe(X, Y);

        System.out.println(X.getValue() + " " + Y.getValue());
    }

    public static void swipe(IRegister<Integer> X, IRegister<Integer> Y) {
        ITransaction transaction = new Transaction();
        while (!transaction.isCommited()) {
            try {
                transaction.begin();
                Integer x = X.read(transaction);
                X.write(transaction, Y.read(transaction));
                Y.write(transaction, x);
                transaction.try_to_commit();
            } catch (AbortException ignored) {}
        }
    }

}