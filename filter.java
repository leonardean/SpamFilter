import java.io.*;
import java.util.*;

import org.apache.commons.codec.binary.Base64;
import org.jsoup.Jsoup;

public class filter {

	private static String[] dangerousWord = { "$", "only", "sex", "credit",
			"cash", "free" };
	private String inputDirectory; // training data directory
	private String inputTestFile; // test file name
	private double pHam, pSpam; // probability of each class
	private int hamWordCount, spamWordCount; // number of words in each class
	private String hamContent, spamContent, testContent; // content of each
															// class
	private String hamSenderDomain, hamTime, hamReply, hamNonCharNum,
			spamSenderDomain, spamTime, spamReply, spamNonCharNum,
			testSenderDomain, testTime, testReply, testNonCharNum;;
	private int vocabLen; // size of vocabulary
	private ArrayList<String> vocabulary; // this is also useful in test()
	private HashMap<String, Integer> hamSenderDomainHash, hamTimeHash,
			hamReplyHash, hamNonCharNumHash, spamSenderDomainHash,
			spamTimeHash, spamReplyHash, spamNonCharNumHash;
	private HashMap<String, Double> hamSenderDomainProb, hamTimeProb,
			hamReplyProb, hamNonCharNumProb, spamSenderDomainProb,
			spamTimeProb, spamReplyProb, spamNonCharNumProb;
	private HashMap<String, Double> hamWordProb, spamWordProb, classProb;

