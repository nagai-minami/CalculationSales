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
		BufferedReader branchBr = null;

		try{
			File file = new File(args[0]+ File.separator + "branch.lst");
			FileReader fr = new FileReader(file);
			branchBr = new BufferedReader(fr);
			String siten;
			while((siten = branchBr.readLine()) != null) {
				String str = (siten);
				String[] items = str.split(",");
				if(items.length != 2 || !items[0].matches("^[0-9]{3}$")){
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
				branchNames.put(items[0],items[1]);
				branchSales.put(items[0],0L);		
			}
		}catch(IOException e){
			System.out.println("支店定義ファイルが存在しません");
			return;
		}finally{
			if (branchBr != null)
                try {
                    branchBr.close();
                } catch (IOException e) {
                    System.out.println("予期せぬエラーが発生しました");
                }
		 }

///////////商品定義ファイルの読み込み
		Map<String,String> commodityNames = new HashMap<String,String>();
		Map<String,Long> commoditySales = new HashMap<String,Long>();
		BufferedReader commodityBr = null;

		try{
			File file = new File(args[0]+ File.separator + "commodity.lst");
			FileReader fr = new FileReader(file);
			commodityBr = new BufferedReader(fr);
			String products;
			while((products = commodityBr.readLine()) != null) {
				String str = (products);
				String[] items = str.split(",");
				
				if(items.length !=2 || !items[0].matches("^\\w{8}$")){
						System.out.println("商品定義ファイルのフォーマットが不正です");
						return;
					}
				commodityNames.put(items[0], items[1]);
				commoditySales.put(items[0],0L);	
			}
		}catch(IOException e){
			System.out.println("商品定義ファイルが存在しません");
			return;
		}finally{
			if (branchBr != null)
                try {
                	commodityBr.close();
                } catch (IOException e) {
                    System.out.println("予期せぬエラーが発生しました");
                }
		 }	

///////////売上ファイルの連番チェックと売上ファイル名のリスト化
		File check = new File(args[0]);
		String[] fileList = check.list();
		ArrayList<String> sales = new ArrayList<String>();

		for(int i = 0; i < fileList.length; i++){
			if(fileList[i].matches("^\\d{8}\\.rcd$")){
	    		sales.add(fileList[i]);
	    		Collections.sort(sales);
	    		}
		}
		
		for(int n = 0; n < sales.size(); n++){
			String str = sales.get(n);
			String[] salesList = str.split(".rcd");
			if(Integer.parseInt(salesList[0])!= n+1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}

///////////売り上げファイルの読み込み
		
		BufferedReader salesBr = null;
		try{
			for(int n = 0; n < sales.size(); n++){
				File file = new File(args[0]+ File.separator + sales.get(n));
				FileReader fr = new FileReader(file);
				salesBr = new BufferedReader(fr);
				ArrayList<String> summaryList = new ArrayList<String>();
				String uriage;
				while((uriage = salesBr.readLine()) != null){
					summaryList.add(uriage);
				}
				if(summaryList.size() != 3){
					System.out.println(sales +"のフォーマットが不正です");
					return;
				}

////////※saleは売上金額※
				Long sale = Long.parseLong(summaryList.get(2));

///////////支店別集計合計金額：branchSalesへ集計
				String branchKey = summaryList.get(0);
				Long branchSalesValue;
				if(!branchSales.containsKey(branchKey)){
					System.out.println(sales + "の支店コードが不正です");
					return;
				}
				branchSalesValue = branchSales.get(branchKey);
				Long branchSum = branchSalesValue + sale;
				branchSales.put(branchKey,branchSum);

				if(branchSum > 10000000000L){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

///////////商品別集計合計金額：commoditySalesへ集計
				String commodityKey = summaryList.get(1);
				Long commodityValues;
				if(!commoditySales.containsKey(commodityKey)){
					System.out.println(sales + "の支店コードが不正です");
					return;
				}
				commodityValues =commoditySales.get(commodityKey);
				Long commoditySum = commodityValues + sale;
				commoditySales.put(commodityKey,commoditySum);
				if(commoditySum > 10000000000L){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			}
		}catch(IOException e2){
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}finally{
			if (salesBr != null)
                try {
                	salesBr.close();
                } catch (IOException e) {
                    System.out.println("予期せぬエラーが発生しました");
                }
		 }

/////////// branchSales List(ソート)
		List<Map.Entry<String,Long>> branchSaleEntries = new ArrayList<Map.Entry<String,Long>>(branchSales.entrySet());
		Collections.sort(branchSaleEntries, new Comparator<Map.Entry<String,Long>>() {
			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
			return (o2.getValue()).compareTo(o1.getValue());
			}
		});

///////////  commoditySale List(ソート)
		List<Map.Entry<String,Long>> commoditySaleEntries = new ArrayList<Map.Entry<String,Long>>(commoditySales.entrySet());
		Collections.sort(commoditySaleEntries, new Comparator<Map.Entry<String,Long>>() {
			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
			return (o2.getValue()).compareTo(o1.getValue());
			}
		});

///////////支店別集計ファイル(branch.out)へ出力
		BufferedWriter branchOutBw = null;
		try{
			File newfile = new File(args[0]+ File.separator + "branch.out");
			FileWriter filewriter = new FileWriter(newfile);
			branchOutBw = new BufferedWriter(filewriter);
			for (Entry<String,Long> s : branchSaleEntries) {
				 branchOutBw.write(s.getKey() + "," +  branchNames.get(s.getKey()) + "," +  s.getValue());
				 branchOutBw.newLine();
			}
		}catch(IOException e){
			 System.out.println("予期せぬエラーが発生しました")	;
			  return;
		}finally{
			  if (salesBr != null)
	                try {
	                	branchOutBw.close( );
	                } catch (IOException e) {
	                    System.out.println("予期せぬエラーが発生しました");
	                }
			  
		} 

///////////支店別集計ファイル(commodity.out)へ出力
		BufferedWriter commodityOutBw = null;
		try{
			File newfile = new File(args[0]+ File.separator + "commodity.out");
			FileWriter filewriter = new FileWriter(newfile);
			commodityOutBw= new BufferedWriter(filewriter);
			for (Entry<String,Long> s : commoditySaleEntries) {
				commodityOutBw.write(s.getKey() + "," +  commodityNames.get(s.getKey()) + "," +  s.getValue());
				commodityOutBw.newLine();
			}
		}catch(IOException e){
		  System.out.println("予期せぬエラーが発生しました")	;
		  return;
		}finally{
			  if (salesBr != null)
	                try {
	                	commodityOutBw.close( );
	                } catch (IOException e) {
	                    System.out.println("予期せぬエラーが発生しました");
	                }		  
		} 
		
	}
}

