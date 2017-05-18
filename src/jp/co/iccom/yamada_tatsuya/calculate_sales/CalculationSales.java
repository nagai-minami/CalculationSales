package jp.co.iccom.yamada_tatsuya.calculate_sales;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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

///////////支店定義ファイルを読み込む
		Map<String,String> branchNames = new HashMap<String,String>();
		Map<String,Long> branchSales = new HashMap<String,Long>();


		try{
			File file = new File(args[0]+ File.separator + "branch.lst");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String siten;
			while((siten = br.readLine()) != null) {
				String str = (siten);
				String[] items = str.split(",");

				if(items[0].matches("^[0-9]{3}$") && items.length ==2){
					branchNames.put(items[0],items[1]);
					branchSales.put(items[0],0L);
				}else {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
			}
			br.close();
		}catch(IOException e){
			System.out.println("支店定義ファイルが存在しません");
			return;
		}

///////////商品定義ファイルを読み込む
		Map<String,String> commodityNames = new HashMap<String,String>();//商品定義ファイルのマップ
		Map<String,Long> commoditySales = new HashMap<String,Long>();

		try{
			File file = new File(args[0]+ File.separator + "commodity.lst");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String products;
			while((products = br.readLine()) != null) {
				String str = (products);
				String[] items = str.split(",");
				if(items[0].matches("^\\w{8}$") && items.length ==2){
					commodityNames.put(items[0],items[1]);
					commoditySales.put(items[0],0L);
				}else {
						System.out.println("商品定義ファイルのフォーマットが不正です");
						return;
					}
				commodityNames.put(items[0], items[1]);
				commoditySales.put(items[0],0L);
		}
		br.close();
		}catch(IOException e){
			System.out.println("商品定義ファイルが存在しません");
			return;
		}

///////////売上ファイルの連番チェックとファイル名のリスト化
		File check = new File(args[0]);
		String[] fileList = check.list();
		ArrayList<String> sales = new ArrayList<String>();

		for(int i = 0; i < fileList.length; i++){
			if(fileList[i].matches("^\\d{8}\\.rcd$")){
	    		sales.add(fileList[i]);
	    		if(fileList[i].matches("^\\d{8}\\.rcd$")){
	    			 Collections.sort(sales);
	    		}else{
	    			System.out.println("売上ファイル名が連番になっていません");
					return;
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
		try{
			for(int n = 0; n < sales.size(); n++){
				File file = new File(args[0]+ File.separator + sales.get(n));
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				ArrayList<String> summaryList = new ArrayList<String>();
				String uriage;
				while((uriage = br.readLine()) != null){
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
				if(branchSales.get(branchKey)==null){
					System.out.println(sales + "の支店コードが不正です");
					return;
				}
				branchSalesValue = branchSales.get(branchKey);
				Long branchSum = branchSalesValue + sale;
				branchSales.put(branchKey,branchSum);

				if(branchSum > 1000000000){
					System.out.println("合計金額が10桁を超えました");
					return;
				}

///////////商品別集計合計金額：commoditySalesへ集計
				String commodityKey = summaryList.get(1);
				Long commodityValues;
				if(commoditySales.get(commodityKey)==null){
					System.out.println(sales + "の商品コードが不正です");
					return;
				}
				commodityValues =commoditySales.get(commodityKey);
				Long commoditySum = commodityValues + sale;
				commoditySales.put(commodityKey,commoditySum);
				if(commoditySum > 1000000000){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			br.close();
			}
		}catch(IOException e){
			System.out.println("売上ファイル名が連番になっていません");
			return;
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
		try{
			File newfile = new File(args[0]+ File.separator + "branch.out");
			PrintWriter pw = new PrintWriter(args[0]+ File.separator + "branch.out");
			for (Entry<String,Long> s : branchSaleEntries) {
				 pw.println(s.getKey() + "," +  branchNames.get(s.getKey()) + "," +  s.getValue());
			}
			pw.close( );
			}catch(IOException e){
			  System.out.println("予期せぬエラーが発生しました")	;
			  return;
			}

///////////支店別集計ファイル(commodity.out)へ出力
		try{
			File newfile2 = new File(args[0]+ File.separator + "commodity.out");
			PrintWriter pw = new PrintWriter(args[0]+ File.separator + "commodity.out");
			for (Entry<String,Long> s : commoditySaleEntries) {
				 pw.println(s.getKey() + "," +  commodityNames.get(s.getKey()) + "," +  s.getValue());
			}
			pw.close( );
			}catch(IOException e){
			  System.out.println("予期せぬエラーが発生しました")	;
			  return;
			}
		}
	}
}

