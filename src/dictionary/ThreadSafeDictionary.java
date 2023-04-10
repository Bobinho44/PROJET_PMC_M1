package dictionary;

import tl2.api.ITransaction;
import tl2.api.exception.AbortException;
import tl2.commons.Register;
import tl2.commons.Transaction;

public class ThreadSafeDictionary {

    /**
     * A node of the dictionary data structure, representing one character.
     * As a dictionary is a tree, a node can be only accessed by following one path from the root.
     * The succession of the characters encoded by the nodes in the path leading to a node, including the node itself, forms a string,
     * that is considered present in the set if, and only if, the member "present" is set to true.
     * <p>
     * More formally, the path leading to a node is defined as such:
     *   - the path leading to the first node is path(start) = "\0";
     *   - if path(n) = s + n.character, then path(n.suffix) = s + n.character + n.suffix.character
     *   - if path(n) = s + n.character, then path(n.next) = s + n.suffix.character
     * <p>
     * A word s is contained in the dictionary if there is a node n whose path is s
     */
    static class Node {
        // The character of the string encoded in this node of the dictionary
        Register<Character> character;
        // True if the string leading to this node has already been inserted, false otherwise
        Register<Boolean> absent = new Register<>(true);
        // Encodes the set of strings starting with the string leading to this word,
        // including the character encoded by this node
        Register<Node> suffix = new Register<>(null);
        // Encodes the set of strings starting with the string leading to this word,
        // excluding the character encoded by this node,
        // and whose next character is strictly greater than the character encoded by this node
        Register<Node> next;

        Node(char character, Node next) {
            this.character = new Register<>(character);
            this.next = new Register<>(next);
        }

        /**
         * Adds the specified string to this set if it is not already present.
         * More formally, adds the specified string s to this set if the set contains no element s2 such that s.equals(s2).
         * If this set already contains the element, the call leaves the set unchanged and returns false.
         * @param s The string that is being inserted in the set
         * @param depth The number of time the pointer "suffix" has been followed
         * @return true if s was not already inserted, false otherwise
         */
        boolean add(String s, int depth, ITransaction t) throws AbortException {
            // First case: we are at the end of the string and this is the correct node
            if (depth >= s.length() || (s.charAt(depth) == character.read(t)) && depth == s.length() - 1) {
                boolean result = absent.read(t);
                absent.write(t, false);
                return result;
            }

            // Second case: the next character in the string was found, but this is not the end of the string
            // We continue in member "suffix"
            if (s.charAt(depth) == character.read(t)) {
                Node nodeSuffix = suffix.read(t);
                if (nodeSuffix == null || nodeSuffix.character.read(t) > s.charAt(depth + 1))
                    suffix.write(t, new Node(s.charAt(depth + 1), nodeSuffix));
                return suffix.read(t).add(s, depth + 1, t);
            }

            // Third case: the next character in the string was not found
            // We continue in member "next"
            // To maintain the order, we may have to add a new node before "next" first
            Node nodeNext = next.read(t);
            if (nodeNext == null || nodeNext.character.read(t) > s.charAt(depth))
                next.write(t, new Node(s.charAt(depth), nodeNext));
            return next.read(t).add(s, depth, t);
        }
    }

    // We start with a first node, to simplify the algorithm, that encodes the smallest non-empty string "\0".
    private final Node start = new Node('\0', null);
    // The empty string is stored separately
    private boolean emptyAbsent = true;

    /**
     * Adds the specified string to this set if it is not already present.
     * More formally, adds the specified string s to this set if the set contains no element s2 such that s.equals(s2).
     * If this set already contains the element, the call leaves the set unchanged and returns false.
     * @param s The string that is being inserted in the set
     * @return true if s was not already inserted, false otherwise
     */
    public boolean add(String s) {
        boolean result = emptyAbsent;

        if (s.equals("")) {
            emptyAbsent = false;
        }

        else {
            ITransaction transaction = new Transaction();
            while (!transaction.isCommited()) {
                try {
                    transaction.begin();
                    result = start.add(s, 0, transaction);
                    transaction.try_to_commit();
                } catch (AbortException ignored) {
                }
            }
        }

        return result;
    }

}