	/**
	 * get the probability of each class, the concatenated documents of each
	 * class, the number of words of each class, and the size of whole
	 * vocabulary.
	 */
	private void train() throws IOException {
		hamContent = new String();// the content of ham training files
		spamContent = new String();// the content of spam training files
		vocabulary = new ArrayList<String>();// all the
												// vocabulary
												// within
												// training
												// files
		hamSenderDomainHash = new HashMap<String, Integer>();
		hamTimeHash = new HashMap<String, Integer>();
		hamReplyHash = new HashMap<String, Integer>();
		hamNonCharNumHash = new HashMap<String, Integer>();
		spamSenderDomainHash = new HashMap<String, Integer>();
		spamTimeHash = new HashMap<String, Integer>();
		spamReplyHash = new HashMap<String, Integer>();
		spamNonCharNumHash = new HashMap<String, Integer>();

		hamWordProb = new HashMap<String, Double>();
		spamWordProb = new HashMap<String, Double>();
		classProb = new HashMap<String, Double>();

		for (int i = 0; i < 24; i++) {
			hamTimeHash.put(String.valueOf(i), 0);
			spamTimeHash.put(String.valueOf(i), 0);
		}
		int numSpam = 0, numHam = 0, numFile = 0;// numbers of training files
		String buff;// file reading tmp buffer

		/* get all the files in the directory */
		File directory = new File(inputDirectory);
		File[] files = directory.listFiles();

		/*
		 * assign the content of hams and spams while counting the number of
		 * files
		 */
		for (File f : files) {
			if (f.getName().startsWith("spam")) {
				String readbuffer = readFile(f, 1);
				if (!spamTime.equals("-1")) {
					spamSenderDomainHash.put(spamSenderDomain, 0);
					hamSenderDomainHash.put(spamSenderDomain, 0);
					spamNonCharNumHash.put(spamNonCharNum, 0);
					hamNonCharNumHash.put(spamNonCharNum, 0);
				}
			}
			if (f.getName().startsWith("ham")) {
				String readbufferString = readFile(f, 0);
				if (!hamTime.equals("-1")) {
					spamSenderDomainHash.put(hamSenderDomain, 0);
					hamSenderDomainHash.put(hamSenderDomain, 0);
					spamNonCharNumHash.put(hamNonCharNum, 0);
					hamNonCharNumHash.put(hamNonCharNum, 0);
				}
			}
		}
		for (File f : files) {
			numFile++;
			// System.out.println(f.getName());
			if (f.getName().startsWith("spam")) {
				numSpam++; // add to the number of spams
				String readbuffer = readFile(f, 1);
				if (!spamNonCharNumHash.containsKey(spamNonCharNum))
					spamNonCharNumHash.put(spamNonCharNum, 1);
				else if (spamNonCharNumHash.containsKey(spamNonCharNum))
					spamNonCharNumHash.put(spamNonCharNum,
							spamNonCharNumHash.get(spamNonCharNum) + 1);
				if (!spamTime.equals("-1")) {
					if (!spamSenderDomainHash.containsKey(spamSenderDomain)
							&& !(spamSenderDomain.equals(""))
							&& (spamSenderDomain.length() <= 3))
						spamSenderDomainHash.put(spamSenderDomain, 1);
					else if (spamSenderDomainHash.containsKey(spamSenderDomain)
							&& !(spamSenderDomain.equals(""))
							&& (spamSenderDomain.length() <= 3))
						spamSenderDomainHash.put(spamSenderDomain,
								spamSenderDomainHash.get(spamSenderDomain) + 1);
					if (!spamTimeHash.containsKey(spamTime))
						spamTimeHash.put(spamTime, 1);
					else if (spamTimeHash.containsKey(spamTime))
						spamTimeHash.put(spamTime,
								spamTimeHash.get(spamTime) + 1);
					if (!spamReplyHash.containsKey(spamReply))
						spamReplyHash.put(spamReply, 1);
					else if (spamReplyHash.containsKey(spamReply))
						spamReplyHash.put(spamReply,
								spamReplyHash.get(spamReply) + 1);

				}
				spamContent += readbuffer.toLowerCase();
				spamContent = spamContent.replaceAll("[^a-zA-Z$\n]+", " ");

			} else if (f.getName().startsWith("ham")) { // the same thing for
														// ham
				numHam++;
				String readbuffer = readFile(f, 0);
				if (!hamNonCharNumHash.containsKey(hamNonCharNum))
					hamNonCharNumHash.put(hamNonCharNum, 1);
				else if (hamNonCharNumHash.containsKey(hamNonCharNum))
					hamNonCharNumHash.put(hamNonCharNum,
							hamNonCharNumHash.get(hamNonCharNum) + 1);
				if (!hamTime.equals("-1")) {
					if (!hamSenderDomainHash.containsKey(hamSenderDomain)
							&& !(hamSenderDomain.equals(""))
							&& (hamSenderDomain.length() <= 3))
						hamSenderDomainHash.put(hamSenderDomain, 1);
					else if (hamSenderDomainHash.containsKey(hamSenderDomain)
							&& !(hamSenderDomain.equals(""))
							&& (hamSenderDomain.length() <= 3))
						hamSenderDomainHash.put(hamSenderDomain,
								hamSenderDomainHash.get(hamSenderDomain) + 1);

					if (!hamTimeHash.containsKey(hamTime))
						hamTimeHash.put(hamTime, 1);
					else if (hamTimeHash.containsKey(hamTime))
						hamTimeHash.put(hamTime, hamTimeHash.get(hamTime) + 1);
					if (!hamReplyHash.containsKey(hamReply))
						hamReplyHash.put(hamReply, 1);
					else if (hamReplyHash.containsKey(hamReply))
						hamReplyHash.put(hamReply,
								hamReplyHash.get(hamReply) + 1);

				}
				hamContent += readbuffer.toLowerCase();
				hamContent = hamContent.replaceAll("[^a-zA-Z$\n]+", " ");
			}
		}
		System.out.println("file read complete!!!");
		// System.out.println(hamSenderDomainHash);
		// System.out.println(hamTimeHash);
		// System.out.println(hamReplyHash);
		// System.out.println(hamNonCharNumHash);
		// System.out.println(spamSenderDomainHash);
		// System.out.println(spamTimeHash);
		// System.out.println(spamReplyHash);
		// System.out.println(spamNonCharNumHash);
		// System.out.println(spamContent);
		// System.out.println(hamContent);

		/* calculate probabilities */
		pHam = (double) numHam / (double) numFile;
		pSpam = (double) numSpam / (double) numFile;
		// System.out.println(numHam);
		// System.out.println(numSpam);
		// System.out.println(numFile);

		classProb.put("hamProb", pHam);
		classProb.put("spamProb", pSpam);

		/* get vocabulary and its length */
		String[] hamWord = hamContent.split("\\s+");
		HashMap<String, Integer> hamWordFreq = new HashMap<String, Integer>();
		for (int i = 0; i < hamWord.length; i++) {
			if (hamWordFreq.containsKey(hamWord[i]))
				hamWordFreq.put(hamWord[i], hamWordFreq.get(hamWord[i]) + 1);
			if (!hamWordFreq.containsKey(hamWord[i]))
				hamWordFreq.put(hamWord[i], 1);
		}
		hamWordCount = hamWord.length;
		String[] spamWord = spamContent.split("\\s+");
		HashMap<String, Integer> spamWordFreq = new HashMap<String, Integer>();
		for (int i = 0; i < spamWord.length; i++) {
			if (spamWordFreq.containsKey(spamWord[i]))
				spamWordFreq
						.put(spamWord[i], spamWordFreq.get(spamWord[i]) + 1);
			if (!spamWordFreq.containsKey(spamWord[i]))
				spamWordFreq.put(spamWord[i], 1);
		}
		spamWordCount = spamWord.length;
		System.out.println(hamWordFreq.size());
		System.out.println(spamWordFreq.size());
		System.out.println("count get!!!");
		// System.out.println(hamWordCount);
		// System.out.println(spamWordCount);
		for (int i = 0; i < hamWordCount; i++) {
			if (!vocabulary.contains(hamWord[i]) && !hamWord[i].equals(""))
				vocabulary.add(hamWord[i]);
		}
		// System.out.println(hamWordFreq);
		for (int i = 0; i < spamWordCount; i++)
			if (!vocabulary.contains(spamWord[i]) && !spamWord[i].equals(""))
				vocabulary.add(spamWord[i]);
		vocabLen = vocabulary.size();
		System.out.println("vocab init");
		/* remove unnecessary words */
		for (int i = 0; i < vocabulary.size(); i++) {
			int hamwordFreq = 0;
			try {
				hamwordFreq = hamWordFreq.get(vocabulary.get(i));
			} catch (Exception e) {
				hamwordFreq = 0;
			}
			int spamwordFreq = 0;
			try {
				spamwordFreq = spamWordFreq.get(vocabulary.get(i));
			} catch (Exception e) {
				spamwordFreq = 0;
			}
			if ((hamwordFreq <= 3) && (spamwordFreq <= 3)) {
				vocabulary.remove(i);
				i--;
				continue;
			}
			if (((double) ((double) hamwordFreq / (double) hamWordCount) >= 0.005)
					&& ((double) ((double) spamwordFreq / (double) spamWordCount) >= 0.005)) {
				// System.out.println(vocabulary.get(i));
				vocabulary.remove(i);
				i--;
				continue;
			}
		}
		System.out.println("vocab complete!!!");
		System.out.println(vocabulary.size());
		vocabLen = vocabulary.size();
		for (int i = 0; i < vocabulary.size(); i++) {
			int hamwordFreq = 0;
			try {
				hamwordFreq = hamWordFreq.get(vocabulary.get(i));
			} catch (Exception e) {
				hamwordFreq = 0;
			}
			int spamwordFreq = 0;
			try {
				spamwordFreq = spamWordFreq.get(vocabulary.get(i));
			} catch (Exception e) {
				spamwordFreq = 0;
			}
			hamWordProb.put(
					vocabulary.get(i),
					java.lang.Math.log((double) (hamwordFreq + 1)
							/ (double) (hamWordCount + vocabLen)));
			spamWordProb.put(
					vocabulary.get(i),
					java.lang.Math.log((double) (spamwordFreq + 1)
							/ (double) (spamWordCount + vocabLen)));
			// System.out.println(i);

		}
		// System.out.println(hamWordCount);
		// System.out.println(hamContent);
		// System.out.println(hamWordProb.size());
		// System.out.println(spamWordProb.size());
		// System.out.println(vocabulary.size());
		//
		// System.out.println(hamWordProb.size());
		// System.out.println(vocabulary);
		// System.out.println(vocabLen);
		System.out.println("word prob complete!!!");
	}

