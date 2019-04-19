<h1>English words definition finder</h1>
Command line tool for finding English words definitions in Cambridge dictionary. 
Takes line by line words from the input file, loads their definitions from the website and puts 
them into file.

Usage:
```
definitor -i <file> [-o <file>] [-md] [-me] [-s]

 -i <arg>                     input file
 -md,--multiple-definitions   load multiple definitions for each word
 -me,--multiple-examples      load examples for each definition
 -o <arg>                     output file
 -s,--sort                    sort the words in the alphabetical order

```