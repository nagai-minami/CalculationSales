package jp.alhinc.nagai_minami.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CalculationSales {

	public static void main(String[] args){

///////////コマンドライン引数のエラー処理
		if (args.length != 1 ) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
	
///////////支店定義ファイルの読み込み
		Map<String,String> branchNames = new HashMap<String,String>();
		Map<String,Long> branchSales = new HashMap<String,Long>();
	
	
		if(!fileRead(args[0],"branch.lst","支店定義ファイル","^[0-9]{3}$",branchNames, branchSales)){
			return;
		}
		
///////////商品定義ファイルの読み込み
		
		Map<String,String> commodityNames = new HashMap<String,String>();
		Map<String,Long> commoditySales = new HashMap<String,Long>();
		
		if(!fileRead(args[0],"commodity.lst","商品定義ファイル", "^\\w{8}$", commodityNames, commoditySales)){
			return;
		}

///////////売上ファイルの連番チェックと売上ファイル名のリスト化
		File check = new File(args[0]);
		String[] fileList = check.list();
		ArrayList<String> sales = new ArrayList<String>();

		for(int i = 0; i < fileList.length; i++){
			File fileName = new File(args[0], fileList[i]);
			if(fileList[i].matches("^\\d{8}\\.rcd$") && fileName.isFile()){
	    		sales.add(fileList[i]);
			}
		}
		Collections.sort(sales);
		for(int n = 0; n < sales.size(); n++){
			String str = sales.get(n);
			String[] salesList = str.split(".rcd");
			if(Integer.parseInt(salesList[0])!= n+1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

///////////売り上げファイルの読み込み
		BufferedReader br = null;
		try{
			for(int n = 0; n < sales.size(); n++){
				File file = new File(args[0]+ File.separator + sales.get(n));
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);
				ArrayList<String> summaryList = new ArrayList<String>();
				String uriage;
				while((uriage = br.readLine()) != null){
					summaryList.add(uriage);
				}
				if(summaryList.size() != 3){
					System.out.println(sales.get(n) +"のフォーマットが不正です");
					return;
				}

				if(!summaryList.get(2).matches("^[0-9]*$")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}

////////※saleは売上金額※
				Long sale = Long.parseLong(summaryList.get(2));

///////////支店別集計合計金額：branchSalesへ集計
				String branchKey = summaryList.get(0);
				Long branchSalesValue;
				if(!branchSales.containsKey(branchKey)){
					System.out.println(sales.get(n) + "の支店コードが不正です");
					return;
				}
				branchSalesValue = branchSales.get(branchKey);
				Long branchSum = branchSalesValue + sale;


				if(branchSum >= 10000000000L){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

///////////商品別集計合計金額：commoditySalesへ集計
				String commodityKey = summaryList.get(1);
				Long commodityValues;
				if(!commoditySales.containsKey(commodityKey)){
					System.out.println(sales.get(n) + "の商品コードが不正です");
					return;
				}
				commodityValues =commoditySales.get(commodityKey);
				Long commoditySum = commodityValues + sale;

				if(commoditySum >= 10000000000L){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				branchSales.put(branchKey,branchSum);
				commoditySales.put(commodityKey,commoditySum);
			}
		}catch(IOException e2){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}finally{
			if (br != null){
                try {
                	br.close();
                } catch (IOException e) {
                    System.out.println("予期せぬエラーが発生しました");
                    return;
                }
			}
		 }

///////////支店別集計ファイル(branch.out)へ出力
		
		if(!fileWrite(branchSales, args[0],"branch.out", branchNames)){
			return;
		}

///////////支店別集計ファイル(commodity.out)へ出力
		if(!fileWrite(commoditySales, args[0], "commodity.out", commodityNames)){
		    return;
	   }
	}
//////////fileReadメソッド：ファイルから入力
	private static boolean fileRead(String path,String fileName, String fileError, String match, Map mapNames, Map mapSales){

		BufferedReader br = null;
		try{
			File file = new File(path + File.separator + fileName);
			if(!file.isFile()){
				System.out.println(fileError +"が存在しません");
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String list;
			while((list = br.readLine()) != null) {
				String str = (list);
				String[] items = str.split(",");
				if(items.length != 2 || !items[0].matches(match)){
					System.out.println(fileError + "のフォーマットが不正です");
					return false;
				}
			mapNames.put(items[0],items[1]);
			mapSales.put(items[0],0L);
				
			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return false;
		}finally{
			if (br != null){
	            try {
	                br.close();
	            } catch (IOException e) {
	                System.out.println("予期せぬエラーが発生しました");
	                return false;
	            }
			  }
		 }
		return true;
	}
	
	
//////////fileWriteメソッド：ファイルへ出力
	private static boolean fileWrite(Map mapSales, String path, String fileOut,Map mapNames){
		List<Map.Entry<String,Long>> entries = new ArrayList<Map.Entry<String,Long>>(mapSales.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String,Long>>() {
			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
			return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		BufferedWriter bw = null;
	
		try{
			File newfile = new File(path + File.separator + fileOut);
			FileWriter filewriter = new FileWriter(newfile);
			bw = new BufferedWriter(filewriter);
			
			for (Entry<String,Long> s : entries) {
				 bw.write(s.getKey() + "," +  mapNames.get(s.getKey()) + "," +  s.getValue());
				 bw.newLine();
			}
		}catch(IOException e){
			 System.out.println("予期せぬエラーが発生しました")	;
			  return false;
		}finally{
			  if (bw != null){
	                try {
	                	bw.close( );
	                } catch (IOException e) {
	                    System.out.println("予期せぬエラーが発生しました");
	                    return false;
	                }
			  }
		}
		return true;
	}

}