	private void freqToProb() {
		hamSenderDomainProb = new HashMap<String, Double>();
		int hamSenderDomainTotal = 0;
		hamTimeProb = new HashMap<String, Double>();
		int hamTimeTotal = 0;
		hamReplyProb = new HashMap<String, Double>();
		int hamReplyTotal = 0;
		hamNonCharNumProb = new HashMap<String, Double>();
		int hamNonCharNumTotal = 0;
		spamSenderDomainProb = new HashMap<String, Double>();
		int spamSenderDomainTotal = 0;
		spamTimeProb = new HashMap<String, Double>();
		int spamTimeTotal = 0;
		spamReplyProb = new HashMap<String, Double>();
		int spamReplyTotal = 0;
		spamNonCharNumProb = new HashMap<String, Double>();
		int spamNonCharNumTotal = 0;

		String[] hamSenderDomainSet = (String[]) (hamSenderDomainHash.keySet()
				.toArray(new String[hamSenderDomainHash.size()]));
		String[] hamTimeSet = (String[]) (hamTimeHash.keySet()
				.toArray(new String[hamTimeHash.size()]));
		String[] hamReplySet = (String[]) (hamReplyHash.keySet()
				.toArray(new String[hamReplyHash.size()]));
		String[] hamNonCharNumSet = (String[]) (hamNonCharNumHash.keySet()
				.toArray(new String[hamNonCharNumHash.size()]));
		String[] spamSenderDomainSet = (String[]) (spamSenderDomainHash
				.keySet().toArray(new String[spamSenderDomainHash.size()]));
		String[] spamTimeSet = (String[]) (spamTimeHash.keySet()
				.toArray(new String[spamTimeHash.size()]));
		String[] spamReplySet = (String[]) (spamReplyHash.keySet()
				.toArray(new String[spamReplyHash.size()]));
		String[] spamNonCharNumSet = (String[]) (spamNonCharNumHash.keySet()
				.toArray(new String[spamNonCharNumHash.size()]));

		for (int i = 0; i < hamSenderDomainSet.length; i++) {
			hamSenderDomainTotal += hamSenderDomainHash
					.get(hamSenderDomainSet[i]);
		}
		for (int i = 0; i < spamSenderDomainSet.length; i++) {
			spamSenderDomainTotal += spamSenderDomainHash
					.get(spamSenderDomainSet[i]);
		}
		for (int i = 0; i < hamTimeSet.length; i++) {
			hamTimeTotal += hamTimeHash.get(hamTimeSet[i]);
		}
		for (int i = 0; i < hamReplySet.length; i++) {
			hamReplyTotal += hamReplyHash.get(hamReplySet[i]);
		}
		for (int i = 0; i < hamNonCharNumSet.length; i++) {
			hamNonCharNumTotal += hamNonCharNumHash.get(hamNonCharNumSet[i]);
		}
		for (int i = 0; i < spamTimeSet.length; i++) {
			spamTimeTotal += spamTimeHash.get(spamTimeSet[i]);
		}
		for (int i = 0; i < spamReplySet.length; i++) {
			spamReplyTotal += spamReplyHash.get(spamReplySet[i]);
		}
		for (int i = 0; i < spamNonCharNumSet.length; i++) {
			spamNonCharNumTotal += spamNonCharNumHash.get(spamNonCharNumSet[i]);
		}
		for (int i = 0; i < hamSenderDomainSet.length; i++) {
			hamSenderDomainProb
					.put(hamSenderDomainSet[i],
							java.lang.Math.log(((double) (hamSenderDomainHash
									.get(hamSenderDomainSet[i]) + 1) / (double) (hamSenderDomainTotal * 2 + spamSenderDomainTotal))));
		}

		for (int i = 0; i < hamTimeSet.length; i++) {
			hamTimeProb.put(hamTimeSet[i],
					java.lang.Math.log((double) ((double) (hamTimeHash
							.get(hamTimeSet[i]) + 1)
							/ (double) hamTimeTotal
							* 2 + spamTimeTotal)));
		}
		for (int i = 0; i < hamReplySet.length; i++) {
			hamReplyProb.put(hamReplySet[i],
					java.lang.Math.log((double) ((double) hamReplyHash
							.get(hamReplySet[i]) / (double) hamReplyTotal)));
		}

		for (int i = 0; i < hamNonCharNumSet.length; i++) {
			hamNonCharNumProb
					.put(hamNonCharNumSet[i],
							java.lang.Math.log((double) ((double) (hamNonCharNumHash
									.get(hamNonCharNumSet[i]) + 1) / (double) (hamNonCharNumTotal * 2 + spamNonCharNumTotal))));
		}
		for (int i = 0; i < spamSenderDomainSet.length; i++) {
			spamSenderDomainProb
					.put(spamSenderDomainSet[i],
							java.lang.Math
									.log((double) ((double) (spamSenderDomainHash
											.get(spamSenderDomainSet[i]) + 1) / (double) (spamSenderDomainTotal * 2 + hamSenderDomainTotal))));
		}

		for (int i = 0; i < spamTimeSet.length; i++) {
			spamTimeProb
					.put(spamTimeSet[i],
							java.lang.Math.log((double) ((double) (spamTimeHash
									.get(spamTimeSet[i]) + 1) / (double) (spamTimeTotal * 2 + hamTimeTotal))));
		}

		for (int i = 0; i < spamReplySet.length; i++) {
			spamReplyProb.put(spamReplySet[i],
					java.lang.Math.log((double) ((double) spamReplyHash
							.get(spamReplySet[i]) / (double) spamReplyTotal)));
		}

		for (int i = 0; i < spamNonCharNumSet.length; i++) {
			spamNonCharNumProb
					.put(spamNonCharNumSet[i],
							java.lang.Math.log((double) ((double) (spamNonCharNumHash
									.get(spamNonCharNumSet[i]) + 1) / (double) (spamNonCharNumTotal * 2 + hamNonCharNumTotal))));
		}
		// System.out.println(hamNonCharNumProb);
	}

