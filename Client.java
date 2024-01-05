
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Scanner;


class Threaddownload extends Thread {

    private String pathfile;
    private String savefile;
    private int indexthread;
    private long startbyte;
    private long endbyte;

    public Threaddownload(String pathfile, String savefile, int indexthread, long startbyte, long endbyte) {
        this.pathfile = pathfile;
        this.savefile = savefile;
        this.indexthread = indexthread;
        this.startbyte = startbyte;
        this.endbyte = endbyte;
    }

    @Override
    public void run(){
        try {
            Socket download = new Socket("localhost", 2323);
            OutputStream outd= download.getOutputStream();
            DataOutputStream outputd = new DataOutputStream(outd);
            
             
            outputd.writeUTF(pathfile);
            outputd.writeUTF(""+this.indexthread);  //send message pathfile name want to download
            outputd.writeUTF(""+startbyte); 
            outputd.writeUTF(""+endbyte);


            byte[] bytes = new byte[1024];
            InputStream in = download.getInputStream();
            RandomAccessFile out = new RandomAccessFile(savefile, "rw");
            out.seek(startbyte);

            long threadfilesize = endbyte - startbyte + 1;

            long nub = 0;
            int count;

            while((nub < threadfilesize) && (count = in.read(bytes)) > 0){
                out.write(bytes, 0, count); 
                nub += count;
            }



        }catch (Exception e){
            System.out.println(e);
        }

    }
}

class Client {
    public static void main(String args[]){
        try {
            //recieved file
            Socket client = new Socket("localhost", 7772);

            OutputStream outcomment = client.getOutputStream();
            DataOutputStream output = new DataOutputStream(outcomment);
            String send = "";

            Scanner scan = new Scanner(System.in);

            FileOutputStream out = null;
            InputStream in = null;

            while(!send.equals("stop")){
                send = scan.nextLine();
                output.writeUTF(send);
         
                if (send.equals("/download")){
                    

                    int threadnumber = 10;
                    Thread.sleep(1000);
                    Thread[] thdownload = new Thread[threadnumber];

                    System.out.println("enterpathfile : ");
                    String pathfile = scan.next(); 

                    System.out.println("entersavefile : ");
                    String savepath = scan.next(); 
                    
                    output.writeUTF(pathfile);
                    
                    in = client.getInputStream();
                    BufferedInputStream inp = new BufferedInputStream(in);
                    DataInputStream input = new DataInputStream(inp);


                    String filesize = "";
                    while (filesize.isEmpty()){
                        filesize = input.readUTF();
                    }

                    System.out.println(filesize);
                    
                    long filesizes = Long.parseLong(filesize);
                    long splitsize = filesizes/threadnumber;


                    for(int i=0; i<threadnumber; i++){
                        
                        long start_byte = i * splitsize;
                        long end_byte = (i == threadnumber-1) ? filesizes - 1 : (i + 1) * splitsize - 1;
                        
                        thdownload[i] = new Threaddownload(pathfile, savepath, i, start_byte, end_byte);
                        thdownload[i].start();
                    }
                    
                    

                    for(Thread thread : thdownload){
                        thread.join();
                    }


                }
                
                

                
            }
            //out.close();
            //in.close();
            //client.close();
            

        }catch (Exception e){
            System.out.println(e);
        }
    }
}








