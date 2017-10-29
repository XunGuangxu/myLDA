import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;


public class myLDA {

	public int K;//number of topics
	public int V;//vocabulary size
	public int D;//number of documents
	public int n_iter;
	public double beta;
	public double alpha;
	public double Vbeta;
	public double Kalpha;
	
	public int[][] z;//D*N_d, topic assignment for each word
	public int[][] C_zdt;//D*K, topic counter for each document
	public int[][] C_tword;//K*V, counter for each word in each topic
	public int[] C_t;//size K, counter for words assigned to each topic
	public int[] C_d;//size D, counter for words in each document
	
	public myLDA(double alpha, double beta, int k, int n_iter, ArrayList<Document> docs, ArrayList<String> uniWordMap) {
		V = uniWordMap.size();
		D = docs.size();
		this.K = k;
		this.alpha = alpha;
		this.beta = beta;
		this.Kalpha = K*alpha;
		this.Vbeta = V*beta;
		this.n_iter = n_iter;
		
		C_zdt = new int[D][K];
		C_tword = new int[K][V];
		C_t = new int[K];
		C_d = new int[D];
		for(int i = 0; i < D; i++) {
			C_d[i] = 0;
			for(int j = 0; j < K; j++) {
				C_zdt[i][j] = 0;
			}
		}
		for(int i = 0; i < K; i++) {
			C_t[i] = 0;
			for(int j = 0; j < V; j++) {
				C_tword[i][j] = 0;
			}
		}
		
		// Randomly assign a topic to each word
		random_init(docs);
	}

	public void random_init(ArrayList<Document> docs) {
		System.out.println("Starting random initialization...");
		
		z = new int[D][];
		for(int d = 0; d < D; d++) {
			C_d[d] = docs.get(d).words.length;
			z[d] = new int[C_d[d]];
			
			for(int i = 0; i < C_d[d]; i++) {
				double rnd = Math.random();
				double tmpsum = 0.0;
				int assignedtopic = -1;
				for(int t = 0; t < K; t++) {
					tmpsum = tmpsum + 1.0/K;
					if(tmpsum > rnd) {
						assignedtopic = t;
						break;
					}
				}
				
				z[d][i] = assignedtopic;
				C_zdt[d][assignedtopic]++;
				C_tword[assignedtopic][docs.get(d).words[i]]++;
				C_t[assignedtopic]++;
			}
		}
		
		System.out.println("Initialization done.");
	}

	public void run(ArrayList<Document> docs) {
		System.out.println("Estimation starts.");
		
		for(int iter = 0; iter < n_iter; iter++) {
			System.out.println("Iteration " + (iter+1) + "...");
			
			for(int d = 0; d < D; d++) {
				for(int i = 0; i < C_d[d]; i++) {
					int word = docs.get(d).words[i];
					sample_z(d, i, word);
				}				
			}
		}
		
		System.out.println("Estimation completed.");
	}

	public void sample_z(int d, int i, int word) {
		int	topic = z[d][i];
		
		C_zdt[d][topic]--;
		C_tword[topic][word]--;
		C_t[topic]--;
		C_d[d]--;
		
		topic = draw_z(d, i, word);
		
		C_zdt[d][topic]++;
		C_tword[topic][word]++;
		C_t[topic]++;
		C_d[d]++;
		z[d][i] = topic;
	}

	public int draw_z(int d, int i, int word) {
		int new_topic = -1;
		double[] pz_w = new double [K];
		
		//pz_w /propto pz_d * pw_z
		double pz_d, pw_z;
		double tmpsum = 0.0;
		for(int t = 0; t < K; t++) {
			/***********************************************/
			pz_d = (C_zdt[d][t] + alpha) / (C_d[d] + Kalpha);
//			pz_d =  C_zdt[d][t] + alpha; // the simplified equation since the denomitor is actually a constant
			/***********************************************/
			pw_z = (C_tword[t][word] + beta) / (C_t[t] + Vbeta);
			pz_w[t] = pz_d * pw_z;
			tmpsum = tmpsum + pz_w[t];
		}
		
		//normalize pz_w
		for(int t = 0; t < K; t++)
			pz_w[t] = pz_w[t]/tmpsum;
		
		//draw z
		double randz = Math.random();
		double thred = 0.0;
		for(int t = 0; t < K; t++) {
			thred = thred + pz_w[t];
			if(thred >= randz) {
				new_topic = t;
				break;
			}
		}
		
		return new_topic;
	}

	public void show_twords(int num_word, ArrayList<String> uniWordMap, String output_tword) {
		try {
			
			BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(output_tword), "UTF-8"));
			for(int i = 0; i < K; i++) {
				int max = 0;
				int[] pos = new int[num_word];
				HashSet<Integer> posi = new HashSet<Integer>();
				for(int j = 0; j < num_word; j++) {
					for(int xx = 0; xx < V; xx++) {
						if((C_tword[i][xx] > max)&&(!posi.contains(xx))) {
							max = C_tword[i][xx];
							pos[j] = xx;
						}
					}
					posi.add(pos[j]);
					max = 0;
				}
				
				writer.write("Topic " + String.valueOf(i) + ":------------------------\n");
				for(int j = 0; j < num_word; j++)
					writer.write(uniWordMap.get(pos[j]) + "\t" + String.valueOf(C_tword[i][pos[j]]) + "\n");
				writer.write("\n");
			}
			
			writer.close();

		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void show_pzd_pzw_sample(String output_pzd, String output_pzw, String output_z, ArrayList<Document> docs) {
		try {			
			BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(output_pzd), "UTF-8"));
			for(int i = 0; i < D; i++) {
				for(int j = 0; j < K; j++) {
					writer.write(String.valueOf(1.0*C_zdt[i][j]/C_d[i])+" ");
				}
				writer.write("\n");
			}			
			writer.close();
			
			writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(output_pzw), "UTF-8"));
			for(int i = 0; i < K; i++) {
				for(int j = 0; j < V; j++) {
					writer.write(String.valueOf(C_tword[i][j])+" ");
				}
				writer.write("\n");
			}			
			writer.close();
			
			writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(output_z), "UTF-8"));
			for(int d  =  0; d < D; d++) {
				for(int i = 0; i < C_d[d]; i++) {
					writer.write(String.valueOf(docs.get(d).words[i]) + ":" + String.valueOf(z[d][i]) + " ");
				}
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