	private void writeFile() throws FileNotFoundException, IOException {
		ObjectOutputStream hamWordProbWrite = new ObjectOutputStream(
				new FileOutputStream("ham_word_prob.ser"));
		ObjectOutputStream hamSenderDomainProbWrite = new ObjectOutputStream(
				new FileOutputStream("ham_sender_domain_prob.ser"));
		ObjectOutputStream hamTimeWrite = new ObjectOutputStream(
				new FileOutputStream("ham_time_prob.ser"));
		ObjectOutputStream hamReplyWrite = new ObjectOutputStream(
				new FileOutputStream("ham_reply_prob.ser"));
		ObjectOutputStream hamNonCharNumWrite = new ObjectOutputStream(
				new FileOutputStream("ham_non_char_num_prob.ser"));
		ObjectOutputStream spamWordProbWrite = new ObjectOutputStream(
				new FileOutputStream("spam_word_prob.ser"));
		ObjectOutputStream spamSenderDomainProbWrite = new ObjectOutputStream(
				new FileOutputStream("spam_sender_domain_prob.ser"));
		ObjectOutputStream spamTimeWrite = new ObjectOutputStream(
				new FileOutputStream("spam_time_prob.ser"));
		ObjectOutputStream spamReplyWrite = new ObjectOutputStream(
				new FileOutputStream("spam_reply_prob.ser"));
		ObjectOutputStream spamNonCharNumWrite = new ObjectOutputStream(
				new FileOutputStream("spam_non_char_num_prob.ser"));
		ObjectOutputStream classProbWrite = new ObjectOutputStream(
				new FileOutputStream("class_prob.ser"));
		ObjectOutputStream vocabularyWrite = new ObjectOutputStream(
				new FileOutputStream("vocabulary.ser"));

		hamWordProbWrite.writeObject(hamWordProb);
		hamSenderDomainProbWrite.writeObject(hamSenderDomainProb);
		hamTimeWrite.writeObject(hamTimeProb);
		hamReplyWrite.writeObject(hamReplyProb);
		hamNonCharNumWrite.writeObject(hamNonCharNumProb);
		spamWordProbWrite.writeObject(spamWordProb);
		spamSenderDomainProbWrite.writeObject(spamSenderDomainProb);
		spamTimeWrite.writeObject(spamTimeProb);
		spamReplyWrite.writeObject(spamReplyProb);
		spamNonCharNumWrite.writeObject(spamNonCharNumProb);
		classProbWrite.writeObject(classProb);
		vocabularyWrite.writeObject(vocabulary);

		hamWordProbWrite.close();
		hamSenderDomainProbWrite.close();
		hamTimeWrite.close();
		hamReplyWrite.close();
		hamNonCharNumWrite.close();
		spamWordProbWrite.close();
		spamSenderDomainProbWrite.close();
		spamTimeWrite.close();
		spamReplyWrite.close();
		spamNonCharNumWrite.close();
		classProbWrite.close();
		vocabularyWrite.close();
	}

