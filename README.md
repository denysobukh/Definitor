<h1>English words definition finder</h1>
Command line tool for finding English words definitions in Cambridge dictionary. 
Takes words line by line from the input file, removes duplicates, loads their definitions from the website and puts 
them into file.

Usage:
```
definitor -i <file> [-md] [-me] [-s]

 -i <arg>                     input file
 -md,--multiple-definitions   load multiple definitions for each word
 -me,--multiple-examples      load examples for each definition
 -s,--sort                    sort the words in the alphabetical order

```
