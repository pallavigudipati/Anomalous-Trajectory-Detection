
import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Partition {
	public static int cutOff = 6 * 60;
	public static int trajectoryId = 0;
	// Put some limits to remove unclean data
	public static double longMax = 120.0;
	public static double longMin = 110.0;
	public static double latMax = 45.0;
	public static double latMin = 35.0;
	// Putting limits for cells.
	public static double longEps = 0.00508;
	public static double latEps = 0.00359;
	// P2 - P1 : Assuming data collected in a week.
	public static int TimeDiff(String date1, String time1, String date2, String time2) {
		int diff = 0;
		String[] partsDate1 = date1.split("-");
		String[] partsDate2 = date2.split("-");
		String[] partsTime1 = time1.split(":");
		String[] partsTime2 = time2.split(":");
		
		diff = (24 * 60 * 60 * (Integer.parseInt(partsDate2[2]) - Integer.parseInt(partsDate1[2])))
				+ (60 * 60 * (Integer.parseInt(partsTime2[0]) - Integer.parseInt(partsTime1[0])))
				+ (60 * (Integer.parseInt(partsTime2[1]) - Integer.parseInt(partsTime1[1])))
				+ (Integer.parseInt(partsTime2[2]) - Integer.parseInt(partsTime1[2]));
		
		return diff;
	}
	
	public static String GetCell(Double longitude, Double latitude) {
		int x = (int) Math.floor((longitude - longMin) / longEps);
		int y = (int) Math.floor((latitude - latMin) / latEps);
		return new String(x + " " + y);
	}
	
    public static class Map extends Mapper<LongWritable, Text, Text, Text> {

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line, " ,");
            if (tokenizer.hasMoreTokens()) {
            	String taxiId = tokenizer.nextToken();
            	String date = tokenizer.nextToken();
            	String time = tokenizer.nextToken();
            	// TODO Check which is lat and which is long
            	String longitude = tokenizer.nextToken();
            	String latitude = tokenizer.nextToken();
            	double lon = Double.parseDouble(longitude);
            	double lat = Double.parseDouble(latitude);
            	
            	if (Double.compare(longMax, lon) > 0 && Double.compare(lon, longMin) > 0 
            		&& Double.compare(latMax, lat) > 0 && Double.compare(lat, latMin) > 0) {
            		// System.out.println(lon + " " + lat);
            		context.write(new Text(taxiId), new Text(date + " " + time + " " + longitude + " " + latitude));
            	}
            }
        }
    } 
 
    public static class Reduce extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            List<String> array = new ArrayList<String>();
        	for (Text val : values) {
        		array.add(val.toString());
            }
        	Collections.sort(array, new Comparator<String>() {
        		@Override
        		public int compare(String str1, String str2) {
        			String[] parts1 = str1.split(" ");
        			String[] parts2 = str2.split(" ");
        			int diff = TimeDiff(parts1[0], parts1[1], parts2[0], parts2[1]);
        			if (diff < 0) {
        				return 1;
        			} else if (diff > 0) {
        				return -1;
        			} else {
        				return 0;
        			}
        		}
			});
        	List<String> trajPoints = new ArrayList<String>();
        	String prevCell = new String();
        	for (int i = 1; i < array.size(); ++i) {
        		String[] parts = array.get(i).split(" ");
        		String[] parts_prev = array.get(i - 1).split(" ");
        		int diff = TimeDiff(parts_prev[0], parts_prev[1], parts[0], parts[1]);
        		if (diff > cutOff) {
        			if (trajPoints.size() > 1) {
        				context.write(new Text(Integer.toString(trajectoryId)), new Text(trajPoints.toString()));
        			}
        			trajPoints.clear();
        			prevCell = new String();
        			trajectoryId++;
        		}
        		// Removing repetitive data
        		if (diff != 0) {
        			String coords = GetCell(Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
        			if (prevCell.isEmpty() || !prevCell.equals(coords)) {  
        				trajPoints.add(coords);
        			}
        		}
        	}
        }
    }
 
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
 
        Job job = new Job(conf, "Partition");
        job.setJarByClass(Partition.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
 
        job.setMapperClass(Map.class);
        //job.setCombinerClass(Reduce.class);
        job.setReducerClass(Reduce.class);
 
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
 
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
        job.waitForCompletion(true);
    }        
}

