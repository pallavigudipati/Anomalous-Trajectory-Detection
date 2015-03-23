import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class iBATRunnable implements Runnable {
	private Thread t;
	private HashMap<String, Integer> iTree;
	private List<String> testTraj;
	private HashMap<String, HashMap<String, List<String>>> allTrajs;
	public boolean isDone = false;
	
	public iBATRunnable(List<String> traj, HashMap<String, HashMap<String, List<String>>> allTrajsIn) {
		testTraj = new ArrayList<String>(traj);
		allTrajs = new HashMap<String, HashMap<String, List<String>>>(allTrajsIn);
	}
	
	@Override
	public void run() {
		iTree = iBAT.Algo(testTraj, allTrajs);
		isDone = true;
	}
	
	public void start() {
		if (t == null) {
			t = new Thread (this);
		    t.start();
		}
	}
	
	public HashMap<String, Integer> getValue() {
		while (!isDone) {
			System.out.println("Not done");
		}
		return iTree;
	}
}
