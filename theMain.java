import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class theMain {
	public static String data_dir = "/media/guangxu/O_o/UB/research/dataset/20newsgroups/";
	
	public static String filename = data_dir + "CoEmbedding/20news_min_cnt.txt";
	public static String stopfile = data_dir + "stoplist.txt";
	
	public static String output_tword  = data_dir + "result/myLDA/twords.txt";
	public static String outputWordmap = data_dir + "result/myLDA/wordmap.txt";
	public static String outputDoc     = data_dir + "result/myLDA/docInNumber.txt";
	public static String output_pzd    = data_dir + "result/myLDA/pzd.txt";
	public static String output_pzw    = data_dir + "result/myLDA/pzw.txt";
	public static String output_z      = data_dir + "result/myLDA/samplingResult.txt";
	
	public static void main(String[] args) {
		ArrayList<Document> docs = new ArrayList<Document>();
		HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
		ArrayList<String> uniWordMap = new ArrayList<String>();
		int K = 20;
		int n_iter = 100;		
		double alpha = 50.0/K;//Prior for p(z|d)
		double beta = 0.01;//Prior for p(w|z)
		
		// 1. read the documents
		read_docs(filename, docs, wordMap, uniWordMap, stopfile);
		
		// 2. run the model
		myLDA model = new myLDA(alpha, beta, K, n_iter, docs, uniWordMap);
		model.run(docs);
		model.show_twords(10, uniWordMap, output_tword);		
		model.show_pzd_pzw_sample(output_pzd, output_pzw, output_z, docs);
		
	}

	private static void read_docs(String filename, ArrayList<Document> docs, HashMap<String, Integer> wordMap, ArrayList<String> uniWordMap, String stopfile) {
		try {
			HashSet<String> stopwords = new HashSet<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(stopfile), "UTF-8"));
			String line = "";
			while((line = reader.readLine())!=null)	{
				stopwords.add(line.trim().toLowerCase());
			}
			reader.close();
			
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			while((line = reader.readLine())!=null)	{
				Document doc = new Document(line, wordMap, uniWordMap, stopwords);
				docs.add(doc);
			}
			reader.close();
			
			BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(outputWordmap), "UTF-8"));
			for(int i = 0; i < uniWordMap.size(); i++)
				writer.write(uniWordMap.get(i)+"\n");
			writer.close();
			
			writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(outputDoc), "UTF-8"));
			for(int i  =  0; i < docs.size(); i++) {
				for(int j = 0; j < docs.get(i).words.length; j++)
					writer.write(String.valueOf(docs.get(i).words[j]) + " ");
				writer.write("\n");
			}
			writer.close();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}