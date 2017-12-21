import java.util.HashSet;

import semantic.search.utilities.Constants;
import maui.main.MauiModelBuilder;
import maui.main.MauiTopicExtractor;

public class ExtractKeyPhrases {
	
	
	private MauiModelBuilder modelBuilder;
	private MauiTopicExtractor extractTopics;
	
	
	
	public ExtractKeyPhrases(){
		modelBuilder = new MauiModelBuilder();
		extractTopics = new MauiTopicExtractor();
	}
	
	
	public void buildModel(String[] options){
		if(options != null){
			// change to 1 for short documents
			modelBuilder.minNumOccur = 1;

			HashSet<String> fileNames;
			try {
				modelBuilder.setOptions(options);
				fileNames = modelBuilder.collectStems();
				modelBuilder.buildModel(fileNames);
				modelBuilder.saveModel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			System.err.println("Please provide Options to build Model");
		}
	}
	
	public void extractKeyPhrases(String[] options){
		if(options != null){
			try {
				extractTopics.setOptions(options);
				extractTopics.loadModel();
				HashSet<String> fileNames = extractTopics.collectStems();
				extractTopics.extractKeyphrases(fileNames);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			System.err.println("Please Provide Options to Extract KeyPhases");
		}
	}
}
public int searchWiki(String query) throws UnsupportedEncodingException, IOException{
		StringBuilder builder = new StringBuilder();
		builder.append(APIBase).append(APIParams);
		builder.append("&gsrlimit=").append(searchLimit).append("&gsrsearch=").append(URLEncoder.encode(query, "UTF-8"));
		String APIUrl = builder.toString();
		System.out.println(APIUrl);
		// delete all files in the directory
		FileUtils.cleanDirectory(new File(Constants.wikiLocale));
		// read JSON data from url
		String responseText = readResponseText(APIUrl);
		// parse and write JSON data to file
		int status = writeWikiExtract(responseText);
		
		return status;
	}
	
	private int writeWikiExtract(String responseText){
		JSONObject response;
		int pageCount = -1;
		try {
			response = (JSONObject) new JSONParser().parse(responseText);
			JSONObject query = (JSONObject) response.get("query");
			if(query == null){
				return -1;
			}
			JSONArray pageids = (JSONArray) query.get("pageids");
			pageCount = pageids.size();
			Iterator<String> pages = pageids.iterator();
			JSONObject pageNodes = (JSONObject) query.get("pages");
			while(pages.hasNext()){
				String pageid = pages.next();
				JSONObject page = (JSONObject) pageNodes.get(pageid);
				String data = (String) page.get("extract");
				if( data == null || data.trim().equals("")){
					data = (String)page.get("title");
				}
				String indexFile = ((Long) page.get("index")).toString();
				// we use index as the file name to write data
				writeExtractToFile(data, indexFile);
			}
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pageCount;
	}
	
	private void writeExtractToFile(String data, String fileName){
		 String filePath = Constants.wikiLocale + fileName + ".txt";
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(filePath, "UTF-8");
			writer.print(data);
		} catch (Exception e) {
			System.err.println("Error Writing Wiki Search Extract to File!");
			e.printStackTrace();
		} 
		writer.close();
	}
	
private String readResponseText(String url) throws MalformedURLException, IOException{
		InputStream in = new URL(url).openStream();
		try {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
			StringBuilder builder = new StringBuilder();
			int charRet;
			while( (charRet = reader.read()) != -1){
				builder.append((char) charRet);
			}
			return builder.toString();
		} finally{
			in.close();
		}
		
	}
}
public void buildInvertedIndex(String folderPath, String stopWordsPath){
		File dir = new File(folderPath);
		try {
			String[] files = dir.list();
			Map<String, Double> tmp_list;
			String fileName;
			String currentLine;
			BufferedReader br = null;
			for(String file: files){
					br = new BufferedReader(new FileReader(folderPath + "/" + file));
					fileName = file.substring(0, file.length()-4);
					while((currentLine = br.readLine()) != null){
						currentLine = currentLine.toLowerCase();
						String [] tokens = currentLine.split("\t");
						
						double score = 0.0;
						String phrase = currentLine;
						if(tokens.length == 3){
							phrase = tokens[0];
							score = Double.parseDouble(tokens[2]);
							hasScore = true;
						}
						if(score >= minDocScore ){
							if(postingList.containsKey(phrase)){
								tmp_list = postingList.get(phrase);
								tmp_list.put(fileName, score);
								postingList.put(phrase, tmp_list);
							}else{
								tmp_list = new HashMap<String, Double>();
								tmp_list.put(fileName, score);
								postingList.put(phrase, tmp_list);
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public Map<String, Double> searchInvertedIndex(String key){
		
		if(! key.equals("") || !key.equals(" ") || key != null){
			key = stemmer.stem(key);
			if(postingList.containsKey(key)){
				return postingList.get(key);
			}
		}
		return null;
	}
}