	@SuppressWarnings("unchecked")
	private void readHash() throws FileNotFoundException, IOException,
			ClassNotFoundException {
		ObjectInputStream hamWordProbRead = new ObjectInputStream(
				new FileInputStream("ham_word_prob.ser"));
		ObjectInputStream hamSenderDomainRead = new ObjectInputStream(
				new FileInputStream("ham_sender_domain_prob.ser"));
		ObjectInputStream hamTimeRead = new ObjectInputStream(
				new FileInputStream("ham_time_prob.ser"));
		ObjectInputStream hamReplyRead = new ObjectInputStream(
				new FileInputStream("ham_reply_prob.ser"));
		ObjectInputStream hamNonCharNumRead = new ObjectInputStream(
				new FileInputStream("ham_non_char_num_prob.ser"));
		ObjectInputStream spamWordProbRead = new ObjectInputStream(
				new FileInputStream("spam_word_prob.ser"));
		ObjectInputStream spamSenderDomainRead = new ObjectInputStream(
				new FileInputStream("spam_sender_domain_prob.ser"));
		ObjectInputStream spamTimeRead = new ObjectInputStream(
				new FileInputStream("spam_time_prob.ser"));
		ObjectInputStream spamReplyRead = new ObjectInputStream(
				new FileInputStream("spam_reply_prob.ser"));
		ObjectInputStream spamNonCharNumRead = new ObjectInputStream(
				new FileInputStream("spam_non_char_num_prob.ser"));
		ObjectInputStream classProbRead = new ObjectInputStream(
				new FileInputStream("class_prob.ser"));
		ObjectInputStream vocabularyRead = new ObjectInputStream(
				new FileInputStream("vocabulary.ser"));

		vocabulary = (ArrayList<String>) vocabularyRead.readObject();
		classProb = (HashMap<String, Double>) classProbRead.readObject();
		hamWordProb = (HashMap<String, Double>) hamWordProbRead.readObject();
		spamWordProb = (HashMap<String, Double>) spamWordProbRead.readObject();
		hamSenderDomainProb = (HashMap<String, Double>) hamSenderDomainRead
				.readObject();
		spamSenderDomainProb = (HashMap<String, Double>) spamSenderDomainRead
				.readObject();
		hamTimeProb = (HashMap<String, Double>) hamTimeRead.readObject();
		spamTimeProb = (HashMap<String, Double>) spamTimeRead.readObject();
		hamReplyProb = (HashMap<String, Double>) hamReplyRead.readObject();
		spamReplyProb = (HashMap<String, Double>) spamReplyRead.readObject();
		hamNonCharNumProb = (HashMap<String, Double>) hamNonCharNumRead
				.readObject();
		spamNonCharNumProb = (HashMap<String, Double>) spamNonCharNumRead
				.readObject();
	}

