package org.cloudcomputing.imageProcessing;

import com.google.gson.JsonElement;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.util.ToolRunner;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.opencv.core.*;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


public class FeatureMatching extends Configured implements Tool {


    private static final String HDFS_HOME = "/user/gathors";
    private static final String FEATURES = "/features";
    private static final String USER = "/user";
    private static final String INPUT = "/input";
    private static final String OUTPUT = "/output";
    private static final String LOCAL_USER_DIR = "/home/gathors/proj/v-opencv/user";
    private static final String PREFIX = "----------------------------------";

    private static final double THRESHOLD_FACTOR = 0.4;
    private static final double PERCENTAGE_THRESHOLD = 0.1;
    private static final IntWritable ONE = new IntWritable(1);


    private static String ID;

    static {System.load((new File("/home/gathors/proj/v-opencv/FeatureMatching/libs/libopencv_java2412.so")).getAbsolutePath());
            System.load((new File("/home/gathors/proj/v-opencv/FeatureMatching/libs/libopencv_highgui.so")).getAbsolutePath());}

    // Transform the json-type feature to mat-type
    public static Mat json2mat(String json) {

        JsonParser parser = new JsonParser();
        JsonElement parseTree = parser.parse(json);

        // Verify the input is JSON type
        if (!parseTree.isJsonObject()) {
            System.out.println("The input is not a JSON type...\nExiting...");
            System.exit(1);
        }
        JsonObject jobj = parser.parse(json).getAsJsonObject();

        int rows = jobj.get("rows").getAsInt();
        int cols = jobj.get("cols").getAsInt();
        int type = jobj.get("type").getAsInt();
        String data = jobj.get("data").getAsString();
        String[] pixs = data.split(",");

        Mat descriptor = new Mat(rows, cols, type);
        for (String pix : pixs) {
            String[] tmp = pix.split(" ");
            int r_pos = Integer.valueOf(tmp[0]);
            int c_pos = Integer.valueOf(tmp[1]);
            double rgb = Double.valueOf(tmp[2]);
            descriptor.put(r_pos, c_pos, rgb);
        }
        return descriptor;
    }

