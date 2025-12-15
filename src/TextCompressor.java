/******************************************************************************
 *  Compilation:  javac TextCompressor.java
 *  Execution:    java TextCompressor - < input.txt   (compress)
 *  Execution:    java TextCompressor + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *  Data files:   abra.txt
 *                jabberwocky.txt
 *                shakespeare.txt
 *                virus.txt
 *
 *  % java DumpBinary 0 < abra.txt
 *  136 bits
 *
 *  % java TextCompressor - < abra.txt | java DumpBinary 0
 *  104 bits    (when using 8-bit codes)
 *
 *  % java DumpBinary 0 < alice.txt
 *  1104064 bits
 *  % java TextCompressor - < alice.txt | java DumpBinary 0
 *  480760 bits
 *  = 43.54% compression ratio!
 ******************************************************************************/

/**
 *  The {@code TextCompressor} class provides static methods for compressing
 *  and expanding natural language through textfile input.
 *
 *  @author Zach Blick, Ryan Faris
 */
public class TextCompressor {
    public static final int EOF = 128;


    private static void compress() {
        String str = BinaryStdIn.readString();
        int strLength = str.length();

        TST tst = new TST();
        String prefix = "";

        for (int i = 0; i < 128; i++) {
            tst.insert("" + (char) i, i);
        }

        int code = 257;

        int index = 0;
        while (index < strLength) {
            System.out.println(prefix);
            prefix = tst.getLongestPrefix(str.substring(index));

            BinaryStdOut.write(tst.lookup(prefix), 8);

            int prefixLen = prefix.length();


            if (index + prefixLen < strLength && code < 256) {
                String lookAheadCode = prefix + str.charAt(index + prefix.length());
                tst.insert(lookAheadCode, code++);
            }
            index += prefix.length();
        }

        BinaryStdOut.close();
    }

    private static void expand() {


        BinaryStdOut.close();
    }

    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