	private String classify() throws IOException,
			ClassNotFoundException {
		double probAttr = 0;
		double probHam, probSpam;
		double wordAppearInVocab = 0;
		testContent = new String();
		ArrayList<String> testWord = new ArrayList<String>();

		testContent = readFile(new File(inputTestFile), 2);
		readHash();

		for (int i = 0; i < testContent.split(" ").length; i++)
			testWord.add(testContent.split(" ")[i]);
		/* ham prob */
		for (int i = 0; i < testWord.size(); i++) {
			if (vocabulary.contains(testWord.get(i)))
				wordAppearInVocab++;
			if (wordAppearInVocab != 0)
				probAttr += hamWordProb.get(testWord.get(i));
			wordAppearInVocab = 0;
		}
		probHam = classProb.get("hamProb") + probAttr;
		probAttr = 0;
		/* features */
		probHam += hamNonCharNumProb.get(testNonCharNum);
		if (!testTime.endsWith("-1")) {
			probHam += hamSenderDomainProb.get(testSenderDomain);
			probHam += hamTimeProb.get(testTime);
			probHam += hamReplyProb.get(testReply);
		}
//		for (int i = 0; i < dangerousWord.length; i++) {
//			probHam += hamWordProb.get(dangerousWord[i]);
//		}
		/* feature end */

		/* spam prob */
		for (int i = 0; i < testWord.size(); i++) {
			if (vocabulary.contains(testWord.get(i)))
				wordAppearInVocab++;
			if (wordAppearInVocab != 0)
				probAttr += spamWordProb.get(testWord.get(i));
			wordAppearInVocab = 0;
		}
		probSpam = classProb.get("spamProb") + probAttr;
		probAttr = 0;

		/* features */
		probSpam += spamNonCharNumProb.get(testNonCharNum);
		if (!testTime.endsWith("-1")) {
			probSpam += spamSenderDomainProb.get(testSenderDomain);
			probSpam += spamTimeProb.get(testTime);
			probSpam += spamReplyProb.get(testReply);
		}
//		for (int i = 0; i < dangerousWord.length; i++) {
//			probSpam += spamWordProb.get(dangerousWord[i]);
//		}
		/* feature end */

		if (probHam > probSpam)
			return "Ham";
		else
			return "Spam";
	}

