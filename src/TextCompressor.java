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

        // Initialize array with single characters
        for (int i = 0; i < 128; i++) {
            tst.insert("" + (char) i, i);
        }

        // Next available code after EOF
        int code = 129;

        int index = 0;
        // Main Loop
        while (index < strLength) {
            // Get the longest available prefix to maximize storage saving
            prefix = tst.getLongestPrefix(str.substring(index));

            BinaryStdOut.write(tst.lookup(prefix), 8);

            int prefixLen = prefix.length();

            // Make sure index doesn't go out of bounds
            if (index + prefixLen < strLength && code < 256) {
                // Create look ahead code using original prefix and next letter of the text
                String lookAheadCode = prefix + str.charAt(index + prefixLen);
                tst.insert(lookAheadCode, code);
                code++;
            }
            // Updates index
            index += prefixLen;
        }

        // Communicates to expand that this is the end of the file
        BinaryStdOut.write(EOF, 8);
        BinaryStdOut.close();
    }

    private static void expand() {
        String[] map = new String[256];
        // Next available code after EOF
        int code = 129;

        // Initialize dictionary with single characters
        for (int i = 0; i < 128; i++) {
            map[i] = "" + (char) i;
        }

        String currentString = "";

        int currentCode = BinaryStdIn.readInt(8);

        String currentStr = map[currentCode];
        for (int i = 0; i < currentStr.length(); i++) {
            // Write the first string
            BinaryStdOut.write(currentStr.charAt(i), 8);
        }

        int lookAheadCode = BinaryStdIn.readInt(8);

        // Keep writing characters until the end of the file
        while (lookAheadCode != EOF) {
            // If code is already in the dictionary, get its value
            if (lookAheadCode < code) {
                currentString = map[lookAheadCode];
            } else if (lookAheadCode == code) {
                // Special case
                currentString = currentStr + currentStr.charAt(0);
            }

            // Write out string
            for (int i = 0; i < currentString.length(); i++) {
                BinaryStdOut.write(currentString.charAt(i), 8);
            }

            // Build dictionary
            if (code < 256) {
                map[code++] = currentStr + currentString.charAt(0);
            }

            currentStr = currentString;
            lookAheadCode = BinaryStdIn.readInt(8);
        }

        BinaryStdOut.close();
    }


    public static void main(String[] args) {
        if      (args[0].equals("-")) compress();
        else if (args[0].equals("+")) expand();
        else throw new IllegalArgumentException("Illegal command line argument");
    }
}