    public static void extractQueryFeatures2HDFS(String filename, Job job) throws IOException {

        // Read the local image.jpg as a Mat
        Mat query_mat_float = Highgui.imread(LOCAL_USER_DIR+ID+INPUT+"/"+filename, CvType.CV_32FC3);
        // Convert RGB to GRAY
        Mat query_gray = new Mat();
        Imgproc.cvtColor(query_mat_float, query_gray, Imgproc.COLOR_RGB2GRAY);
        // Convert the float type to unsigned integer(required by SIFT)
        Mat query_mat_byte = new Mat();
        query_gray.convertTo(query_mat_byte, CvType.CV_8UC3);
//        System.out.println("quert_mat_byte:"+query_mat_float);
        // Resize the image to 1/FACTOR both width and height
//        Mat query_mat_byte = FeatureExtraction.resize(query_mat_byte);
        // Extract the feature from the (Mat)image
        Mat query_features = FeatureExtraction.extractFeature(query_mat_byte);
//        query_features = FeatureExtraction.extractFeature(query_mat_float);

        System.out.println(PREFIX+"Extracting the query image feature...");
        System.out.println("query_mat(float,color):"+query_mat_float);
        System.out.println("query_mat(float,gray):"+query_gray);
        System.out.println("query_mat(byte,gray):"+query_mat_byte);
        System.out.println("query_mat_features:"+query_features);
        System.out.println();

        // Store the feature to the hdfs in order to use it later in different map tasks
        System.out.println(PREFIX+"Generating the feature file for the query image in HDFS...");
        FileSystem fs = FileSystem.get(job.getConfiguration());
        String featureFileName = filename.substring(0, filename.lastIndexOf("."))+".json";
        FSDataOutputStream fsDataOutputStream = fs.create(new Path(HDFS_HOME+USER+ID+INPUT+"/"+featureFileName));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fsDataOutputStream));
        bw.write(FeatureExtraction.mat2json(query_features));
        bw.close();
        System.out.println(PREFIX+"Query feature extraction finished...");
        System.out.println();
    }

    public static class FeatureMatchMapper extends Mapper<Text, Text, IntWritable, Text> {

        public void setup(Context context) {

            try {
                //System.setProperty("java.library.path", "/home/gathors/proj/libs");
                //System.loadLibrary(Core.NATIVE_xxx);
                //System.loacLibrary("/home/gathors/proj/libs/opencv-300.jar");
            } catch (UnsatisfiedLinkError e) {
                System.err.println("\nNATIVE LIBRARY failed to load...");
                System.err.println("ERROR:"+e);
                System.err.println("NATIVE_LIBRARY_NAME:"+Core.NATIVE_LIBRARY_NAME);
                System.err.println("#"+System.getProperty("java.library.path"));
                System.exit(1);
            }
        }

        public void map(Text key, Text value, Context context) throws IOException, InterruptedException {

            String filename = key.toString();
            String json = value.toString();
            if ( ! (filename.isEmpty() || json.isEmpty()) ) {

                // Change the json-type feature to Mat-type feature
                Mat descriptor = json2mat(json);

                // Read the query feature from the cache in Hadoop
                Mat query_features;
                String pathStr = context.getConfiguration().get("featureFilePath");
                FileSystem fs = FileSystem.get(context.getConfiguration());
                FSDataInputStream fsDataInputStream = fs.open(new Path(pathStr));
                Scanner sc = new Scanner(fsDataInputStream, "UTF-8");
                StringBuilder sb = new StringBuilder();
                while(sc.hasNextLine()) {
                    sb.append(sc.nextLine());
                }
                String query_json = sb.toString();
                fsDataInputStream.close();
//                System.out.println("query_json:"+query_json);
                query_features = json2mat(query_json);

                // Get the similarity of the current database image against the query image
                DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
                MatOfDMatch matches = new MatOfDMatch();
                System.out.println("current_record_feature:" + descriptor + "\nquery_feature:" + query_features);

                // Ensure the two features have same length of cols (the feature extracted are all 128 cols(at least in this case))
                if (query_features.cols() == descriptor.cols()) {

                    matcher.match(query_features, descriptor, matches);
                    DMatch[] dMatches = matches.toArray();

                    // Calculate the max/min distances
//                    double max_dist = Double.MAX_VALUE;
//                    double min_dist = Double.MIN_VALUE;
                    double max_dist = 0;
                    double min_dist = 100;
                    for (int i = 0; i < dMatches.length; i++) {
                        double dist = dMatches[i].distance;
                        if (min_dist > dist) min_dist = dist;
                        if (max_dist < dist) max_dist = dist;
                    }
                    System.out.println("min_dist of keypoints:" + min_dist + "  max_dist of keypoints:" + max_dist);
                    // Only distances ≤ threshold are good matches
                    double threshold = max_dist * THRESHOLD_FACTOR;
//                    double threshold = min_dist * 2;
                    LinkedList<DMatch> goodMatches = new LinkedList<DMatch>();

                    for (int i = 0; i < dMatches.length; i++) {
                        if (dMatches[i].distance <= threshold) {
                            goodMatches.addLast(dMatches[i]);
                        }
                    }

                    // Get the ratio of good_matches to all_matches
                    double ratio = (double) goodMatches.size() / (double) dMatches.length;
                    System.out.printf("current_record_filename: %s --- ratio: %f   total_matches: %d    good_matches: %d\n",
                            filename, ratio, dMatches.length, goodMatches.size());
//                    System.out.println("type:" + descriptor.type() + " channels:" + descriptor.channels() + " rows:" + descriptor.rows() + " cols:" + descriptor.cols());
//                    System.out.println("qtype:" + query_features.type() + " qchannels:" + query_features.channels() + " qrows:" + query_features.rows() + " qcols:" + query_features.cols());
                    System.out.println();

                    if (ratio > PERCENTAGE_THRESHOLD) {
                        // Key:1        Value:filename|ratio
                        context.write(ONE, new Text(filename + "|" + ratio));
//                        context.write(ONE, new Text(filename + "|" + String.valueOf(goodMatches.size())));
                    }
                } else {
                    System.out.println("The size of the features are not equal");
                }
            }
        }
    }

    // We need the reducer to collect all the results from different map tasks of the same query_image
    public static class FeatureMatchReducer extends Reducer<IntWritable, Text, Text, DoubleWritable> {

        public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

            System.out.println(PREFIX+"Collecting all the matched results");
            for (Text val : values) {
                String[] tmp = val.toString().split("\\|");     //  The \\ here is very important. Cannot use "|" since split()
                                                                //  need a regex(regular expression), and the vertical bar is
                                                                //  special character.
                System.out.println("filename:"+tmp[0]+" ratio:"+tmp[1]);
                String filename = tmp[0];
                double ratio = Double.valueOf(tmp[1]);

                // Key:filename     Value:ratio
                context.write(new Text(filename), new DoubleWritable(ratio));
            }
        }
    }

    // This Filter is not working
