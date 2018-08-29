package code;

import java.io.UnsupportedEncodingException;

import utils.SystemParas;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.FileReader;
import java.io.FileWriter;


/*
 * 本工具是在做 中文分词，结果作为 LDA的输入*/

public class NlpirTest {

	// 定义接口CLibrary，继承自com.sun.jna.Library
	public interface CLibrary extends Library {
		// 定义并初始化接口的静态变量
		CLibrary Instance = (CLibrary) Native.loadLibrary(
				"E:\\WorkSpace_2014\\CNWordsParse\\bin\\win64\\NLPIR", CLibrary.class);  // 可能会因时间过期失效，需要重新加载
			//	"D:\\NLPIR\\bin\\ICTCLAS2013\\x64\\NLPIR", CLibrary.class);
		
		public int NLPIR_Init(String sDataPath, int ctype, String sLicenceCode);		
		public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);
		public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit,
				boolean bWeightOut);
		public String NLPIR_GetFileKeyWords(String sLine, int nMaxKeyLimit,
				boolean bWeightOut);
		public int NLPIR_AddUserWord(String sWord);//add by qp 2008.11.10
		public int NLPIR_DelUsrWord(String sWord);//add by qp 2008.11.10
		public String NLPIR_GetLastErrorMsg();
		public void NLPIR_Exit();
	}

	public static String transString(String aidString, String ori_encoding,
			String new_encoding) {
		try {
			return new String(aidString.getBytes(ori_encoding), new_encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		String argu = "G:\\Workspace\\CNWordsParse";      //执行的根目录  
		
		//String system_charset = "GBK";//GBK----0
	    String system_charset = "UTF-8";
		int charset_type = 1;
		
		int init_flag = CLibrary.Instance.NLPIR_Init(argu, charset_type,  "0");
		String nativeBytes = null;

		if (0 == init_flag) {
			nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			System.err.println("初始化失败！fail reason is "+nativeBytes);
			return;
		}

		//String sInput = "克里在波士顿家乡招待杨洁篪的举动受到了诸多外媒的关注。路透社17日称，克里笑容满面地站在家门口欢迎杨洁篪并与他握手，并为中国客人准备了晚宴。";// 试验
		System.out.println("Start~~");
    	
    	File  fin = new File("E:\\WorkSpace_2014\\CNWordsParse\\Inputdata\\Auto");   // 从自动抽取的 文档中读取字符串      此处也对应下面一处路径
    	//File  fin = new File("E:\\WorkSpace_2014\\CNWordsParse\\Inputdata");   // 从手动产生的 文档中读取字符串
    	
    	File[] input = fin.listFiles();
    	String filename="";
    	String sInput="";
     
  	  // 删除 结果文件夹下（LDA模块） 存在的文件
		  File folder = new File("E:\\WorkSpace_2014\\NLPLDAYL\\testdata\\CNLdaOriginalDocs");
		  File[] files = folder.listFiles();
		  for (int i=0;i<files.length;i++){
		    File fileexist = files[i];
		    if (fileexist.exists()){
		    	fileexist.delete();
		    }
		  }
    	
         
    	for(int sg=0;sg<input.length;sg++){  		
    		sInput="";
    		filename = input[sg].getName();
    		String filePath = "E:\\WorkSpace_2014\\CNWordsParse\\Inputdata\\Auto\\"+filename;        // 经过 上一步从Excel表抽取的      处理的 单个 文件的绝对路径    
    		//String filePath = "E:\\WorkSpace_2014\\CNWordsParse\\Inputdata\\"+filename;        // 手工建立的  处理的 单个 文件的绝对路径    
    		System.out.println("正在将第" + (sg+1) + "个文件" + filename + "读入内存……");  		
    		String encoding="UTF-8";
    		//String encoding="GBK";
            File file=new File(filePath);         
        
         if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式 		
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){    // 在此获得需要分词的字符串
                	
                	sInput= sInput+lineTxt;
                	System.out.println(lineTxt);
                }                
            	System.out.println(sInput);
                
                System.out.println("关闭第" + (sg+1) + "个文件" + filename + "，释放资源……");
                System.out.print("\r\n");
                read.close();
            }else{
                System.out.println("找不到指定的文件");
            }  
         
	    //  分词过程
              nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
			  System.out.println("分词结果为： " + nativeBytes);
              System.out.println("分词结束");
			  System.out.print("\r\n");
			  System.out.print("\r\n");			  
			  
		//  写结果过程
			 		  
            String filePathout = "E:\\WorkSpace_2014\\NLPLDAYL\\testdata\\CNLdaOriginalDocs\\"+"Words"+filename;        //  存储 单个结果 文件的绝对路径
            File fout = new File(filePathout); 
            if(!fout.exists()){  
                fout.createNewFile();  
               } 
            System.out.println("正在写第" + (sg+1) + "个文件" + fout);
            BufferedWriter output = new BufferedWriter(new FileWriter(fout));  
            output.write(nativeBytes);          
            System.out.println(nativeBytes);
            System.out.println("写完第" + (sg+1) + "个文件" + fout);
            System.out.print("\r\n");
            System.out.print("\r\n");
            output.close();      
			
            
		/*	CLibrary.Instance.NLPIR_AddUserWord("要求美方加强对输 n");
			CLibrary.Instance.NLPIR_AddUserWord("华玉米的产地来源 n");
			nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
			System.out.println("增加用户词典后分词结果为： " + nativeBytes);
			
			CLibrary.Instance.NLPIR_DelUsrWord("要求美方加强对输");
			nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
			System.out.println("删除用户词典后分词结果为： " + nativeBytes);*/
			
			
			/*int nCountKey = 0;
			String nativeByte = CLibrary.Instance.NLPIR_GetKeyWords(sInput, 10,false);

			System.out.print("关键词提取结果是：" + nativeByte);

			nativeByte = CLibrary.Instance.NLPIR_GetFileKeyWords("D:\\NLPIR\\feedback\\huawei\\5341\\5341\\产经广场\\2012\\5\\16766.txt", 10,false);

			System.out.print("关键词提取结果是：" + nativeByte);
*/
					
		/*} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}*/
    }
    	System.out.println("Over~~");
        CLibrary.Instance.NLPIR_Exit();
  }
}
