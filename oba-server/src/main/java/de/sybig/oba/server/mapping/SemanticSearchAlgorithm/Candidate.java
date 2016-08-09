/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sybig.oba.server.mapping.SemanticSearchAlgorithm;

import de.sybig.oba.server.ObaClass;

/**
 *
 * @author zaynab
 */
public class Candidate {

    private String NameString, MostSimilarOntRes;
    private ObaClass cls, clsMapped;
    private double SimilarityValue;

    public Candidate(ObaClass cls, ObaClass similar, double simValue) {
        this.cls = cls;
        clsMapped = similar;
        NameString = cls.getProperty("label").getValue();
        MostSimilarOntRes = clsMapped.getProperty("label").getValue();
        SimilarityValue = simValue;
    }

    public Candidate(ObaClass cls, ObaClass similar) {
        this.cls = cls;
        clsMapped = similar;
        NameString = cls.getProperty("label").getValue();
        MostSimilarOntRes = clsMapped.getProperty("label").getValue();
        SimilarityValue = 0.0;
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

    public ObaClass getCls() {
        return cls;
    }

    public void setCls(ObaClass cls) {
        this.cls = cls;
    }

    public ObaClass getClsMapped() {
        return clsMapped;
    }

    public void setClsMapped(ObaClass clsMapped) {
        this.clsMapped = clsMapped;
    }

    @Override
    public String toString() {
        return cls.toStringID() + "," + NameString + " , " + clsMapped.toStringID() + "," + MostSimilarOntRes + " , " + String.valueOf(SimilarityValue);
    }

    public int compareTo(Candidate c) {
        if (NameString.equals(c.getNameString())) {
            if (SimilarityValue > c.getSimilarityValue()) {
                return 1;
            } else {
                if (SimilarityValue < c.getSimilarityValue()) {
                    return -1;
                }
            }
        }
        return 0;
    }

}
