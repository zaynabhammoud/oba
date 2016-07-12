/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alignment;

/**
 *
 * @author zaynab
 */
public class Test {
    public static void main(String[] args){
        System.out.println("LEVEN SIMILARITY= "+Scores.LevenSimilarity("String$Sim", "$SimilarityString"));
        System.out.println("ISUB SIMILARITY= "+Scores.ISUBSimilarity("String$Sim", "$SimilarityString"));
        System.out.println("JIANGCONRATH SIMILARITY= "+Scores.JiangConrath("String", "SimString"));
    }
    
}
