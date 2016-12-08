package com.a3dnonsense.ga.gaweightandbalance;

/**
 * Created by mike on 12/7/2016.
 */

import java.util.ArrayList;

class AircraftClass implements java.io.Serializable {
    public class mechanicalWeight implements java.io.Serializable {
        String name;
        Double weight;
        Double arm;
        int id;
    }
    public class baggageArea implements java.io.Serializable {
        String name;
        Double weight;
        Double arm;
        int id;
    }
    public class passengerRow implements java.io.Serializable {
        String name;
        Double arm;
        int numseats;
    }
    public class envelopeData implements java.io.Serializable {
        Double weight;
        Double lowMoment;
        Double highMoment;
    }

    public ArrayList<mechanicalWeight> mechanicalWeights = new ArrayList<mechanicalWeight>();
    public ArrayList<passengerRow> passengerRows = new ArrayList<passengerRow>();
    public ArrayList<baggageArea> baggageAreas = new ArrayList<baggageArea>();
    public ArrayList<envelopeData> envelopeDataSet = new ArrayList<envelopeData>();

    public String tailNumber;
    public String model;
    public Double maxGross;
    public Double momentDivide;

    public AircraftClass makeSample() {
        AircraftClass a = new AircraftClass();
        a.tailNumber = "NSAMPL";
        a.model = "c210";
        a.maxGross = 3400.0;
        a.momentDivide = 1000.0;

        mechanicalWeight empty = new mechanicalWeight();
        mechanicalWeight oil = new mechanicalWeight();
        mechanicalWeight fuel = new mechanicalWeight();
        empty.name = "Empty";
        empty.weight = 2071.0;
        empty.arm = 37.86;
        oil.name = "Oil";
        oil.weight = 22.0;
        oil.arm = -18.18;
        fuel.name = "Fuel";
        fuel.weight = 528.0;
        fuel.arm = 42.97;
        a.mechanicalWeights.add(empty);
        a.mechanicalWeights.add(oil);
        a.mechanicalWeights.add(fuel);

        passengerRow front = new passengerRow();
        passengerRow middle = new passengerRow();
        passengerRow rear = new passengerRow();
        front.name = "Front";
        front.arm = 35.88;
        front.numseats = 2;
        middle.name = "Middle";
        middle.arm = 70.0;
        middle.numseats = 2;
        rear.name = "Rear";
        rear.arm = 94.29;
        rear.numseats = 2;
        a.passengerRows.add(front);
        a.passengerRows.add(middle);
        a.passengerRows.add(rear);

        baggageArea baggage = new baggageArea();
        baggage.name = "Baggage";
        baggage.arm = 115.0;
        a.baggageAreas.add(baggage);

        envelopeData env1 = new envelopeData();
        envelopeData env2 = new envelopeData();
        envelopeData env3 = new envelopeData();
        env1.weight = 2000.0;
        env1.lowMoment = 71.0;
        env1.highMoment = 95.5;
        env2.weight = 2800.0;
        env2.lowMoment = 99.0;
        env2.highMoment = 134.0;
        env3.weight = 3400.0;
        env3.lowMoment = 135.0;
        env3.highMoment = 162.5;
        a.envelopeDataSet.add(env1);
        a.envelopeDataSet.add(env2);
        a.envelopeDataSet.add(env3);

        return a;
    }

    public Boolean SaveToInternal() {


        return false;
    }
}
