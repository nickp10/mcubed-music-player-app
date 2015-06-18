package dev.paddock.adp.mCubed.utilities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import dev.paddock.adp.mCubed.model.Progress;

public class ProgressManager {
	private static final List<Progress> progress = new ArrayList<Progress>();
	
	/**
	 * Prevents an external instance of a ProgressManager.
	 */
	private ProgressManager() { }
	
	private static Progress getProgress(String id) {
		Queue<List<Progress>> queue = new LinkedList<List<Progress>>();
		queue.offer(progress);
		while (!queue.isEmpty()) {
			List<Progress> curProgressList = queue.poll();
			for (Progress curProgress : curProgressList) {
				if (curProgress.getID().equals(id) && !curProgress.isStarted()) {
					return curProgress;
				} else {
					queue.offer(curProgress.getSubProgress());
				}
			}
		}
		return null;
	}
	
	public static Progress startProgress(String id, String title, String... subIDs) {
		return startProgress(id, title, false, subIDs);
	}
	
	public static Progress startProgress(String id, String title, boolean isBlocking, String... subIDs) {
		return startProgress(id, title, isBlocking, true, subIDs);
	}
	
	public static Progress startProgress(String id, String title, boolean isBlocking, boolean allowChildTitle, String... subIDs) {
		Progress progress = getProgress(id);
		if (progress == null) {
			progress = new Progress();
			ProgressManager.progress.add(progress);
		}
		progress.setID(id);
		progress.setTitle(title);
		progress.setSubIDs(subIDs);
		progress.setAllowChildTitle(allowChildTitle);
		progress.setIsBlocking(isBlocking);
		progress.start();
		return progress;
	}
	
	public static void endProgress(Progress progress) {
		if (progress != null) {
			progress.end();
			cleanup(progress);
		}
	}
	
	public static void cleanup(Progress progress) {
		ProgressManager.progress.remove(progress);
	}
}