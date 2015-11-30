package mytube.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GeraChave extends Remote {
	String geraChave(String titulo, String descricao) throws RemoteException;
}