	/**
	 * this method calculate the probabilities of being spam and ham under the
	 * theory of naive Bayesian. by comparing each probability, an result is
	 * printed
	 * 
	 * @throws IOException
	 */
	private void test() throws IOException {
		double wordFreq = 0, probAttr = 0, // overlapped word frequency,
											// probability of each attribute
		probHam, probSpam;// probability of being ham and spam
		double wordAppearInVocab = 0; // whether words in test file appeared in
										// all training files
		testContent = new String();// content of test file

		/* words in test, spam, ham files */
		ArrayList<String> testWord = new ArrayList<String>();
		ArrayList<String> hamWord = new ArrayList<String>();
		ArrayList<String> spamWord = new ArrayList<String>();
		String buff;// tmp buffer for file reading
		BufferedReader in = new BufferedReader(new FileReader(inputTestFile));

		/* assign words */
		while ((buff = in.readLine()) != null)
			testContent += buff;
		for (int i = 0; i < testContent.split(" ").length; i++)
			testWord.add(testContent.split(" ")[i]);
		for (int i = 0; i < hamContent.split(" ").length; i++)
			hamWord.add(hamContent.split(" ")[i]);
		for (int i = 0; i < spamContent.split(" ").length; i++)
			spamWord.add(spamContent.split(" ")[i]);

		/*
		 * calculate the probability of being ham
		 */
		for (int i = 0; i < testWord.size(); i++) {
			if (vocabulary.contains(testWord.get(i))) // check whether a word is
														// in vocabulary, if
														// not, ignore it
				wordAppearInVocab++;
			for (int j = 0; j < hamWord.size(); j++) { // get word frequency of
														// words in training
														// files
				if (testWord.get(i).equals(hamWord.get(j))) {
					wordFreq++;
				}
			}
			if (wordAppearInVocab != 0) {
				probAttr += java.lang.Math.log((double) (wordFreq + 1) // get
																		// probability
																		// of an
																		// attribute
						/ (double) (hamWordCount + vocabLen));
			}
			wordFreq = 0;
			wordAppearInVocab = 0;
		}
		probHam = java.lang.Math.log(pHam) + probAttr;
		probAttr = 0;
		/*
		 * calculate the probability of being spam
		 */
		for (int i = 0; i < testWord.size(); i++) {
			if (vocabulary.contains(testWord.get(i)))
				wordAppearInVocab++;

			for (int j = 0; j < spamWord.size(); j++) {
				if (testWord.get(i).equals(spamWord.get(j))) {
					wordFreq++;
				}

			}
			if (wordAppearInVocab != 0) {
				probAttr += java.lang.Math.log((double) (wordFreq + 1)
						/ (double) (spamWordCount + vocabLen));
			}
			wordFreq = 0;
			wordAppearInVocab = 0;
		}
		probSpam = java.lang.Math.log(pSpam) + probAttr;
		probAttr = 0;

		/*
		 * output
		 */

		/*
		 * for (int i = 0; i < vocabulary.size(); i++) {
		 * System.out.println(vocabulary.get(i)); } System.out.println(probHam -
		 * probSpam);
		 */
		if (probHam > probSpam)
			System.out.println("ham");
		else
			System.out.println("spam");
	}

