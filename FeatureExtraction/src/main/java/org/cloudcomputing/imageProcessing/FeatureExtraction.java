package org.cloudcomputing.imageProcessing;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobCounter;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.ToolRunner;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

import org.hipi.image.FloatImage;
import org.hipi.image.HipiImageHeader;
import org.hipi.image.PixelArray;
import org.hipi.imagebundle.mapreduce.HibInputFormat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;


public class FeatureExtraction extends Configured implements Tool {


    public static final String HDFS_HOME = "/user/gathors";
    public static final String FEATURES = "/features";
    public static final String ADMIN = "/admin";
    public static final String INPUT = "/input";

    public static final int FACTOR = 10;

    private static int[][] openCVTypeLUT = new int[][]
            {{CvType.CV_8UC1, CvType.CV_8UC2, CvType.CV_8UC3, CvType.CV_8UC4},
             {CvType.CV_16UC1, CvType.CV_16UC2, CvType.CV_16UC3, CvType.CV_16UC4},
             {CvType.CV_16SC1, CvType.CV_16SC2, CvType.CV_16SC3, CvType.CV_16SC4},
             {CvType.CV_32SC1, CvType.CV_32SC2, CvType.CV_32SC3, CvType.CV_32SC4},
             {CvType.CV_32FC1, CvType.CV_32FC2, CvType.CV_32FC3, CvType.CV_32FC4},
             {CvType.CV_64FC1, CvType.CV_64FC2, CvType.CV_64FC3, CvType.CV_64FC4}};

    public static int generateOpenCVType(int pixelArrayDataType, int numBands) {

        int depthIndex = -1;
        switch(pixelArrayDataType) {
            case PixelArray.TYPE_BYTE:
                depthIndex = 0;
                break;
            case PixelArray.TYPE_USHORT:
                depthIndex = 1;
                break;
            case PixelArray.TYPE_SHORT:
                depthIndex = 2;
                break;
            case PixelArray.TYPE_INT:
                depthIndex = 3;
                break;
            case PixelArray.TYPE_FLOAT:
                depthIndex = 4;
                break;
            case PixelArray.TYPE_DOUBLE:
                depthIndex = 5;
                break;
            default:
                break;
        }

        int channelIndex = numBands - 1;

        if(depthIndex < 0 || depthIndex >= openCVTypeLUT.length) {
            return -1;
        }
        if(channelIndex < 0 || channelIndex >= openCVTypeLUT[0].length) {
            return -1;
        }

        return openCVTypeLUT[depthIndex][channelIndex];
    }

    public static Mat floatImage2OpenCVMat(FloatImage floatImage) {
        int w = floatImage.getWidth();
        int h = floatImage.getHeight();

        // pixleArrayDataType: float        numBands: 3         openCVType: CV_32FC3
        float[] pix = floatImage.getData();

        // the SIFT need the type to be CV_8U, so we use int rather than float
        float[] rgb = new float[3];
        Mat mat = new Mat(h, w, CvType.CV_32FC3);

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int start = i*w*3 + j*3;
                // CV_8UC3 value from 0-255, CV_32FC3 value from 0.0-1.0, so have to scale it
                rgb[0] = pix[start + 0] * 255.0f; // R
                rgb[1] = pix[start + 1] * 255.0f; // G
                rgb[2] = pix[start + 2] * 255.0f; // B
                mat.put(i, j, rgb);
            }
        }
        return mat;
    }

    public static Mat extractFeature(Mat image) {

        Mat descriptor = new Mat();
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        // Detect the keypoints
//        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SIFT);
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.SIFT);
        featureDetector.detect(image, keyPoints);
        // Extract the features as descriptor
