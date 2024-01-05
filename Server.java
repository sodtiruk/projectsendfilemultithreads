import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class Multithreaddownload extends Thread {

    private Socket socketdownload;

    public Multithreaddownload(Socket socket){
        this.socketdownload = socket;
    }

    @Override
    public void run(){
        try { 

            InputStream in = socketdownload.getInputStream();
            BufferedInputStream inp = new BufferedInputStream(in);
            DataInputStream input = new DataInputStream(inp);

            OutputStream out = socketdownload.getOutputStream();
            DataOutputStream output = new DataOutputStream(out);
            
            

            String pathflieclient_wannadownload = "";
            while (pathflieclient_wannadownload.isEmpty()){
                pathflieclient_wannadownload = input.readUTF();
            }

             String thread= "";
            while (thread.isEmpty()){
                thread = input.readUTF();
            }

            String startbyte = "";
            while (startbyte.isEmpty()){
                startbyte = input.readUTF();
            }
            
            long startbytes = Long.parseLong(startbyte); //casting string to long


            String endbyte= "";
            while (endbyte.isEmpty()){
                endbyte = input.readUTF();
            }

            long endbytes = Long.parseLong(endbyte); //casing string to long
            System.out.printf("thread = %s %s %s %s\n", thread, pathflieclient_wannadownload, startbyte, endbyte);

            //File file = new File(pathfile);       
            
            FileInputStream fis = new FileInputStream(pathflieclient_wannadownload);
            fis.skip(startbytes);

            OutputStream outfile = socketdownload.getOutputStream();

            byte[] bytes = new byte[1024];
            long count = 0;
            int sent;
            long thisfilesizethread = endbytes - startbytes + 1;
            System.out.println("filesizethread = " + thread); 

            while (((sent = fis.read(bytes)) > 0) && (count < thisfilesizethread)){
                outfile.write(bytes, 0, sent);
                count += sent;
                System.out.println("thread = " + thread + " " + count);
            }

            
        }catch (Exception e){
            System.out.println(e);
        }

    }
}

class Acceptclientdownload extends Thread {
    

    @Override
    public void run(){
        try {
            
            InetAddress ipserver = InetAddress.getByName("localhost");
            ServerSocket Serverdownload = new ServerSocket(2323, 100, ipserver);

            while(true){

                new Multithreaddownload(Serverdownload.accept()).start();   

            }

        }catch (Exception e){
            System.out.println(e);
        }
    }
}


class EchoThread extends Thread{

    private Socket socket; 

    public EchoThread(Socket socket){
       this.socket = socket; 
    }

    @Override
    public void run(){
        try {
            InputStream inp = this.socket.getInputStream();
            BufferedInputStream buffin = new BufferedInputStream(inp);
            DataInputStream input = new DataInputStream(buffin);
               

            InputStream in = null;
            OutputStream out = null;

            String mess = "";
            while(!mess.equals("stop")){
                mess = input.readUTF();
                //sendfile
                System.out.println("message from clients: " + mess); 
                
                if (mess.equals("/download")){


                    //client send path file to me for sender

                    String pathfile = "";
                    while (pathfile.isEmpty()){
                        pathfile = input.readUTF();
                    }
                    System.out.println(pathfile); 

                    File file = new File(pathfile);
                    System.out.println(file.length()); //filesize
                    
                    String filesize = String.valueOf(file.length()); //casting long to string

                    out = this.socket.getOutputStream();
                    DataOutputStream output = new DataOutputStream(out);
                    
                    output.writeUTF(filesize);
                    

                    
                    Thread appcept = new Acceptclientdownload();
                    appcept.start();
                }
                
            }
            
            out.close();
            in.close();
            socket.close();
                    
        }catch (Exception e){
            System.out.println(e);
        }
    }
}


class Server {
    public static void main(String args[]){
        try { 
            InetAddress ipserver = InetAddress.getByName("localhost");
            ServerSocket server = new ServerSocket(7772, 10, ipserver);
            //ArrayList<Socket> list = new ArrayList<>();
            
            while (true) {
                Socket socket = server.accept();
                System.out.println(socket.getInetAddress());
                new EchoThread(socket).start();
                
                //thread.test();
                //list.add(socket);   
            }
            
        }catch (Exception e){
            System.out.println(e);
        }

    }
}

