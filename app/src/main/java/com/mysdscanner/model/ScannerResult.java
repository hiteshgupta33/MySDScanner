package com.mysdscanner.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Created by hiteshgupta on 8/10/16.
 */

@Data
public class ScannerResult implements Parcelable {

    private int scanError;

    private List<Map.Entry<String, Long>> lstBiggestFiles;

    private List<Map.Entry<String, Long>> lstFrequentFiles;

    private double averageSize;

    public ScannerResult() {
    }

    public ScannerResult(Parcel in) {
        scanError = in.readInt();
        lstBiggestFiles = in.readArrayList(null);
        lstFrequentFiles = in.readArrayList(null);
        averageSize = in.readDouble();
    }

    public static final Creator<ScannerResult> CREATOR = new Creator<ScannerResult>() {
        @Override
        public ScannerResult createFromParcel(Parcel in) {
            return new ScannerResult(in);
        }

        @Override
        public ScannerResult[] newArray(int size) {
            return new ScannerResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(scanError);
        parcel.writeList(lstBiggestFiles);
        parcel.writeList(lstFrequentFiles);
        parcel.writeDouble(averageSize);
    }
}
