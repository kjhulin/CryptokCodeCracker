# CryptokCodeCracker
# By Kevin Hulin (Cryptok)

About the source:
This project was developed over the past five years, largely to automate the decryption of codes in order to compete in the Defcon Badge Challenge.  Read more about the badge challenge here: http://elegin.com

This project has two main components: RKCCrack and ClassicCipherSolver.

To read about Running Key Cipher and the techniques employed to solve them here, please see my blog post at https://cryptok.space/crypto

The Classic Cipher Solver includes a set of algorithms and encodings that the program enumerates through in permutations to attempt to find solutions that appear to be English text.

Additionally, a Vigenere Cipher solver is included which utilizes index of coincidence and character frequency distributions to find probable keys and decryptions.

At the heart of these cipher solvers is the language model that, given a string of text, will output a decimal number roughly corresponding to the log likelihood that the string of text is English.

Building and running the project:
This project was developed in Netbeans 8.0, however, all source is included to enable building on any platform where Java is available.

Quickest way to get the code running from command line:
Build:
$ make

Run: (** Must be run from project root -- Program expects corpus directory to be present **)
java -jar dist/rkccrack.jar

On first run, the program will learn 6-gram probabilities from the included corpus.  Feel free to add / remove text files to/from this corpus.  Texts included are taken from project gutenburg https://www.gutenberg.org .

On successive runs, a learned frequency file is read (RKC.prob) to save time.

Sample ciphers are provided in ciphers.txt

Have fun!