	private String readFile(File file, int flag) throws IOException { // flag:0
																		// ham,
																		// 1
																		// spam,
																		// 2
																		// test
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String senderDomain = "";
		String subject = "";
		int nonCharNum = -1;
		int sendTime = -1;
		Boolean firstLine = true;
		Boolean base64 = false;
		Boolean reply = false;
		Boolean firstSpace = true;
		String emailContent = "";
		String emailContentLine = new String();
		while ((emailContentLine = reader.readLine()) != null) {
			if (emailContentLine.equals("-- "))
				break;
			if (emailContentLine.startsWith("From ") && (firstLine == true)) {
				senderDomain = emailContentLine.split(" ")[1].split("\\.")[emailContentLine
						.split(" ")[1].split("\\.").length - 1].toLowerCase();
			}
			if (emailContentLine.startsWith("Return-Path: ")) {
				if (emailContentLine.split(" ")[1].split("\\.")[emailContentLine
						.split(" ")[1].split("\\.").length - 1]
						.replaceAll("\\W+", "").toLowerCase().length() <= 3)
					senderDomain = emailContentLine.split(" ")[1].split("\\.")[emailContentLine
							.split(" ")[1].split("\\.").length - 1].replaceAll(
							"\\W+", "").toLowerCase();
			}
			if ((emailContentLine.startsWith("From ") && (firstLine == true))
					|| emailContentLine.startsWith("Delivery-Date")) {
				int hour = Integer
						.parseInt(emailContentLine.split(" ")[emailContentLine
								.split(" ").length - 2].split(":")[0]);
				sendTime = hour;
			}
			if (emailContentLine.startsWith("Date: ")
					&& (emailContentLine.split("\\s+").length == 7)) {
				int hour = 0;
				try {
					hour = Integer.parseInt(emailContentLine.split("\\s+")[5]
							.split(":")[0]);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					System.out.println(file.getAbsolutePath());
				}
				sendTime = hour;
			}
			if (emailContentLine.startsWith("Subject:")) {
				subject = emailContentLine.substring(9);
				if (subject.startsWith("Re:")) {
					reply = true;
					subject = subject.substring(5);
				}
			}
			emailContent += emailContentLine + "\n";
			firstLine = false;
			if (emailContentLine.length() == 0) {
				if (firstSpace == true)
					emailContent = subject + "\n";
				firstSpace = false;
			}
			if (emailContentLine.contains("base64")) {
				emailContent = "";
				base64 = true;
			}
		}
		if (base64 == true) {
			emailContent = emailContent.replace(" ", "+");
			emailContent = Jsoup.parse(
					new String(Base64.decodeBase64(emailContent.getBytes())))
					.text();
		}
		emailContent = Jsoup.parse(emailContent).text().replaceAll(" ", " ");
		int orginalLengh = emailContent.length();
		emailContent = emailContent.replaceAll("[^a-zA-Z0-9$]+", " ");
		emailContent = emailContent.replace("$", "$ ");
		nonCharNum = (int) (((double) (orginalLengh - emailContent.length()) / orginalLengh) * 10);
		if (flag == 0) {
			hamSenderDomain = new String();
			hamTime = new String();
			hamReply = new String();
			hamNonCharNum = new String();
			hamSenderDomain = senderDomain;
			hamTime = String.valueOf(sendTime);
			hamReply = reply.toString();
			hamNonCharNum = String.valueOf(nonCharNum);
		} else if (flag == 1) {
			spamSenderDomain = new String();
			spamTime = new String();
			spamReply = new String();
			spamNonCharNum = new String();
			spamSenderDomain = senderDomain;
			spamTime = String.valueOf(sendTime);
			spamReply = reply.toString();
			spamNonCharNum = String.valueOf(nonCharNum);
		} else if (flag == 2) {
			testSenderDomain = new String();
			testTime = new String();
			testReply = new String();
			testNonCharNum = new String();
			testSenderDomain = senderDomain;
			testTime = String.valueOf(sendTime);
			testReply = reply.toString();
			testNonCharNum = String.valueOf(nonCharNum);
		}
		reader.close();

		return emailContent;
	}

	/*
	 * check whether user inputs are valid
	 */
	private void inputHandle(String[] arguments) {
		if (arguments.length != 1)
			System.err.println("There needs to be one argument:\n"
					+ "directory of training data set, and test file name.");
		else {			
			inputTestFile = arguments[0];
		}
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		filter ft = new filter();
		ft.inputHandle(args);
		System.out.println(ft.classify());
//		 ft.train();
//		 System.out.println("train complete!!!");
//		 ft.freqToProb();
//		 System.out.println("prob complete!!!");
//		 ft.writeFile();

//		File directory = new File("spam_train");
//		File[] files = directory.listFiles();

		/*
		 * assign the content of hams and spams while counting the number of
		 * files
		 */

//		int trueSpamPrecision = 0, classifiedasSpamPrecision = 0;
//		int trueSpamRecall = 0, classfiedasSpamRecall = 0;
//		for (File f : files) {
//
//			if (f.getName().startsWith("spam")) {
//				trueSpamRecall++;
//				if (ft.classify(f).equals("Spam"))
//					classfiedasSpamRecall++;
//			}
//			if (ft.classify(f).equals("Spam")) {
//				classifiedasSpamPrecision++;
//				if (f.getName().startsWith("spam"))
//					trueSpamPrecision++;
//			}
//		}
//		System.out.println(trueSpamPrecision + " " + classifiedasSpamPrecision);
//		System.out.println(classfiedasSpamRecall + " " + trueSpamRecall);
//		System.out
//				.println("Precision: "
//						+ (double) ((double) trueSpamPrecision / (double) classifiedasSpamPrecision));
//		System.out
//				.println("Recall: "
//						+ (double) ((double) classfiedasSpamRecall / (double) trueSpamRecall));
//		ft.readHash();

		// System.out.println(ft.classify(new File(inputTestFile)));
		// ft.test();
	}
}
