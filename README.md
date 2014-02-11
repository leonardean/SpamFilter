SpamFilter
==========

An Email spam filter algorithm based on Bayesian. (Note: works only for English Emails) 

##Email Content Extraction
* Remove system auto text (if it has)
* Interpret HTML content
* Decode Base64 coding
* Remove punctuations except '$'

##Domain Specific Features
* Vocabulary Reduction (VR)
* Probability of Non-character-num Symbols (PNS)
* Sender Domain (SD)
* Concrete Send Time (CST)
* Reply

##Analysis
![analysis](http://www.mftp.info/20140202/1392081219x1927178161.png)
##Top words
* Non-spam: more, there, would
* Spam: how, send, order

##Compilation and Running
To compile:
```
javac filter.java
```
To run:
```
java filter emailFile.txt
```
Note that the java Serialized Files are the trained classification models and need to be kept.