//    class MyPathFilter implements PathFilter {
//
//        @Override
//        public boolean accept(Path path) {
//            return path.toString().contains("_SUCCESS") ? false : true;
//        }
//    }

    public int run(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        if (args.length != 2) {
            System.out.println("Usage: FeatureMatching ID <inputName.jpeg/inputName.jpg>");
            System.exit(1);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("", Locale.US);
        sdf.applyPattern("yyyy-MM-dd_HH-mm-ss");
        String time = sdf.format(new Date());

        Job job = Job.getInstance();

        ID = "/"+args[0];
        String filename = args[1];
        filename = filename.toLowerCase();
        System.out.println("current filename:"+filename);

        // Detect illegal username (if the username dir doesn't exist)
        File userPath = new File(LOCAL_USER_DIR+ID);
        if (!userPath.exists()) {
            System.out.println("Unauthorized username!!!\nExiting......");
            System.exit(1);
        }
        // Preprocess the input image.jpg from local dir: /local.../user/ID/input/image.jpg
        // Save the features to local dir: hdfs://.../user/ID/input/image.jpg
        extractQueryFeatures2HDFS(filename, job);

        // Add the feature file to the hdfs cache
        String featureFileName = filename.substring(0, filename.lastIndexOf("."))+".json";
//        job.addCacheFile(new Path(HDFS_HOME + USER + ID + INPUT + "/" + featureFileName).toUri());
        job.getConfiguration().set("featureFilePath", HDFS_HOME+USER+ID+INPUT+"/"+featureFileName);

        // Check the file type. Only support jpeg/jpg type images
        String type = filename.substring(args[1].lastIndexOf("."));
        if (!(type.equals(".jpg") || type.equals(".jpeg"))) {
            System.out.println("Image type not supported!!!\nExiting");
            System.exit(1);
        }

        // Input: hdfs://.../features/
        // The feature dir is a location of all features extracted from the database
        String inputPathStr = HDFS_HOME+FEATURES;
        // Output: hdfs://.../user/ID/output/
        String outputPathStr = HDFS_HOME+USER+ID+OUTPUT+"/"+time;

        job.setInputFormatClass(KeyValueTextInputFormat.class);
//        job.setOutputFormatClass(TextOutputFormat.class);

        // Get the lists of all feature files: /.../features/data/part-*
        FileSystem fs = FileSystem.get(job.getConfiguration());
        FileStatus[] statuses = fs.listStatus(new Path(inputPathStr));
        StringBuffer sb = new StringBuffer();
        for (FileStatus fileStatus : statuses) {
            sb.append(fileStatus.getPath()+",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));

        job.setJarByClass(FeatureMatching.class);
        job.setMapperClass(FeatureMatchMapper.class);
        job.setReducerClass(FeatureMatchReducer.class);

        // only need one reducer to collect the result
        job.setNumReduceTasks(1);

        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        // Input a directory, so need the recursive input
        FileInputFormat.setInputDirRecursive(job, true);
        // Set the PathFilter, to omit _SUCCESS files
        // (This is not working correctly, as the PathFilter class is an interface rather than a class.
        // But the 2nd arg asks me to extend the PathFilter)
//        FileInputFormat.setInputPathFilter(job, MyPathFilter.class);
//
//        FileInputFormat.setInputPaths(job, new Path(inputPathStr));
        FileInputFormat.setInputPaths(job, sb.toString());
        FileOutputFormat.setOutputPath(job, new Path(outputPathStr));

        boolean success =  job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        if (ToolRunner.run(new FeatureMatching(), args) == 1) {
            System.out.println(".......Feature Match failure........");
            System.exit(1);
        }
        System.exit(0);
    }
}


