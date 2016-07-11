/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package alignment;

import java.util.ArrayList;

/**
 *
 * @author zaynab
 */

public class Alignment {
    
    private double semanticFilteringLowThreshold=0.2;
    
    /**
     * 
     */
    protected void similaritySearch()
    {
    }
    
    /**
     * This method compares SimString to all the names exist in OntSearchList.
     * Once it found the concept name that has the highest similarity to 
     * SimString, it compares their similarity value to a threshold and if the 
     * similarity surpasses the needed threshold, it will return found similar 
     * concept. Otherwise, it will return null.
     * For the direct comparisons, it uses the ISUB similarity measure which has
     * a clear superiority in ontology alignment name searching over other 
     * lexical measures and specially designed for ontology alignment.
     * 
     * @param SimString:<code>String</code> String to compare
     * @param OntSearchList:<code>ArrayList</code> concepts names
     * @param thrshld:<code>double</code> threshold of similarity
     * @return SimOntRes:<code>String</code> most similar String in the 
     *                                       OntSearchList 
     */
    protected String findLexicalSimilar(String SimString,
            ArrayList<String> OntSearchList,double thrshld){
        String SimOntRes=null;
        double SimOntResSimValue=0.0;
        for(String OntRes:OntSearchList){
            double Sim=Scores.ISUBSimilarity(SimString, OntRes);
            if(Sim>SimOntResSimValue){
                SimOntRes=OntRes;
                SimOntResSimValue=Sim;
            }
        }
        if(SimOntResSimValue>thrshld){
            return SimOntRes;
        }
        return null;
    }
    
    /**
     * This method calculates the semantic similarity of two concepts by JIANG 
     * measure.
     * If the similarity could not be determined, this method has not enough 
     * information to reject the similarity(showed in method by -1).
     * If the proposed similar concepts have a semantic similarity lower than a 
     * threshold, the method will reject the similarity.
     * Otherwise, the method accepts their similarity.
     * @param OntResName:<code>String</code>
     * @param SimString:<code>String</code>
     * @return <code>true</code> if the similarity is accepted,
     *         <code>false</code> if not
     */
    protected boolean semanticFilteringAccepts(String OntResName,String SimString)
    {
        Scores s=new Scores();
        double sim=s.JiangConrath(OntResName,SimString);
        if(sim==-1 || sim>semanticFilteringLowThreshold){
            return true;
        }
        return false;
    }
    
    /**
     *This method compares each concept name from OntSearchList to all 
     *candidates NameString in matrix; if the concepts name similarity to 
     *candidate NameString be more than SimilarityValue, candidate 
     *MostSimilarOntRes is replaced by this new concept and SimilarityValue is 
     *also updated. Once all ontology concepts were compared to matrix 
     *candidates, each SimMatrix[i][j] would contain ontology concept that is 
     *most similar concept to original SimString. For comparing the extracted 
     *synonyms and hypernyms to ontology concepts, we simply use the Levenshtein
     *similarity measure since we know the ISUB failed in previous step and we
     *want to use an alternative measure.
     */
    protected void calculateSimilarities()
    {
    }
    
    /**
     *
     */
    protected void findCandidate()
    {
    }
    
    /**
     *
     */
    protected void WSD()
    {
        
    }
}
