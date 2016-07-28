/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server.mapping.SemanticSearchAlgorithm;

/**
 *
 * @author zaynab
 */
public class Candidate{

    private String NameString, MostSimilarOntRes;
    private double SimilarityValue;

    protected Candidate(String name, String similar, double simValue) {
        NameString = name;
        MostSimilarOntRes = similar;
        SimilarityValue = simValue;
    }

    //Getters and Setters
    public String getNameString() {
        return NameString;
    }

    public void setNameString(String NameString) {
        this.NameString = NameString;
    }

    public String getMostSimilarOntRes() {
        return MostSimilarOntRes;
    }

    public void setMostSimilarOntRes(String MostSimilarOntRes) {
        this.MostSimilarOntRes = MostSimilarOntRes;
    }

    public double getSimilarityValue() {
        return SimilarityValue;
    }

    public void setSimilarityValue(double SimilarityValue) {
        this.SimilarityValue = SimilarityValue;
    }

    @Override
    public String toString() {
        return NameString+" , "+MostSimilarOntRes+" , "+String.valueOf(SimilarityValue);
    }

    public int compareTo(Candidate c) {
        if(NameString.equals(c.getNameString()))
            if(SimilarityValue>c.getSimilarityValue())
                return 1;
            else
                if(SimilarityValue<c.getSimilarityValue())
                    return -1;
        return 0;
    }

}
