import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

public class augmentPath {
	static int abs(int input) {
		if (input < 0)
			return -1 * input;
		else
			return input;
	}

	static int randRoundoff(float a) {
		Random random = new Random();
		if (random.nextInt() % 2 == 1) {
			return (int) Math.floor(a);
		} else {
			return (int) Math.ceil(a);
		}
	}
	
	static int randNum(){
		Random random = new Random();
		return random.nextInt();
	}

	public static void main(String[] args) {
		int traj1, pt1x, pt1y;
		int traj2, pt2x, pt2y;
		int ptx, pty;
		String optstring = null;
		
		
		try{
			Scanner fp = new Scanner(new FileReader("/host/Users/Pallavi/Desktop/Acads/"
										+ "sem-6/Data-Mining/Project/data/partition_cells/cells.txt"));//input file
			PrintWriter outfile = new PrintWriter("/host/Users/Pallavi/Desktop/Acads/"
										+ "sem-6/Data-Mining/Project/data/partition_cells/aug_cells.txt");//outfile
			traj1 = fp.nextInt();
			pt1x = fp.nextInt();
			pt1y = fp.nextInt();
			pt2x=pt2y=traj2=-1;
			
			outfile.print(traj1 + ", ");

			while (fp.hasNext()) {
				// update traj1 details into the database

				traj2 = fp.nextInt();
				pt2x = fp.nextInt();
				pt2y = fp.nextInt();
				
				if(traj1==traj2 && pt1x==pt2x && pt1y==pt2y){
					continue;
				}
				
				optstring = pt1x + " " + pt1y+ " ";
				outfile.print(optstring);

				// check if both points are of same trajectory
				if (traj1 == traj2) {
					ptx = pt1x;
					pty = pt1y;
					
					while(!(ptx==pt2x && pty==pt2y)){
						if(randNum()%2==0){
							//step horizontally
							if(ptx!=pt2x){
								if(pt2x > pt1x){
									ptx = ptx + 1;
								}else{
									ptx = ptx -1;
								}
								if(!(ptx==pt2x && pty==pt2y)){
									outfile.print(ptx+" "+pty+" ");
								}
							}
						}else{
							if(pty != pt2y){
								if(pt2y > pt1y){
									pty = pty +1;
								}else{
									pty = pty -1;
								}
								if(!(ptx==pt2x && pty==pt2y)){
									outfile.print(ptx+" "+pty+" ");
								}
							}
						}
					}
					
				} else {
					// if not just assign pt2 to pt1
					outfile.println();
					outfile.print(traj2+", ");
				}// end if-else(traj1 == traj2)
				
				traj1 = traj2;
				pt1x = pt2x;
				pt1y = pt2y;
			}// end while(fp.hasNext())
				// enter pt2 into the database
			if(pt2x != -1){
				outfile.print(pt2x + " "+ pt2y+" ");
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}