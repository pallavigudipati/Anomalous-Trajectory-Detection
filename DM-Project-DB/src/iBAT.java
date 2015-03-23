import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jfree.data.xy.XYSeries;


public class iBAT {
	public static int numIter = 100;
	public static iBATDB db = new iBATDB();
	// public static List<XYSeries> plotSeries = new ArrayList<XYSeries>();
	public static HashMap<String, List<List<String>>> dumpPlot = new HashMap<String, List<List<String>>>(); 
	public static void main(String[] args) {
		db.SetUp();
		// Get test trajectory.
		String testTrajId = new String("253243");
		List<String> testTraj = db.Query("Trajectory", testTrajId);
		
		System.out.println("testTraj " + testTraj);
		List<String> testTrajPoints = GetPoints(testTraj.get(0));
		
		String src = new String(testTrajPoints.get(0));
		String dst = new String(testTrajPoints.get(testTrajPoints.size() - 1));
		System.out.println("src " + src);
		System.out.println("dst " + dst);
		List<String> relTrajs = GetCommonTraj(src, dst);
		System.out.println("common traj " + relTrajs);
		
		// --------------- Got all the trajectories req. Now have to run the algo----------------------
	
		HashMap<String, HashMap<String, List<String>>> allTrajs = GetInfoTrajectories(relTrajs);
		System.out.println("Got all relevant traj");
		/*
		// TODO Refine!!!!
		XYSeries testSeries = new XYSeries("TestTraj");
		for (int i = 0; i < testTrajPoints.size(); ++i) {
			String[] parts = testTrajPoints.get(i).split(" ");
			testSeries.add(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
		}
		plotSeries.add(testSeries);
		PlotFreeChart.Run(plotSeries);
		*/
		HashMap<String, Double> scores = ParallelAlgo(testTrajPoints, allTrajs);
		System.out.println(scores);
		// System.out.println(dumpPlot);
		try {
			File file = new File("dump2.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(dumpPlot.toString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		db.TearDown();
	}
	
	public static List<String> GetPoints(String traj) {
		String[] trajParts = traj.split(" ");
		List<String> ret = new ArrayList<String>();
		String point = new String();
		for (int i = 0; i < trajParts.length; ++i) {
			if (i % 2 == 0) {
				point = new String(trajParts[i]);
			} else {
				point += " " + trajParts[i];
				ret.add(point);
			}
		}
		return ret;
	}
	
	public static List<String> GetCommonTraj(String src, String dst) {
		List<String> ret = new ArrayList<String>();
		List<String> srcTrajs = db.Query("Grid", src);
		List<String> dstTrajs = db.Query("Grid", dst);
		// System.out.println(srcTrajs.get(0));
		HashMap<String, List<String>> srcMap = ExtractTrajectories(srcTrajs.get(0));
		HashMap<String, List<String>> dstMap = ExtractTrajectories(dstTrajs.get(0));
		Set<String> srcKeys = srcMap.keySet();
		Iterator<String> it = srcKeys.iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (dstMap.containsKey(key)) {
				// For multiple positions, allowing any combo of positions.
				List<String> srcPos = srcMap.get(key);
				List<String> dstPos = dstMap.get(key);
				for (int i = 0; i < srcPos.size(); ++i) {
					for (int j = 0; j < dstPos.size(); ++j) {
						if (Integer.parseInt(srcPos.get(i)) < Integer.parseInt(dstPos.get(j))) {
							i = srcPos.size(); // to break
							ret.add(key);
							break;
						}
					}
				}
			}
		}
		return ret;
	}
	
	public static HashMap<String, HashMap<String, List<String>>> GetInfoTrajectories(List<String> trajs) {
		HashMap<String, HashMap<String, List<String>>> ret = new HashMap<String, HashMap<String, List<String>>>();
		for (int i = 0; i < trajs.size(); ++i) {
			List<String> singleTrajRaw = db.Query("Trajectory", trajs.get(i));
			List<String> singleTrajPoints = GetPoints(singleTrajRaw.get(0));
			List<List<String>> pointsScore = new ArrayList<List<String>>();
			pointsScore.add(singleTrajPoints);
 			dumpPlot.put(trajs.get(i), pointsScore);
			// For plots
			/*
			List<String> singleTrajPoints = GetPoints(singleTrajRaw.get(0));
			XYSeries relSeries = new XYSeries("RelTraj" + Integer.toString(i));
			for (int j = 0; j < singleTrajPoints.size(); ++j) {
				String[] parts = singleTrajPoints.get(j).split(" ");
				relSeries.add(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
			}
			plotSeries.add(relSeries);
			*/
			HashMap<String, List<String>> singleTraj = ExtractPoints(singleTrajRaw.get(0));
			// System.out.println("singleTraj " + singleTraj);
			ret.put(trajs.get(i), singleTraj);
		}
		return ret;
	}
	
	// point, positions
	public static HashMap<String, List<String>> ExtractPoints(String traj) {
		HashMap<String, List<String>> ret = new HashMap<String, List<String>>();
		List<String> points = GetPoints(traj);
		for (int i = 0; i < points.size(); ++i) {
			String key = points.get(i);
			if (ret.containsKey(key)) {
				List<String> old = ret.get(key);
				// TODO check position from 0 or 1
				old.add(Integer.toString(i));
				ret.put(key, old);
			} else {
				List<String> newl = new ArrayList<String>();
				newl.add(Integer.toString(i));
				ret.put(key, newl);
			}
		}
		return ret;
	}
	
	// List<String> because one point can occur at multiple positions in a trajectory. 
	public static HashMap<String, List<String>> ExtractTrajectories(String input) {
		String csv = new String(input);
		csv = csv.replace('(', ' ');
		csv = csv.replace(')', ',');
		String[] trajParts = csv.split(",");
		for (int i = 0; i < trajParts.length - 1; ++i) {
			trajParts[i] = trajParts[i].trim();
		}
		
		HashMap<String, List<String>> ret = new HashMap<String, List<String>>();
		for (int i = 0; i < trajParts.length - 1; ++i) {
			String[] split = trajParts[i].split(" ");
			if (ret.containsKey(split[0])) {
				List<String> oldList = ret.get(split[0]);
				oldList.add(split[1]);
				ret.put(split[0], oldList);
			} else {
				List<String> newList = new ArrayList<String>();
				newList.add(split[1]);
				ret.put(split[0], newList);
			}
		}
		
		return ret;
	}
	
	// removes trajs that do not pass through the given cell.
	public static List<List<String>> SetMinus(HashMap<String, HashMap<String, List<String>>> allTrajs, 
											 List<String> currentTrajs, String cell) {
		List<List<String>> ret = new ArrayList<List<String>>();
		List<String> removed = new ArrayList<String>();
		List<String> retained = new ArrayList<String>();
		
		for (int i = 0; i < currentTrajs.size(); ++i) {
			String key = currentTrajs.get(i);
			if (allTrajs.get(key).containsKey(cell)) {
				retained.add(key);
			} else {
				removed.add(key);
			}
		}
		ret.add(0, removed);
		ret.add(1, retained);
		// System.out.println("cell " + cell);
		// System.out.println("removed " + removed);
		// System.out.println("retained " + retained);
		return ret;
	}
	
	// Not doing any sub-sampling as of now.
	public static HashMap<String, Integer> Algo(List<String> testTraj, 
												HashMap<String, HashMap<String, List<String>>> trajs) {
		HashMap<String, Integer> iTree = new HashMap<String, Integer>();
		List<String> shuffledTraj = new ArrayList<String>(testTraj);
		Collections.shuffle(shuffledTraj);
		List<String> currentTrajs = new ArrayList<String>(trajs.keySet());

		for (int i = 0; i < shuffledTraj.size(); ++i) {
			List<List<String>> branch = SetMinus(trajs, currentTrajs, shuffledTraj.get(i));
			// System.out.println("Current size " + currentTrajs.size() + " removing " + branch.get(0).size());
			// removed
			for (int j = 0; j < branch.get(0).size(); ++j) {
				iTree.put(branch.get(0).get(j), i);
				// System.out.println("Setting iTree");
			}
			currentTrajs = new ArrayList<String>(branch.get(1));
			if (currentTrajs.isEmpty()) {
				break;
			}
			// TODO check the below stuff
			if (i == shuffledTraj.size() - 1) {
				System.out.println("size " + currentTrajs.size());
				System.out.println((int)Math.ceil(Math.log(currentTrajs.size()) + 1));
				for (int j = 0; j < currentTrajs.size(); ++j) {
					iTree.put(currentTrajs.get(j), (int)Math.ceil(Math.log(currentTrajs.size()) + 1));
				}
			}
		}
		// System.out.println("iTree size " + iTree.size() + " trajs size " + trajs.size());
		return iTree;
	}
	
	public static HashMap<String, Double> GetAnomalyScores(HashMap<String, Integer> sumLengths) {
		System.out.println("In GetAnomalyScores");
		Set<String> keys = sumLengths.keySet();
		Iterator<String> it = keys.iterator();
		HashMap<String, Double> scores = new HashMap<String, Double>();
		
		int N = keys.size();
		double H = Math.log(N - 1) + 0.57721566;
		double c = (2 * H) - ((2 * (N - 1)) / N);
		
		while (it.hasNext()) {
			String key = it.next();
			double avgLength = (double)sumLengths.get(key) / (double)numIter;
			// System.out.println(avgLength + " " + c + " " + avgLength/c);
			double score = Math.pow(2, -(avgLength / c));
			scores.put(key, score);
			
			// dump
			List<String> scoreList = new ArrayList<String>();
			scoreList.add(Double.toString(score));
			List<List<String>> old = dumpPlot.get(key);
			old.add(scoreList);
			dumpPlot.put(key, old);
		}
		return scores;
	}
	
	public static HashMap<String, Double> ParallelAlgo(List<String> testTraj, 
												HashMap<String, HashMap<String, List<String>>> trajs) {
		System.out.println("In parallel algo");
		HashMap<String, Integer> sumLengths = new HashMap<String, Integer>();
		List<iBATRunnable> threads = new ArrayList<iBATRunnable>();
		for (int i = 0; i < numIter; ++i) {
			iBATRunnable thread = new iBATRunnable(testTraj, trajs);
			threads.add(thread);
			thread.run();
		}
		// Hopefully all the threads are running. Naive way to recover stuff from thread.
		Set<String> keys = trajs.keySet();
		for (int i = 0; i < numIter; ++i) {
			HashMap<String, Integer> threadScore = threads.get(i).getValue();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (threadScore.get(key) == null) {
					System.out.println("iTree has nulls");
				}
				if (sumLengths.containsKey(key)) {
					sumLengths.put(key, sumLengths.get(key) + threadScore.get(key));
				} else {
					sumLengths.put(key, threadScore.get(key));
				}
			}
		}
		
		// ---- Haven't taken the average yet...its just the sum of all its positions.
		System.out.println("sumLengths " + sumLengths);
		HashMap<String, Double> scores = GetAnomalyScores(sumLengths);
		return scores;
	}
}
