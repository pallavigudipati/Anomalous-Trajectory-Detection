
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
 
public class InvertIndex {
 
    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        	String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line, " ");
        	int i = 1;
        	int skip = 0;
        	String cell = new String();
        	String trajNo = tokenizer.nextToken();
        	trajNo = trajNo.replaceAll(",","");
            while (tokenizer.hasMoreTokens()) {
            	if (skip % 2 == 0) {
            		cell = tokenizer.nextToken();
            	} else {
            		cell += " " + tokenizer.nextToken();
            		context.write(new Text(cell), new Text("(" + trajNo + " " + i + ")"));
            		i++;
            	}
            	skip++;
            }
        }
    } 
 
    public static class Reduce extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            String allOccur = new String();
            for (Text val : values) {
            	allOccur += val.toString();
            }
            context.write(new Text(key.toString() + ","), new Text(allOccur));
        }
    }
 
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
 
        Job job = new Job(conf, "InvertIndex");
        job.setJarByClass(InvertIndex.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
 
        job.setMapperClass(Map.class);
        // job.setCombinerClass(Reduce.class);
        job.setReducerClass(Reduce.class);
 
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
 
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
 
        job.waitForCompletion(true);
    }        
}

