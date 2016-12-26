package com.a3dnonsense.ga.gaweightandbalance;

import android.content.res.Resources;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

class AircraftClass implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    class mechanicalWeight implements java.io.Serializable {
        String name = "";
        Double weight = 0.0;
        Double arm = 0.0;
        TextView tvWeight;
        TextView tvMoment;
    }
    class baggageArea implements java.io.Serializable {
        String name = "";
        Double weight = 0.0;
        Double arm = 0.0;
        TextView tvWeight;
        TextView tvMoment;
    }
    class passengerRow implements java.io.Serializable {
        String name = "";
        Double arm = 0.0;
        int numseats = 0;
        TextView[] tvWeights;
        TextView tvMoment;
    }
    class envelopeData implements java.io.Serializable {
        Double weight;
        Double lowMoment;
        Double highMoment;
    }

    ArrayList<mechanicalWeight> mechanicalWeights = new ArrayList<>();
    ArrayList<passengerRow> passengerRows = new ArrayList<>();
    ArrayList<baggageArea> baggageAreas = new ArrayList<>();
    ArrayList<envelopeData> envelopeDataSet = new ArrayList<>();

    String tailNumber = "";
    String model = "";
    String weightUnits = "";
    String armUnits = "";
    Double maxGross = 0.0;
    Double emptyWeight = 0.0;
    Double momentDivide = 0.0;

    //methods
    String getTemplateName() {
        return this.tailNumber + "-" + this.model;
    }

    AircraftClass makeSample(String t, Resources res) {
        AircraftClass a = new AircraftClass();
        a.tailNumber = t;
        a.model = "c210";
        a.weightUnits = res.getString(R.string.pounds);
        a.armUnits = res.getString(R.string.inches);
        a.maxGross = 3400.0;
        a.momentDivide = 1000.0;

        mechanicalWeight empty = new mechanicalWeight();
        mechanicalWeight oil = new mechanicalWeight();
        mechanicalWeight fuel = new mechanicalWeight();
        a.emptyWeight = 2071.0;
        empty.name = res.getString(R.string.empty);
        empty.weight = a.emptyWeight;
        empty.arm = 37.86;
        oil.name = res.getString(R.string.oil);
        oil.weight = 22.0;
        oil.arm = -18.18;
        fuel.name = res.getString(R.string.fuel);
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
        front.tvWeights = new TextView[2];
        middle.name = "Middle";
        middle.arm = 70.0;
        middle.numseats = 2;
        middle.tvWeights = new TextView[2];
        rear.name = "Rear";
        rear.arm = 94.29;
        rear.numseats = 2;
        rear.tvWeights = new TextView[2];
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
}

//create transient data holder class which will be used to save dynamic data to a data file
class TransientData implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    //hashmap to hold weight name to weight string.
    HashMap<String, Double> WtNameToWtDouble = new HashMap<>();
    Double totalWeight = 0.0;
    Double totalMoment = 0.0;
}