//        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        descriptorExtractor.compute(image, keyPoints, descriptor);
        System.out.println("image to extract:"+image);
        System.out.println("keypoints:"+keyPoints);
        System.out.println("descriptor:"+descriptor);

        return descriptor;
    }

    public static String mat2json(Mat mat) {
        JsonObject jobj = new JsonObject();

        if (mat.isContinuous()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < mat.rows(); i++) {
                for (int j = 0; j < mat.cols(); j++) {
                    builder.append(i+" ");
                    builder.append(j+" ");
                    double[] pix = mat.get(i, j);
                    builder.append(pix[0]+",");
                }
            }
            builder.deleteCharAt(builder.lastIndexOf(","));

            jobj.addProperty("rows", mat.rows());
            jobj.addProperty("cols", mat.cols());
            jobj.addProperty("type", mat.type());
            jobj.addProperty("data", builder.toString());
        }

        Gson gson = new Gson();
        return gson.toJson(jobj);
    }

    // Resize the Mat to 1/FACTOR
    public static Mat resize(Mat image) {
        int rows = image.rows() / FACTOR;
        int cols = image.cols() / FACTOR;
        Mat resizeImage = new Mat(rows, cols, image.type());
        Imgproc.resize(image, resizeImage, resizeImage.size());
        return resizeImage;
    }

    public static class FeatureExtractMapper extends Mapper<HipiImageHeader, FloatImage, Text, Text> {

        public void setup(Context context) {
            try {
            //System.setProperty("java.library.path", "/home/gathors/proj/libs");
            //System.loadLibrary(Core.NATIVE_xxx);
            //System.loacLibrary("/home/gathors/proj/libs/opencv-300.jar");
            System.load((new File("/home/gathors/proj/v-opencv/FeatureExtraction/libs/libopencv_java2412.so")).getAbsolutePath());
            } catch (UnsatisfiedLinkError e) {
                System.err.println("\nNATIVE LIBRARY failed to load...");
                System.err.println("ERROR:"+e);
                System.err.println("NATIVE_LIBRARY_NAME:"+Core.NATIVE_LIBRARY_NAME);
                System.err.println("#"+System.getProperty("java.library.path"));
                System.exit(1);
            }
        }

        public void map(HipiImageHeader key, FloatImage value, Context context) throws IOException, InterruptedException {
            if (value != null && value.getWidth() > 1 && value.getHeight() > 1 && value.getNumBands() == 3) {
                // CV_<bit-depth>{U|S|F}C(<number_of_channels>)
                // U:unsigned integer, S:signed integer, F:float

                // Transform the (FloatImage)image to (Mat)image
                Mat image_float = floatImage2OpenCVMat(value);
                // Convert RGB to GRAY
                Mat image_gray = new Mat();
                Imgproc.cvtColor(image_float, image_gray, Imgproc.COLOR_RGB2GRAY);
                // Convert the float type to unsigned integer(required by SIFT)
                Mat image_byte = new Mat();
                image_gray.convertTo(image_byte, CvType.CV_8UC3);
                // Resize the image to 1/FACTOR
//                image_byte = resize(image_byte);
                // Extract the (Mat)feature from the given (Mat)image
                Mat descriptor = extractFeature(image_byte);

                String filename = key.getMetaData("filename");

                // Key: filename            Value: json-type feature(String)
                context.write(new Text(filename), new Text(mat2json(descriptor) + "\n"));

                System.out.println("filename:"+filename);
                System.out.println("mat(float,color):"+image_float);
                System.out.println("mat(float,gray):" + image_gray);
                System.out.println("mat(byte,gray):"+image_byte);
                System.out.println("mat_features:" + descriptor);
                System.out.println();
            }
        }
    }

    public int run(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length != 1) {
            System.out.println("Usage: FeatureExtraction <inputName.hib>");
            System.exit(1);
        }


        SimpleDateFormat sdf = new SimpleDateFormat("", Locale.US);
        sdf.applyPattern("yyyy-MM-dd_HH-mm-ss");
        String time = sdf.format(new Date());

        Job job = Job.getInstance();

        String inputPathStr = HDFS_HOME+ADMIN+INPUT+"/"+args[0];
        String outputPathStr = HDFS_HOME+FEATURES+"/"+time;

        job.setInputFormatClass(HibInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.setJarByClass(FeatureExtraction.class);
        job.setMapperClass(FeatureExtractMapper.class);
        // no reducer needed
        job.setNumReduceTasks(0);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.setInputPaths(job, new Path(inputPathStr));
        FileOutputFormat.setOutputPath(job, new Path(outputPathStr));

        boolean success =  job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        if (ToolRunner.run(new FeatureExtraction(), args) == 1) {
            System.out.println(".......Feature Extraction failure........");
            System.exit(1);
        }
        System.exit(0);
    }
}


