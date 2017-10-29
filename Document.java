import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class Document {
	
	protected int[] words;

	public Document(String line, HashMap<String, Integer> wordMap, ArrayList<String> uniWordMap, HashSet<String> stopwords) {
		int number = wordMap.size();
//		String tmpline = line.replaceAll("[^a-zA-Z]", " ");
		String tmpline = line;
		ArrayList<Integer> tmpwords = new ArrayList<Integer>();
		
		String[] tokens = tmpline.split(" |\t");
		for(int i = 0; i < tokens.length; i++) {
			String tmp = tokens[i].trim().toLowerCase();
			if(stopwords.contains(tmp)) continue;
			if(!wordMap.containsKey(tmp)) {
				tmpwords.add(number);
				wordMap.put(tmp, number++);
				uniWordMap.add(tmp);
			} else {
				tmpwords.add(wordMap.get(tmp));
			}
		}
		
		words = new int[tmpwords.size()];
		for(int i = 0; i < tmpwords.size(); i++)
			words[i] = tmpwords.get(i);
	}

}