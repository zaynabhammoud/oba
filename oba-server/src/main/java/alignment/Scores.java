/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alignment;

import info.debatty.java.stringsimilarity.*;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.lexical_db.NictWordNet;

/**
 *
 * @author zaynab
 */
//TODO
public class Scores {
 
    /**
     * Returns the Jaro-Winkler distance between two strings
     * 
     * @param s1:<code>String</code>
     * @param s2:<code>String</code>
     * @return distance:<code>double</code>
     */
    public double winkler(String s1,String s2){
        JaroWinkler jw = new JaroWinkler();
        return jw.similarity(s1,s2);
    }
   
//    /**
//     * Returns an ArrayList containing the common characters between two strings
//     * 
//     * @param s1:<code>String</code>
//     * @param s2:<code>String</code>
//     * @return common:<code>ArrayList</code>
//     */
//    private ArrayList<Character> com(String s1,String s2){
//        ArrayList<Character> common=new ArrayList<Character>();
//        for(int i=0;i<s1.length();i++){
//            for(int j=0;j<s2.length();j++){
//                if(s1.charAt(i)==s2.charAt(j)/* && !common.contains(s1.charAt(i))*/){
//                    common.add(s1.charAt(i));
//                }
//            }
//        }
//        return common;
//    }
    
    //Public Methods
	
	/**
	 * Computes the similarity between two Strings
	 * @param st1: the first string to compare
	 * @param st2: the second string to compare
	 * @return the ISub similarity between st1 and st2
	 */
    public static double ISUBSimilarity(String st1, String st2){
            String s1 = st1, s2 = st2;
            if(s1 == null || s2 == null)
		return 0;
            int L1 = s1.length(), L2 = s2.length();
            if(L1 == 0 || L2 == 0)
		return 0;
            int l1, l2;
            double common = 0;
            int best = 2;
            while (s1.length() > 0 && s2.length() > 0 && best != 0){
                best = 0;
		l1 = s1.length();
		l2 = s2.length();
		int i = 0, j = 0;
		int startS1 = 0, endS1 = 0;
		int startS2 = 0, endS2 = 0;
		int p = 0;
		for(i = 0; (i < l1) && (l1 - i > best); i++)
		{
                    j = 0;
                    while (l2 - j > best){
                        int k = i;
			while(j < l2 && s1.charAt(k) != s2.charAt(j))
                            j++;
			if(j != l2){
                            p = j;
                            for(j++, k ++; (j < l2) && (k < l1) && (s1.charAt(k) == s2.charAt(j)); j++, k++);
				if(k - i > best){
                                    best = k - i;
                                    startS1 = i;
                                    endS1 = k;
                                    startS2 = p;
                                    endS2 = j;
                                }
                        }
                    }
                }
		char[] newString = new char[s1.length() - (endS1 - startS1)];
		j = 0;
		for(i = 0; i < s1.length(); i++){
                    if(i >= startS1 && i < endS1)
                    	continue;
                    newString[j++] = s1.charAt(i);
                }
		s1 = new String(newString);
		newString = new char[s2.length() - (endS2 - startS2)];
		j = 0;
		for(i = 0; i < s2.length(); i++){
                    if(i >= startS2 && i < endS2)
			continue;
                    newString[j++] = s2.charAt(i);
                }
		s2 = new String(newString);
		if(best > 2)
                    common += best;
		else
                    best = 0;
            }
		      double commonality = 0;
        double scaledCommon = (double) (2 * common) / (L1 + L2);
        commonality = scaledCommon;
        double winklerImprovement = winklerImprovement(st1, st2, commonality);
        double dissimilarity = 0;
        double rest1 = L1 - common;
        double rest2 = L2 - common;
        double unmatchedS1 = Math.max(rest1, 0);
        double unmatchedS2 = Math.max(rest2, 0);
        unmatchedS1 = rest1 / L1;
        unmatchedS2 = rest2 / L2;
        double suma = unmatchedS1 + unmatchedS2;
        double product = unmatchedS1 * unmatchedS2;
        double p = 0.6;
        if ((suma - product) == 0) {
            dissimilarity = 0;
        } else {
            dissimilarity = (product) / (p + (1 - p) * (suma - product));
        }
        double result = commonality - dissimilarity + winklerImprovement;
        if (result < 0) {
            result = 0;
        }
        return result;
    }

    //Private Methods
    private static double winklerImprovement(String s1, String s2, double commonality) {
        int i, n = Math.min(s1.length(), s2.length());
        for (i = 0; i < n; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                break;
            }
        }
        double commonPrefixLength = Math.min(4, i);
        double winkler = commonPrefixLength * 0.1 * (1 - commonality);
        return winkler;
    }
    
    public double JiangConrath(String s1,String s2){
        ILexicalDatabase db = new NictWordNet();
        RelatednessCalculator jcn = new JiangConrath(db);
        return jcn.calcRelatednessOfWords(s1,s2);
    } 


}
