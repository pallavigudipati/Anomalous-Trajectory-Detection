
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
 
// Operation on "partition" data.
public class GetLongLat {
 
    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
    	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
        	if (tokenizer.hasMoreTokens()) {
        		String trajectoryId = tokenizer.nextToken();
        		String longitude = tokenizer.nextToken();
        		String latitude = tokenizer.nextToken();
        		if (longitude.matches("0.0") || latitude.matches("0.0")) {
        			System.out.println(line);
        		}
        		context.write(new Text("longitude"), new Text(longitude));
        		context.write(new Text("latitude"), new Text(latitude));
        	}
        }
    } 
 
    public static class Reduce extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            double max = 0.0;
            double min = 1000.0;
            for (Text val : values) {
            	if (Double.compare(Double.parseDouble(val.toString()), max) > 0) {
            		max = Double.parseDouble(val.toString());
            	} 
            	if (Double.compare(min, Double.parseDouble(val.toString())) > 0) {
            		min = Double.parseDouble(val.toString());
            	}
            }
            context.write(key, new Text("max " + Double.toString(max)));
            context.write(key, new Text("min " + Double.toString(min)));
        }
    }
 
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
 
        Job job = new Job(conf, "GetLongLat");
        job.setJarByClass(GetLongLat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
 
        job.setMapperClass(Map.class);
      //  job.setCombinerClass(Reduce.class);
        job.setReducerClass(Reduce.class);
 
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
 
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
        job.waitForCompletion(true);
    }        
}

