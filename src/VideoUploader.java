

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.bson.Document;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;
import static java.util.Arrays.asList;



@Path("/uploadvideo")
public class VideoUploader {
	
	public String geraChave(String titulo, String descricao) throws RemoteException {
		// TODO Auto-generated method stub
		String gera = titulo + descricao;
		MessageDigest mDigest=null;
		try {
			mDigest = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        byte[] result = mDigest.digest(gera.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }
         
        return sb.toString();
	}
	
	
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String uploadFile(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail,
			@FormDataParam("descricao") String desc){
		try {
			
			String response = geraChave(fileDetail.getFileName(), desc);
			AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials("AKIAI4OJDIHZLUZWM37A", "ZdBnnwRBbNA2KP72KiovYOqzgapxDzAnj9Oov22P"));
			
			writeToFile(uploadedInputStream, fileDetail.getFileName());
			List<Bucket> bck = s3.listBuckets();
			
			s3.putObject(new PutObjectRequest(bck.get(0).getName(),fileDetail.getFileName(),new File(fileDetail.getFileName())).withCannedAcl(CannedAccessControlList.PublicRead));
			String link = "https://s3-sa-east-1.amazonaws.com/mytubestorage/"+fileDetail.getFileName();
			MongoClient mC = new MongoClient(new MongoClientURI("mongodb://thiago:madera@ds047712.mongolab.com:47712/videolink"));
			MongoDatabase db = mC.getDatabase("videolink");
			db.getCollection("links").insertOne(new Document().append("videoID", response).append("s3Link", link));
			mC.close();
			new File(fileDetail.getFileName()).delete();
			return response;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Client exeception"+e.toString();
		}
	}
	
	private void writeToFile(InputStream uploadedInputStream,
			String uploadedFileLocation) {

			try {
				OutputStream out = new FileOutputStream(new File(
						uploadedFileLocation));
				int read = 0;
				byte[] bytes = new byte[1024];

				out = new FileOutputStream(new File(uploadedFileLocation));
				while ((read = uploadedInputStream.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				out.flush();
				out.close();
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

	@POST
	@Path("/download")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String downloadFile(@FormParam("download") String key,
			@Context HttpServletResponse _currentResponse){
		MongoClient mC = new MongoClient(new MongoClientURI("mongodb://thiago:madera@ds047712.mongolab.com:47712/videolink"));
		MongoDatabase db = mC.getDatabase("videolink");
		FindIterable<Document> it = db.getCollection("links").find(new Document("videoID",key));
		Document result = it.first();
		String link = result.getString("s3Link");
		mC.close();
		try {
			_currentResponse.sendRedirect(link);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return link;
	}
}
