package mytube.server;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.core.header.FormDataContentDisposition;

@Path("/uploadvideo")
public class VideoUploader {
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String uploadFile(
			@FormParam("file") InputStream uploadedInputStream,
			@FormParam("file") FormDataContentDisposition fileDetail,
			@FormParam("descricao") String desc){
		try {
			Registry reg = LocateRegistry.getRegistry("ec2-52-91-20-168.compute-1.amazonaws.com");
			GeraChave stub = (GeraChave) reg.lookup("GeraChave");
			String response = stub.geraChave(fileDetail.getFileName(), desc);
			return response;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Client exeception"+e.toString();
		}
	}
}
