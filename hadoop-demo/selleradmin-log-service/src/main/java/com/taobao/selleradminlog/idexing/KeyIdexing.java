package log.test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class KeyIdexing {
	
	public static void main(String[] args){
		
		try {
		   run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void run() throws IOException, InterruptedException, ClassNotFoundException{	
		
		
		 Configuration conf = new Configuration();
		 Job job = new Job(conf, "key idexing");
		 conf.set("hadoop.job.ugi", "hadoop,hadoop");
		 job.setJarByClass(KeyIdexing.class);
		 job.setOutputKeyClass(Text.class);
		 job.setOutputValueClass(Text.class);
		 job.setReducerClass(RecordReducer.class);

		 //ʹ���Զ��������ʽ
//		 job.setOutputFormatClass(MutipleRecordFileFormat.class);
			 
		 //ʹ��ʱ����������Ŀ¼
		 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		 String localDir = "";

		 String inputPath = "logs";//"selleradmin/raw";
		 String outputPath = new StringBuffer("selleradmin/base")
		                         .append(dateFormat.format(new Date()).toString())
		                         .toString();
		 
		 //����ָ��������Ŀ¼��inputPath = "/input1,/input2"
		 //ʹ��FileInputFormat.getPathStrings�����Եõ���ǰ���е�����Ŀ¼	 
		 FileInputFormat.addInputPath(job, new Path(inputPath));
		 
		 
		 //��Ӧmapred.output.dir
		 FileOutputFormat.setOutputPath(job, new Path(outputPath));
		
		long start = System.currentTimeMillis(); 
		if(job.waitForCompletion(true)){
			//���������ɵ�Ŀ¼
//			File file = new File(localDir);
//			String dest = "";
//			if(file.isDirectory()){
//				dest = file.getPath().lastIndexOf("/") == -1 ? "/" + localDir
//						: localDir;
//			    File destDir = new File(dest);
//			    if(!destDir.exists()){
//			    	destDir.mkdir();
//			    }
//			    
//				file.renameTo(destDir);
//			}else{
//				System.out.println("error local file");
//			}
		}
		System.out.println(System.currentTimeMillis()-start);
	}
	
	
	

}
