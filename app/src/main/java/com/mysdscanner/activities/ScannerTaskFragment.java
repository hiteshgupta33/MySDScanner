package com.mysdscanner.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.mysdscanner.Utils.Constants;
import com.mysdscanner.model.ScannerResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple retainer fragment to perform background task for scanning external storage directory
 * {@link ScannerTaskFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */

/**
 * Created by hiteshgupta on 8/10/16.
 */
public class ScannerTaskFragment extends Fragment {

	private SDScannerTask scannerTask;

	private OnFragmentInteractionListener mListener;

	private ScannerResult scanResult;

	/**
	 * Interface for communicating result back to the host activity.
	 */
	public interface OnFragmentInteractionListener {
		void onScanComplete(ScannerResult result);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		startScanTask();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	private class SDScannerTask extends AsyncTask<Void, Void, ScannerResult> {

		private Map<String, Long> frequentFileExtensions = new HashMap<>();

		private double totalFileSize = 0d;

		@Override
		protected ScannerResult doInBackground(Void... voids) {
			Log.v(Constants.TAG, "Scanner Task Started");
			return getExternalStorageFiles();
		}

		@Override
		protected void onPostExecute(ScannerResult result) {
			super.onPostExecute(result);
			scanResult = result;
			if (mListener != null) {
				mListener.onScanComplete(result);
			}
			Log.v(Constants.TAG, "Scanner Task Finished");
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			Log.v(Constants.TAG, "Scanner Task Cancelled");
		}

		private ScannerResult getExternalStorageFiles() {
			ScannerResult scannerResult = new ScannerResult();

			String status = Environment.getExternalStorageState();

			if (Environment.MEDIA_MOUNTED.equals(status) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(status)) {
				Map<String, Long> storageFilesMap = scanExternalStorage(Environment.getExternalStorageDirectory());

				if (!storageFilesMap.isEmpty()) {
					List<Map.Entry<String, Long>> lstStorageFiles = new ArrayList<>(storageFilesMap.entrySet());

					// Sort files collection from highest file size to lowest
					Collections.sort(lstStorageFiles, Collections.reverseOrder(new Comparator<Map.Entry<String, Long>>() {
						@Override
						public int compare(Map.Entry<String, Long> leftEntry, Map.Entry<String, Long> rightEntry) {
							return leftEntry.getValue().compareTo(rightEntry.getValue());
						}
					}));

					scannerResult.setLstBiggestFiles(lstStorageFiles.size() > 10 ? lstStorageFiles.subList(0, Constants.NUMBER_OF_BIGGEST_FILES) : lstStorageFiles);
					scannerResult.setAverageSize(totalFileSize / lstStorageFiles.size());

					if (frequentFileExtensions != null && !frequentFileExtensions.isEmpty()) {
						lstStorageFiles = new ArrayList<>(frequentFileExtensions.entrySet());

						// Sort files collection from highest file size to lowest
						Collections.sort(lstStorageFiles, Collections.reverseOrder(new Comparator<Map.Entry<String, Long>>() {
							@Override
							public int compare(Map.Entry<String, Long> leftEntry, Map.Entry<String, Long> rightEntry) {
								return leftEntry.getValue().compareTo(rightEntry.getValue());
							}
						}));

						scannerResult.setLstFrequentFiles(lstStorageFiles.size() > 10 ? lstStorageFiles.subList(0, Constants.NUMBER_OF_FREQUENT_FILES) : lstStorageFiles);
					}
				} else {
					scannerResult.setScanError(Constants.ERROR_STORAGE_EMPTY);
				}
			} else {
				scannerResult.setScanError(Constants.ERROR_STORAGE_NOT_READABLE);
			}

			return scannerResult;
		}

		private Map<String, Long> scanExternalStorage(File file) {
			Map<String, Long> storageFiles = new LinkedHashMap<>();

			if (file != null) {
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					for (File storedFile : files) {
						storageFiles.putAll(scanExternalStorage(storedFile));
					}
				} else {
					storageFiles.put(file.getName(), file.length());
					totalFileSize += (double) file.length();

					extractFileExtensions(file);
				}
			}

			return storageFiles;
		}

		private void extractFileExtensions(File file) {
			String fileExtension = null;
			String[] splitter = file.getName().split("\\.");

			if (splitter != null && splitter.length > 1) {
				fileExtension = splitter[splitter.length - 1];
			}

			if (fileExtension != null) {
				long value = frequentFileExtensions.containsKey(fileExtension) ? frequentFileExtensions.get(fileExtension) : 0;
				frequentFileExtensions.put(fileExtension, value + 1);
			}
		}
	}

	public void cancelScanTask() {
		scannerTask.cancel(true);
		scannerTask = null;
	}

	public void startScanTask() {
		scannerTask = new SDScannerTask();
		scannerTask.execute();
	}

	public ScannerResult getScanResult() {
		return scanResult;
	}
}
