package cn.ruc.gyf.tieba;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Random;

import cn.ruc.gyf.util.Const;

public class gyftest {
	
	private static void getTiebaRoot(){
		String filepath = Const.TiebaRootPath;
		String encoding = Const.FileSaveEncode;
		File file = new File(filepath);
		String line;
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader br = new BufferedReader(read);
			while((line=br.readLine())!=null){
				System.out.println(line);
				System.out.println(line.length());
			}
			br.close();
			read.close();
		} catch (Exception e) {
			System.out.println("init tieba root err");
			System.exit(-1);
		}
	}
	
	public static void main(String[] args){
		Random random=new Random();
		for(int i=0;i<10;i++){
			System.out.println(random.nextInt(1000));
		}
	}
}
